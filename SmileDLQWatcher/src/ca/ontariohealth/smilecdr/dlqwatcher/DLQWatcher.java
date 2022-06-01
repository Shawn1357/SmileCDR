/**
 * 
 */
package ca.ontariohealth.smilecdr.dlqwatcher;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
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
import ca.ontariohealth.smilecdr.support.commands.DLQResponseContainer;
import ca.ontariohealth.smilecdr.support.commands.ProcessingMessage;
import ca.ontariohealth.smilecdr.support.commands.ProcessingMessageSeverity;
import ca.ontariohealth.smilecdr.support.commands.json.CommandParamAdapter;
import ca.ontariohealth.smilecdr.support.commands.json.MyInstantAdapter;
import ca.ontariohealth.smilecdr.support.commands.json.ReportRecordAdapter;
import ca.ontariohealth.smilecdr.support.commands.response.KeyValue;
import ca.ontariohealth.smilecdr.support.commands.response.ReportRecord;
import ca.ontariohealth.smilecdr.support.config.ConfigProperty;
import ca.ontariohealth.smilecdr.support.config.Configuration;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.PartitionInfo;


/**
 * @author shawn.brant
 *
 */
public class DLQWatcher extends BaseApplication 
{
static private final Logger 			        logr                 = LoggerFactory.getLogger(DLQWatcher.class);

static private final String				        CLI_INITIAL_CMD_SHRT = "i";
static private final String                     CLI_INITIAL_CMD_LONG = "initCmd";

static private final Long				        KILL_POLLER_MAX_WAIT = 10000L; // milliseconds

private Map<String, Consumer<String,String>>    allConsumers         = new HashMap<>();
private Map<String, Producer<String,String>>    allProducers         = new HashMap<>();

private	Consumer<String, String>		        controlConsumer 	 = null;
private	boolean							        exitWatcher     	 = false;
private	DLQPollingThread				        dlqPoller       	 = null;

private GsonBuilder                             jsonBuilder          = new GsonBuilder();

private int                                     rollingRcrdIndex     = 0;


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

//sendEMail( appConfig.configValue( ConfigProperty.EMAIL_TEMPLATE ) );

jsonBuilder.registerTypeAdapter( DLQCommandParam.class, new CommandParamAdapter() );
jsonBuilder.registerTypeAdapter( ReportRecord.class,    new ReportRecordAdapter() );
jsonBuilder.registerTypeAdapter( MyInstant.class,       new MyInstantAdapter() );
jsonBuilder.setPrettyPrinting();


/*
 * Create the consume that will be waiting for commands to appear on the
 * Control Kafka topic.
 * 
 */

String	controlTopic = appConfig.configValue( ConfigProperty.CONTROL_TOPIC_NAME_COMMAND );

controlConsumer = createConsumer( controlTopic );

logr.debug( "Subscribed to Control Topic: {}", controlTopic );

Integer	maxRunTime = appConfig.configInt( ConfigProperty.QUIT_AFTER_MILLIS, null );

listenForControlCommands( maxRunTime );

// Ensure the Poller Thread has been stopped.
stopPollingThread();


logr.debug( "Exiting: {}.launch", DLQWatcher.class.getSimpleName() );
return;
}



private void	listenForControlCommands( Integer maxRunTime )
{
logr.debug( "Entering: listenForControlCommands");

long	startTime = System.currentTimeMillis();
long    maxTime   = (maxRunTime != null) ? maxRunTime.longValue() : -1L;

int		pollInterval = appConfig.configInt( ConfigProperty.KAFKA_CONSUMER_POLL_INTERVAL, 500 ).intValue();
if (pollInterval < 0) pollInterval = 500;

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

if (controlConsumer != null)
	controlConsumer.close();

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
    
    logr.info( "Processed Command resulting in:" );
    logr.info( "\n{}", respAsJSON );
    
    Producer<String, String>    producer = createProducer( channel );
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
        
        }
    }

logr.debug( "Exiting: resturnResponse()" );
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
DLQResponseContainer    resp = null;

if ((cmd != null) && (cmd.getCommandToIssue() != null))
    {
    cmd.recordProcessingStartTimestamp();
    
    resp = new DLQResponseContainer( cmd );    
    
    switch (cmd.getCommandToIssue())
        {
        case    HELLO:
            resp.setOutcome( DLQCommandOutcome.SUCCESS );
            resp.addProcessingMessage( ProcessingMessage.DLQW_0000 );            
            resp.addReportEntry( DLQCommand.HELLO.commandStr() );
            break;
            
        case    LIST:
            logr.info("All known {} Commands:", appConfig.getApplicationName().appName() );
            resp.setOutcome( DLQCommandOutcome.SUCCESS );
            resp.addProcessingMessage( ProcessingMessage.DLQW_0000 );            
            
            for (DLQCommand crnt : DLQCommand.values())
                if (crnt != DLQCommand.UNKNOWN)
                    {
                    KeyValue keyVal = new KeyValue( crnt.commandStr(), crnt.usageStr() );
                    
                    String  rprtLine = String.format( "%s - %s", crnt.commandStr(), crnt.usageStr() );
                    logr.info( "   {}", rprtLine);
                    
                    resp.addReportEntry( keyVal );
                    }
                
            break;
            
        case    START:
            logr.info( "Starting the DLQ Poller Thread if it is not already running." );
            startPollingThread( resp );
            break;
                
        case    STOP:
            logr.info( "Stopping the DLQ Poller Thread if it is running." );
            stopPollingThread( resp );
            break;
                
        case    QUIT:
            logr.info( "Triggering Exit of: {}", appConfig.getApplicationName().appName() );
            exitWatcher = true;
            
            resp.setOutcome( DLQCommandOutcome.SUCCESS );
            resp.addProcessingMessage( ProcessingMessage.DLQW_0000 );            
            break;
            
        case    UNKNOWN:
        default:
            logr.error( "Received unknown command: {}", cmd.getCommandToIssue().commandStr() );
        }
    
    resp.recordCompleteTimestamp();
    }


return resp;
}




private void	startPollingThread( DLQResponseContainer resp )
{

logr.debug( "Entering: startPollingThread" );

if (dlqPoller == null)
	{
	logr.debug( "DLQ Poller Thread does not exist: creating it.");
	dlqPoller = new DLQPollingThread( appConfig );
	}

if (dlqPoller.isAlive())
    {
    String  msg = "DLQ Poller Thread is already alive: nothing to do.";
    
	logr.debug( msg );
	resp.addProcessingMessage( new ProcessingMessage( ProcessingMessageSeverity.INFO,
	                                                  "DLQW-0001",
	                                                  msg ) );
    }

else
	{
	logr.debug( "Starting DLQ Poller Thread..." );
	dlqPoller.start();
	
	// Give it a moment to fire up.
	try 
		{
		Thread.sleep( 250 );
		}
	
	catch (InterruptedException e) 
		{
		e.printStackTrace();
		}
	
	if (dlqPoller.isAlive())
	    {
	    String msg = "DLQ Poller Thread is running.";
	    
		logr.debug( msg );
		resp.setOutcome( DLQCommandOutcome.SUCCESS );
		resp.addProcessingMessage( new ProcessingMessage( ProcessingMessageSeverity.INFO,
		                                                  "DLQW-0003",
		                                                  msg ) );
	    }
	
	else
	    {
	    String msg = "DLQ Poller Thread did not start.";
	    
		logr.error( msg );
		resp.setOutcome( DLQCommandOutcome.ERROR );
		resp.addProcessingMessage( new ProcessingMessage( ProcessingMessageSeverity.ERROR,
		                                                  "DLQW-0002",
		                                                  msg ) );
	    }
	}

logr.debug( "Exiting: startPollingThread" );
return;	
}




private void    stopPollingThread()
{
DLQResponseContainer throwaway = new DLQResponseContainer();

stopPollingThread( throwaway );

return;
}



@SuppressWarnings("deprecation")
private void	stopPollingThread( DLQResponseContainer resp )
{
logr.debug( "Entering: stopPollingThread" );
if (dlqPoller != null)
	{
	Long 	killStart 	= System.currentTimeMillis();
	boolean	keepWaiting = true;
	
	logr.debug( "Polling Thread object exists. Setting flag for it to stop." );			
	dlqPoller.indicateToStopThread();
	
	if (dlqPoller.isAlive())
		logr.debug( "DLQ Polling Thread is Alive, we need to wait up to {} milliseconds for it to end.", KILL_POLLER_MAX_WAIT );
	
	while (dlqPoller.isAlive() && keepWaiting)
		{
		try
			{
			logr.debug( "Waiting..." );
			Thread.sleep( 250 );
			} 
		catch (InterruptedException e)
			{
			e.printStackTrace();
			}
		
		keepWaiting = (System.currentTimeMillis() <= killStart + KILL_POLLER_MAX_WAIT);
		}
	
	// If the Poller Thread is still alive after our maximum wait time,
	// kill the thread.
	if (dlqPoller.isAlive())
		{
		String    msg = String.format( "DLQ Poller is still alive after %l milliseconds. Killing it.", KILL_POLLER_MAX_WAIT );
		logr.debug( msg );
		dlqPoller.stop();
		
		resp.setOutcome( DLQCommandOutcome.WARNINGS );
		resp.addProcessingMessage( new ProcessingMessage( ProcessingMessageSeverity.WARN,
		                                                  "DLQW-0005",
		                                                  msg ) );
		}
	
	dlqPoller = null;
	}

else
    {
    String msg = "DLQ Poller Thread is not running: nothing to do.";
	logr.debug( msg );
	
	resp.setOutcome( DLQCommandOutcome.SUCCESS );
	resp.addProcessingMessage( new ProcessingMessage( ProcessingMessageSeverity.INFO,
	                                                  "DLQW-0004",
	                                                  msg ) );
    }

logr.debug( "Exiting: stopPollingThread" );
return;
}




private Consumer<String, String>	createConsumer( String topicName )
{
Consumer<String, String>  rtrn  = allConsumers.get( topicName );

if ((rtrn == null) && (topicName != null) && (topicName.length() > 0))
    {
    Properties	props               = new Properties();
    
    String		groupID 		 	= appConfig.configValue( ConfigProperty.KAFKA_CONTROL_GROUP_ID,
    		                                              	 appConfig.getApplicationName().appName() + ".control.group.id" );
    
    String		bootstrapServers 	= appConfig.configValue( ConfigProperty.BOOTSTRAP_SERVERS );
    String      keyDeserializer  	= Configuration.KAFKA_KEY_DESERIALIZER_CLASS_NAME;
    String      valueDeserializer	= Configuration.KAFKA_VALUE_DESERIALIZER_CLASS_NAME;
    
    logr.debug( "   Group ID:           {}", groupID );
    logr.debug( "   Bootstrap Servers:  {}", bootstrapServers );
    logr.debug( "   Key Deserializer:   {}", keyDeserializer );
    logr.debug( "   Value Deserializer: {}", valueDeserializer );
    
    props.put( ConsumerConfig.GROUP_ID_CONFIG,                 groupID );
    props.put( ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,        bootstrapServers );
    props.put( ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,   keyDeserializer );
    props.put( ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer );
    
    rtrn = new KafkaConsumer<>( props );
    rtrn.subscribe( Collections.singletonList( topicName ) );
    
    allConsumers.put( topicName,  rtrn );
    }

return rtrn;	
}



private Producer<String, String>    createProducer( String topicName )
{
Producer<String, String>    rtrn = allProducers.get( topicName );

if ((rtrn == null) && (topicName != null) && (topicName.length() > 0))
    {
    Properties  props = new Properties();

    String      clientID         = appConfig.getApplicationName().appName();
    String      bootstrapServers = appConfig.configValue( ConfigProperty.BOOTSTRAP_SERVERS );
    String      keySerializer    = Configuration.KAFKA_KEY_SERIALIZER_CLASS_NAME;
    String      valueSerializer  = Configuration.KAFKA_VALUE_SERIALIZER_CLASS_NAME;

    logr.debug( "   Client ID:         {}", clientID );
    logr.debug( "   Bootstrap Servers: {}", bootstrapServers );
    logr.debug( "   Key Serializer:    {}", keySerializer );
    logr.debug( "   Value Serializer:  {}", valueSerializer );

    props.put( ProducerConfig.CLIENT_ID_CONFIG,                 clientID );
    props.put( ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,         bootstrapServers );
    props.put( ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,      keySerializer );
    props.put( ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,    valueSerializer );

    rtrn = new KafkaProducer<>( props );
    allProducers.put( topicName, rtrn );
    }


return rtrn;
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