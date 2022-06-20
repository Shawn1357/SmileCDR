/**
 * 
 */
package ca.ontariohealth.smilecdr.dlqwatcher;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ca.ontariohealth.smilecdr.BaseApplication;
import ca.ontariohealth.smilecdr.support.MyInstant;
import ca.ontariohealth.smilecdr.support.commands.DLQCommand;
import ca.ontariohealth.smilecdr.support.commands.DLQCommandContainer;
import ca.ontariohealth.smilecdr.support.commands.DLQCommandOutcome;
import ca.ontariohealth.smilecdr.support.commands.DLQCommandParam;
import ca.ontariohealth.smilecdr.support.commands.DLQRecordsInterpreter;
import ca.ontariohealth.smilecdr.support.commands.DLQResponseContainer;
import ca.ontariohealth.smilecdr.support.commands.EMailNotifier;
import ca.ontariohealth.smilecdr.support.commands.ProcessingMessage;
import ca.ontariohealth.smilecdr.support.commands.ProcessingMessageCode;
import ca.ontariohealth.smilecdr.support.commands.json.CommandParamAdapter;
import ca.ontariohealth.smilecdr.support.commands.json.JSONApplicationSupport;
import ca.ontariohealth.smilecdr.support.commands.json.MyInstantAdapter;
import ca.ontariohealth.smilecdr.support.commands.json.ProcessingMessageAdapter;
import ca.ontariohealth.smilecdr.support.commands.json.ReportRecordAdapter;
import ca.ontariohealth.smilecdr.support.commands.response.CWMDLQRecordEntry;
import ca.ontariohealth.smilecdr.support.commands.response.KeyValue;
import ca.ontariohealth.smilecdr.support.commands.response.ReportRecord;
import ca.ontariohealth.smilecdr.support.config.ConfigProperty;
import ca.ontariohealth.smilecdr.support.kafka.KafkaAdministration;
import ca.ontariohealth.smilecdr.support.kafka.KafkaConsumerHelper;
import ca.ontariohealth.smilecdr.support.kafka.KafkaProducerHelper;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;


/**
 * @author shawn.brant
 *
 */
public class DLQWatcher extends BaseApplication 
{
static private final Logger 			        logr                 = LoggerFactory.getLogger(DLQWatcher.class);

static private final String				        CLI_INITIAL_CMD_SHRT = "i";
static private final String                     CLI_INITIAL_CMD_LONG = "initCmd";

private	Consumer<String, String>		        controlConsumer 	 = null;
private	boolean							        exitWatcher     	 = false;
private	DLQPollingThread				        dlqPoller       	 = null;
private DLQParkingThread                        dlqParker            = null;         

private GsonBuilder                             jsonBuilder          = new GsonBuilder();

private int                                     rollingRcrdIndex     = 0;


private enum ThreadIndicator 
    { DLQ_POLLING_THREAD( "DLQ Poller" ), 
      PARKING_THREAD(     "Parker" );

    private String threadNm;
    
    private ThreadIndicator( String name ) { threadNm = name; }
    public  String threadName() { return threadNm; }
    };

/**
 * @param args
 * 
 */
public static void main(String[] args) 
{
logr.debug( "Entering: main" );

DLQWatcher	watcher = new DLQWatcher();
watcher.launch(args);

logr.debug( "Exiting: main");
return;
}


	
@Override
protected	void launch()
{
logr.debug( "Entering: {}.launch", DLQWatcher.class.getSimpleName() );

System.err.println( appSignature() );

//sendEMail( appConfig.configValue( ConfigProperty.EMAIL_TEMPLATE ) );

JSONApplicationSupport.registerGsonTypeAdpaters( jsonBuilder );
jsonBuilder.setPrettyPrinting();


/*
 * Ensure all the Kafka topics exist and are set to the correct
 * entry retention periods.
 * 
 */

{
KafkaAdministration adminConn = new KafkaAdministration( appConfig );

String  topicName = appConfig.configValue( ConfigProperty.KAFKA_DLQ_TOPIC_NAME );
String  retention = appConfig.configValue( ConfigProperty.KAFKA_DLQ_RETENTION_HOURS );

adminConn.ensureTopicExists( topicName );
adminConn.setTopicRetentionPeriod( topicName, retention );

topicName = appConfig.configValue( ConfigProperty.KAFKA_PARK_TOPIC_NAME );
retention = appConfig.configValue( ConfigProperty.KAFKA_PARK_RETENTION_HOURS );

adminConn.ensureTopicExists( topicName );
adminConn.setTopicRetentionPeriod( topicName, retention );

topicName = appConfig.configValue( ConfigProperty.CONTROL_TOPIC_NAME_COMMAND );
retention = appConfig.configValue( ConfigProperty.KAFKA_DEFAULT_TOPIC_RETENTION );

adminConn.ensureTopicExists( topicName );
adminConn.setTopicRetentionPeriod( topicName, retention );

topicName = appConfig.configValue( ConfigProperty.CONTROL_TOPIC_NAME_RESPONSE );
retention = appConfig.configValue( ConfigProperty.KAFKA_DEFAULT_TOPIC_RETENTION );

adminConn.ensureTopicExists( topicName );
adminConn.setTopicRetentionPeriod( topicName, retention );
}


/*
 * Create the consumer that will be waiting for commands to appear on the
 * Control Kafka topic.
 * 
 */

String      groupID = appConfig.configValue( ConfigProperty.KAFKA_CONTROL_GROUP_ID,
                                             appConfig.getApplicationName().appName() + ".control.group.id" );

String  controlTopic = appConfig.configValue( ConfigProperty.CONTROL_TOPIC_NAME_COMMAND );

controlConsumer = KafkaConsumerHelper.createConsumer( appConfig, groupID, controlTopic );

logr.debug( "Subscribed to Control Topic: {}", controlTopic );

Integer maxRunTime = appConfig.configInt( ConfigProperty.QUIT_AFTER_MILLIS, null );


/*
 * Start listening for Control Commands.
 * 
 */

listenForControlCommands( maxRunTime );

// Ensure the Poller Thread has been stopped.
stopThreads();


logr.debug( "Exiting: {}.launch", DLQWatcher.class.getSimpleName() );
return;
}



private void	listenForControlCommands( Integer maxRunTime )
{
logr.debug( "Entering: listenForControlCommands");

long	startTime = System.currentTimeMillis();
long    maxTime   = (maxRunTime != null) ? maxRunTime.longValue() : -1L;

int		pollInterval = appConfig.configInt( ConfigProperty.KAFKA_CONSUMER_POLL_INTERVAL ).intValue();

if (pollInterval < 0)
    pollInterval = 500;

Duration	pollDuration = Duration.ofMillis( pollInterval );


if (controlConsumer == null)
	{
	logr.error( "Kafka Control Topic Consumer was not defined!" );
	exitWatcher = true;
	}

if (maxTime == 0L)
	exitWatcher = true;

/*
 * List the topics available to the Control Consumer
 * 
 */

Map<String, List<PartitionInfo>> topicList = controlConsumer.listTopics();
logr.debug( "Control Consumer has access to {} topic(s).", topicList.size() );

for (String crntTopic : topicList.keySet())
	logr.debug( "   Topic Name: {}", crntTopic );


/*
 * Check for and process the initial command(s).
 * 
 */

if (cmdLine.hasOption( CLI_INITIAL_CMD_LONG ))
	{
	String[] rawInitCmds = cmdLine.getOptionValues( CLI_INITIAL_CMD_LONG );
	
	if ((rawInitCmds != null) && (rawInitCmds.length > 0))
		{
		for (String initCmd : rawInitCmds)
			{
			logr.debug( "Processing Initial Command: {}", initCmd != null ? initCmd : "<NULL>" );
			
			DLQCommandContainer  cmdLineCmd  = constructCommandFromCmdLine( initCmd.strip() );
			DLQResponseContainer cmdLineResp = processReceivedCommand( cmdLineCmd );
			
			returnResponse( cmdLineResp );
			}
		}
	}


/*
 * Start the main control listener loop.
 * 
 */

if (!exitWatcher)
	{
	if (maxTime < 0)
		logr.debug( "Starting control loop for an indefinite period" );
	
	else
		logr.debug( "Starting control loop to run for max {} milliseconds.", maxTime );
	}

while (!exitWatcher)
	{
	final	ConsumerRecords<String, String> rcrds = controlConsumer.poll( pollDuration );
	
	if ((rcrds != null) && (rcrds.count() > 0))
		{
		logr.debug( "Received {} Control Record(s).", rcrds.count() );
		for (ConsumerRecord<String, String> crnt : rcrds)
			{
			if (crnt != null)
			    {
				DLQResponseContainer resp = processReceivedCommand( crnt.value() );
				
				returnResponse( resp );
			    }
			}
		}
	
	controlConsumer.commitAsync();
	
	if ((maxTime >= 0) && (!exitWatcher))
		exitWatcher = ((System.currentTimeMillis() - startTime) > maxTime);
	}

//if (controlConsumer != null)
	//controlConsumer.close();

logr.debug( "Exiting: listenForControlCommands" );
return;
}




private void    returnResponse( DLQResponseContainer resp )
{
logr.debug( "Entering: returnResponse" );

DLQCommandContainer cmd     = (resp != null) ? resp.getSourceCommand()      : null;
String              channel = (cmd != null)  ? cmd.getResponseChannelName() : null;

if ((resp != null) && (cmd != null) && (channel != null) && (channel.length() > 0))
    {
    Gson    xltrToJSON = jsonBuilder.create();
    String  respAsJSON = xltrToJSON.toJson( resp );
    
    logr.info( "Processed Command resulting in the following JSON being returned:" );
    logr.info( "\n{}", respAsJSON );
    
    Producer<String, String>    producer = KafkaProducerHelper.createProducer( appConfig, channel );
    if (producer != null)
        {
        long            crntTime = System.currentTimeMillis();
        String          msgID    = String.valueOf( crntTime ) + String.format( "-%04d", rollingRcrdIndex++ );
        RecordMetadata  metadata = null;
        
        if (rollingRcrdIndex >= 10000)
            rollingRcrdIndex = 0;
        
        ProducerRecord<String, String> record = new ProducerRecord<>( channel,
                                                                      msgID,
                                                                      respAsJSON );
        try
            {
            metadata = producer.send( record ).get();
            } 
        
        catch (InterruptedException e)
            {
            // TODO Auto-generated catch block
            e.printStackTrace();
            } 
        
        catch (ExecutionException e)
            {
            // TODO Auto-generated catch block
            e.printStackTrace();
            }
        
        
        
        if (metadata != null)
            {
            logr.debug( "Metadata Returned:" );
            logr.debug( "   Has Offset:            {}", metadata.hasOffset()    ? "Yes" : "No" );
            logr.debug( "   Has Timestamp:         {}", metadata.hasTimestamp() ? "Yes" : "No" );
            
            if (metadata.hasOffset())
                logr.debug( "   Offset Value:          {}", metadata.offset() );
            
            if (metadata.hasTimestamp())
                logr.debug( "   Timestamp:             {}", metadata.timestamp() );
            
            logr.debug( "   Partition:             {}", metadata.partition() );
            logr.debug( "   Serialized Key Size:   {}", metadata.serializedKeySize() );
            logr.debug( "   Serialized Value Size: {}", metadata.serializedValueSize() );
            }
        
        else
            logr.error( "Kafka Send Request resulted in null Metadata" );
        
        //producer.close();
        }
    }

logr.debug( "Exiting: returnResponse()" );
return;
}





private DLQCommandContainer constructCommandFromCmdLine( String initCmd )
{
DLQCommandContainer cmdLineCmd = null;

if ((initCmd != null) && (initCmd.length() > 0))
    {
    DLQCommand  newCmdType = DLQCommand.valueOf( initCmd );
    
    if (newCmdType != null)
        {
        cmdLineCmd = new DLQCommandContainer();
        
        switch (newCmdType)
            {
            case    HELLO:
            case    LIST:
            case    START:
            case    STOP:
            case    DLQLIST:
            case    DLQEMAIL:
            case    QUIT:
                cmdLineCmd.setCommandToIssue( newCmdType );
                break;
                
            case    UNKNOWN:
                logr.error( "Unrecognized Command Line Initial Command: '{}' - Ignoring...", initCmd );
                break;
                
            default:
                logr.error( "Unexpected Command Line Initial Command: '{}' - Ignoring...", initCmd );
                newCmdType = null;
                break;
            }
        }
    }
return cmdLineCmd;
}



protected   DLQResponseContainer    processReceivedCommand( String jsonCmd )
{
DLQResponseContainer    resp    = null;

logr.debug( "Entering: processReceivedCommand(String)" );

if ((jsonCmd != null) && (jsonCmd.length() > 0))
    {
    logr.debug( "JSON string to be parsed:" );
    logr.debug( "\n{}", jsonCmd );
    
    DLQCommandContainer     cmdObj  = DLQCommandContainer.fromJSON( jsonCmd );
    
    if (cmdObj != null)
        {
        logr.debug( "Received Control Command: {}", cmdObj.getCommandToIssue().commandStr() );

        resp = processReceivedCommand( cmdObj );
        }
    
    else
        {
        logr.error( "Unable to translate source string into Command Container object." );
        }
    }

else
    logr.debug( "Input JSON string is null or zero length." );

logr.debug( "Exiting: processingReceivedCommand(String)" );
return resp;    
}





protected   DLQResponseContainer    processReceivedCommand( DLQCommandContainer cmd )
{
DLQResponseContainer    resp  = null;
DLQRecordsInterpreter   rcrds = null;

if ((cmd != null) && (cmd.getCommandToIssue() != null))
    {
    cmd.recordProcessingStartTimestamp();
    
    resp = new DLQResponseContainer( cmd );    
    
    switch (cmd.getCommandToIssue())
        {
        case    HELLO:
            resp.setOutcome( DLQCommandOutcome.SUCCESS );
            resp.addProcessingMessage( new ProcessingMessage( ProcessingMessageCode.DLQW_0000, appConfig ) );            
            resp.addReportEntry( DLQCommand.HELLO.commandStr() );
            break;
            
        case    LIST:
            logr.info("All known {} Commands:", appConfig.getApplicationName().appName() );
            resp.setOutcome( DLQCommandOutcome.SUCCESS );
            resp.addProcessingMessage( new ProcessingMessage( ProcessingMessageCode.DLQW_0000, appConfig ) );            
            
            for (DLQCommand crnt : DLQCommand.values())
                if (crnt != DLQCommand.UNKNOWN)
                    {
                    KeyValue keyVal = new KeyValue( crnt.commandStr(), crnt.usageStr() );
                    
                    String  rprtLine = String.format( "%s - %s", crnt.commandStr(), crnt.usageStr() );
                    logr.info( "   {}", rprtLine);
                    
                    resp.addReportEntry( keyVal );
                    }
                
            break;
            
        case    DLQLIST:
            logr.info( "Starting process to list DLQ entries." );
            resp.setOutcome( DLQCommandOutcome.SUCCESS );
            resp.addProcessingMessage( new ProcessingMessage( ProcessingMessageCode.DLQW_0000, appConfig ) );            
            rcrds = listDLQEntries( resp );
            break;
         
        case    DLQEMAIL:
            logr.info( "Starting process to email DLQ entries." );
            resp.setOutcome( DLQCommandOutcome.SUCCESS );
            resp.addProcessingMessage( new ProcessingMessage( ProcessingMessageCode.DLQW_0000, appConfig ) );            
            rcrds = listDLQEntries( resp );
            break;
            
        case    START:
            logr.info( "Starting the DLQ Poller Thread if it is not already running." );
            resp.setOutcome( DLQCommandOutcome.SUCCESS );
            resp.addProcessingMessage( new ProcessingMessage( ProcessingMessageCode.DLQW_0000, appConfig ) );            
            startThreads( resp );
            break;
                
        case    STOP:
            logr.info( "Stopping the DLQ Poller Thread if it is running." );
            resp.setOutcome( DLQCommandOutcome.SUCCESS );
            resp.addProcessingMessage( new ProcessingMessage( ProcessingMessageCode.DLQW_0000, appConfig ) );            
            stopThreads( resp );
            break;
                
        case    QUIT:
            logr.info( "Triggering Exit of: {}", appConfig.getApplicationName().appName() );
            exitWatcher = true;
            
            resp.setOutcome( DLQCommandOutcome.SUCCESS );
            resp.addProcessingMessage( new ProcessingMessage( ProcessingMessageCode.DLQW_0000, appConfig ) );            
            break;
            
        case    UNKNOWN:
        default:
            logr.error( "Received unknown command: {}", cmd.getCommandToIssue().commandStr() );
        }
    
    resp.recordCompleteTimestamp();
    
    /*
     * Process further if the request is email the response.
     * 
     */
    
    switch (cmd.getCommandToIssue())
        {
        case    DLQEMAIL:
            // The response has some number of records to be emailed.
            // Lets do that:
            String  dlqEmailTemplateNm = appConfig.configValue( ConfigProperty.EMAIL_DLQLIST_TEMPLATE_NAME );
            EMailNotifier.sendEMail( appConfig, dlqEmailTemplateNm, rcrds );
            break;
            
        default:
            // Nothing to do... we can skip this part.
            break;
        }
    }


return resp;
}



private DLQRecordsInterpreter    listDLQEntries( DLQResponseContainer resp )
{
DLQRecordsInterpreter interpRcrds = new DLQRecordsInterpreter( appConfig );

Properties  disabledAutoCommit = new Properties();     

String topicNm = appConfig.configValue( ConfigProperty.KAFKA_DLQ_TOPIC_NAME );
String groupID = appConfig.configValue( ConfigProperty.KAFKA_DLQ_LISTER_GROUP_ID,
                                        appConfig.getApplicationName().appName() + ".control.group.id" );

disabledAutoCommit.setProperty( "enable.auto.commit", "false" );
disabledAutoCommit.setProperty( "auto.offset.reset",  "earliest" );

Consumer<String,String> lister = KafkaConsumerHelper.createConsumer( appConfig,
                                                                     groupID,
                                                                     topicNm,
                                                                     disabledAutoCommit );

logr.debug( "Actually subscribed to the following topic(s):" );
for (String crntSub : lister.subscription())
    {
    if (crntSub != null)
        logr.debug( "   {}", crntSub );
    }

logr.debug( "Assigned to the following topic partition(s):" );
for (TopicPartition crntPart : lister.assignment())
    {
    if (crntPart != null)
        logr.debug( "   {} - {}", crntPart.topic(), crntPart.partition() );
    }

lister.seekToBeginning( lister.assignment() );
lister.commitSync();

if (lister != null)
    {
    Long        loopStartedAt = System.currentTimeMillis();
    Long        pollInterval  = appConfig.configLong( ConfigProperty.KAFKA_CONSUMER_POLL_INTERVAL, Long.valueOf( 250L ) );
    Long        maxWait       = appConfig.configLong( ConfigProperty.RESPONSE_WAIT_MILLIS, Long.valueOf( 10000L ) );
    Duration    interval      = Duration.ofMillis( pollInterval );
    int         totalRcvd     = 0;
    boolean     loopAgain     = true;
    
    do
        {
        ConsumerRecords<String,String> rcrds = lister.poll( interval );
        logr.debug( "Received {} KAFKA.DLQ Event(s).", rcrds.count() );
        
        if ((rcrds != null) && (rcrds.count() > 0))
            {
            interpRcrds.addDLQRecords( rcrds );
            
            for (ConsumerRecord<String, String> crnt : rcrds)
                {
                if (crnt != null)
                    {                    
                    CWMDLQRecordEntry  entry = new CWMDLQRecordEntry( crnt, appConfig );
                    logr.info( "DLQ Entry:" );
                    logr.info( "    Timestamp:       {}", entry.dlqEntryTimestamp().getEpochMillis() );
                    logr.info( "    Subscription ID: {}", entry.subscriptionID() );
                    logr.info( "    Resource Type:   {}", entry.resourceType() );
                    logr.info( "    Resource ID:     {}", entry.resourceID() );
                    
                    resp.addReportEntry( entry );
                    
                    }
                }
            }
        
        totalRcvd += rcrds.count();
        
        loopAgain = ((System.currentTimeMillis() <= loopStartedAt + maxWait) &&
                     ((totalRcvd == 0) || (rcrds.count() == 0)));
        }
    
    while (loopAgain);
    
    //lister.close();
    }


return interpRcrds;
}



private DLQCommandOutcome    startThreads( DLQResponseContainer resp )
{
DLQCommandOutcome   outcome = DLQCommandOutcome.SUCCESS;

for (ThreadIndicator crntThrdInd : ThreadIndicator.values())
    {
    DLQCommandOutcome   rslt           = DLQCommandOutcome.SUCCESS;
    MyThread            thrdObjToStart = null;
    Boolean             cfgStartThread = null;
    
    switch (crntThrdInd)
        {
        case    DLQ_POLLING_THREAD:
            cfgStartThread = appConfig.configBool( ConfigProperty.START_DLQ_POLL_THREAD );
            if (cfgStartThread && (dlqPoller == null))
                dlqPoller = new DLQPollingThread( appConfig );
            
            thrdObjToStart = dlqPoller;
            
            break;
            
        case    PARKING_THREAD:
            cfgStartThread = appConfig.configBool( ConfigProperty.START_DLQ_PARK_THREAD );
            if (cfgStartThread && (dlqParker == null))
                dlqParker = new DLQParkingThread( appConfig );
            
            thrdObjToStart = dlqParker;
            
            break;
            
        default:
            // Should never get here.
            break;
        }
    
    if (thrdObjToStart != null)
        rslt = startThread( resp, thrdObjToStart, crntThrdInd, cfgStartThread );
    
    if (rslt.asPriority() > outcome.asPriority())
        outcome = rslt;
    }


resp.setOutcome( outcome );
return outcome;
}


private DLQCommandOutcome    startThread( DLQResponseContainer  resp, 
                                          MyThread              thrdObj, 
                                          ThreadIndicator       threadInd,
                                          Boolean               startThread )
{
DLQCommandOutcome   rslt = DLQCommandOutcome.SUCCESS;
logr.debug( "Entering: startThread" );

if (thrdObj.isAlive())
    {
    if (startThread)
        {
        // Thread is already alive. Nothing to do.
        ProcessingMessage   procMsg = new ProcessingMessage( ProcessingMessageCode.DLQW_0001, appConfig, threadInd.threadName() );
        resp.addProcessingMessage( procMsg );
        logr.debug( procMsg.getMsgDesc() );
        }
    
    else
        {
        // Thread is running but the configuration says it should not be.
        ProcessingMessage procMsg = new ProcessingMessage( ProcessingMessageCode.DLQW_0005, appConfig, threadInd.threadName() );
        resp.addProcessingMessage( procMsg );
        
        logr.debug( procMsg.getMsgDesc() );

        if (rslt.asPriority() < DLQCommandOutcome.WARNINGS.asPriority())
            rslt = DLQCommandOutcome.WARNINGS;
        }
    }

else
    {
    logr.debug( "Starting {} Thread...", threadInd.threadName() );
    thrdObj.start();
    
    // Give it a moment to fire up.
    try 
        {
        Thread.sleep( 250 );
        }
    
    catch (InterruptedException e) 
        {
        e.printStackTrace();
        }
    
    if (thrdObj.isAlive())
        {
        // Thread started.
        ProcessingMessage procMsg = new ProcessingMessage( ProcessingMessageCode.DLQW_0003, appConfig, threadInd.threadName() );
        resp.addProcessingMessage( procMsg );
        logr.debug( procMsg.getMsgDesc() );
        }
    
    else
        {
        // Thread did not start.
        if (rslt.asPriority() < DLQCommandOutcome.ERROR.asPriority())
            rslt = DLQCommandOutcome.ERROR;
        
        ProcessingMessage procMsg = new ProcessingMessage( ProcessingMessageCode.DLQW_0002, appConfig, threadInd.threadName() );
        resp.addProcessingMessage( procMsg );
        logr.error( procMsg.getMsgDesc() );
        }
    }

logr.debug( "Exiting: startThread" );
return rslt;
}



private DLQCommandOutcome   stopThreads()
{
return stopThreads( null );
}



private DLQCommandOutcome   stopThreads( DLQResponseContainer resp )
{
DLQCommandOutcome outcome = DLQCommandOutcome.SUCCESS;

for (ThreadIndicator crntThrdInd : ThreadIndicator.values())
    {
    DLQCommandOutcome   rslt          = DLQCommandOutcome.SUCCESS;
    MyThread            thrdObjToStop = null;
    Boolean             cfgStopThread = null;
    
    switch (crntThrdInd)
        {
        case    DLQ_POLLING_THREAD:
            thrdObjToStop = dlqPoller;
            cfgStopThread = appConfig.configBool( ConfigProperty.START_DLQ_POLL_THREAD );
            
            break;
            
        case    PARKING_THREAD:
            thrdObjToStop = dlqParker;
            cfgStopThread = appConfig.configBool( ConfigProperty.START_POLL_PARK_THREAD );
            break;
            
        default:
            // Should never get here.
            break;
        }
    
    if (thrdObjToStop != null)
        rslt = stopThread( resp, thrdObjToStop, crntThrdInd, cfgStopThread );
    
    switch (crntThrdInd)
        {
        case    DLQ_POLLING_THREAD:
            dlqPoller = null;
            break;
            
        case    PARKING_THREAD:
            dlqParker = null;
            break;
            
        default:
            // Should never get here.
            break;
        }
 
    
    if (rslt.asPriority() > outcome.asPriority())
        outcome = rslt;
    }


if (resp != null)
    resp.setOutcome( outcome );

return outcome;
}



private DLQCommandOutcome   stopThread( DLQResponseContainer resp,
                                        MyThread             thrdToStop,
                                        ThreadIndicator      thrdInd,
                                        Boolean              stopThrd )
{
DLQCommandOutcome   outcome = DLQCommandOutcome.SUCCESS;
logr.debug( "Entering: stopThread for {}", thrdInd.threadName() );

if ((thrdToStop != null) && (thrdToStop.isAlive()))
    {
    if (!stopThrd)
        {
        // Thread is running but Configuration indicates it should not be.
        ProcessingMessage procMsg = new ProcessingMessage( ProcessingMessageCode.DLQW_0005, appConfig, thrdInd.threadName() );
        resp.addProcessingMessage( procMsg );        
        logr.debug( procMsg.getMsgDesc() );
        
        if (outcome.asPriority() < DLQCommandOutcome.WARNINGS.asPriority())
            outcome = DLQCommandOutcome.WARNINGS;
        }
    
    DLQCommandOutcome rslt = MyThread.stopThread( resp, appConfig, thrdToStop, thrdInd.threadName() );
    if (outcome.asPriority() < rslt.asPriority())
        outcome = rslt;
    }

else
    {
    // Thread is not running. Nothing to do.
    ProcessingMessage procMsg = new ProcessingMessage( ProcessingMessageCode.DLQW_0004, appConfig, thrdInd.threadName() );
    logr.debug( procMsg.getMsgDesc() );
        
    if (resp != null)
        resp.addProcessingMessage( procMsg );
    
    if (outcome.asPriority() < DLQCommandOutcome.SUCCESS.asPriority())
        outcome = DLQCommandOutcome.SUCCESS;
     }

logr.debug( "Exiting: stopThread for {}", thrdInd.threadName() );
return outcome;
}




@Override
protected void createCLIOptions( Options cmdLineOpts ) 
{
super.createCLIOptions( cmdLineOpts );

Option	initCmd = new Option( CLI_INITIAL_CMD_SHRT,  CLI_INITIAL_CMD_LONG,  true, "DLQWatcher Command to run on startup. This option is repeatable." );

cmdLineOpts.addOption( initCmd );

return;
}




@Override
protected void displayUsage() 
{
super.displayUsage();
System.out.println( "" );
System.out.println( "Available DLQ Watcher Commands:" );
for (DLQCommand crnt : DLQCommand.values())
	if (crnt != DLQCommand.UNKNOWN)
		{
		String	cmdHelp = "   " + crnt.commandStr() + " - " + crnt.usageStr();
		System.out.println( cmdHelp );
		}
System.out.println( "" );
return;
}
}