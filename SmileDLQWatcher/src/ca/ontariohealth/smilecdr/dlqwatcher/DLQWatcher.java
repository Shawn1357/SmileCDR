/**
 * 
 */
package ca.ontariohealth.smilecdr.dlqwatcher;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ca.ontariohealth.smilecdr.BaseApplication;
import ca.ontariohealth.smilecdr.support.commands.DLQCommand;
import ca.ontariohealth.smilecdr.support.commands.DLQCommandContainer;
import ca.ontariohealth.smilecdr.support.commands.DLQCommandOutcome;
import ca.ontariohealth.smilecdr.support.commands.DLQCommandParam;
import ca.ontariohealth.smilecdr.support.commands.DLQRecordEntry;
import ca.ontariohealth.smilecdr.support.commands.DLQResponseContainer;
import ca.ontariohealth.smilecdr.support.commands.ReportRecord;
import ca.ontariohealth.smilecdr.support.commands.json.CommandParamAdapter;
import ca.ontariohealth.smilecdr.support.commands.json.InstantAdapter;
import ca.ontariohealth.smilecdr.support.commands.json.ReportRecordAdapter;
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
import org.apache.kafka.common.PartitionInfo;


/**
 * @author shawn.brant
 *
 */
public class DLQWatcher extends BaseApplication 
{
static private final Logger 			        logr      = LoggerFactory.getLogger(DLQWatcher.class);

static private final String				        CLI_INITIAL_CMD_SHRT = "i";
static private final String                     CLI_INITIAL_CMD_LONG = "initCmd";

static private final Long				        KILL_POLLER_MAX_WAIT = 10000L; // milliseconds

private Map<String, Consumer<String,String>>    allConsumers         = new HashMap<>();
private Map<String, Producer<String,String>>    allProducers         = new HashMap<>();

private	Consumer<String, String>		        controlConsumer 	 = null;
private	boolean							        exitWatcher     	 = false;
private	DLQPollingThread				        dlqPoller       	 = null;

private GsonBuilder                             jsonBuilder          = new GsonBuilder();


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
jsonBuilder.registerTypeAdapter( Instant.class,         new InstantAdapter() );
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
			DLQResponseContainer cmdLineResp = processReceivedCommand( initCmd );
			
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
				processReceivedCommand( crnt.value() );
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
if (resp != null)
    {
    Gson    xltrToJSON = jsonBuilder.create();
    String  respAsJSON = xltrToJSON.toJson( resp );
    
    logr.info( "Processed Command resulting in:" );
    logr.info( "\n{}", respAsJSON );
    
    }

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




protected	DLQResponseContainer	processReceivedCommand( String cmd )
{
DLQResponseContainer    resp = null;
if (cmd != null)
	{
	String	 controlCommand = cmd.strip();
	
	if ((controlCommand != null) &&  (controlCommand.length() > 0))
		{
		logr.debug( "Received Control Command: {}", controlCommand );

		String[] args = controlCommand.split( "\s+", 0 );
		resp = processReceivedCommand( args );
		}				
	}
	
	
return resp;	
}



protected	DLQResponseContainer	processReceivedCommand( String[] args )
{
DLQResponseContainer    resp = null;

if ((args != null) && (args.length > 0))
	{
	DLQCommandContainer    cmdToProcess = new DLQCommandContainer();
	DLQCommand             cmdName      = DLQCommand.valueOf( args[0] );

	logr.debug( "Processing received command: '{}' translated to DLQWatcherCommand: '{}'",
			    args[0],
			    cmdName.toString() );
	
	int    ndx = 0;
	for (String crntArg : args)
	    {
	    if (ndx == 0)
	        cmdToProcess.setCommandToIssue( DLQCommand.valueOf( crntArg ) );
	    
	    else
	        {
	        DLQCommandParam    parm = new DLQCommandParam( crntArg, '=' );
	       
	        cmdToProcess.getCommandParams().add( parm );
	        }
	    
	    ndx++;
	    }
	
	resp = processReceivedCommand( cmdToProcess );
	}

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
            resp.addReportEntry( DLQCommand.HELLO.commandStr() );
            break;
            
        case    LIST:
            logr.info("All known {} Commands:", appConfig.getApplicationName().appName() );
            resp.setOutcome( DLQCommandOutcome.SUCCESS );
            
            for (DLQCommand crnt : DLQCommand.values())
                if (crnt != DLQCommand.UNKNOWN)
                    {
                    String  rprtLine = String.format( "%s - %s", crnt.commandStr(), crnt.usageStr() );
                    logr.info( "   {}", rprtLine);
                    resp.addReportEntry( rprtLine );
                    }
                
            break;
            
        case    START:
            logr.info( "Starting the DLQ Poller Thread if it is not already running." );
            startPollingThread();
            break;
                
        case    STOP:
            logr.info( "Stopping the DLQ Poller Thread if it is running." );
            stopPollingThread();
            break;
                
        case    QUIT:
            logr.info( "Triggering Exit of: {}", appConfig.getApplicationName().appName() );
            exitWatcher = true;
            
            resp.setOutcome( DLQCommandOutcome.SUCCESS );
            break;
            
        case    UNKNOWN:
        default:
            logr.error( "Received unknown command: {}", cmd.getCommandToIssue().commandStr() );
        }
    
    resp.recordCompleteTimestamp();
    }


return resp;
}




private void	startPollingThread()
{
logr.debug( "Entering: startPollingThread" );

if (dlqPoller == null)
	{
	logr.debug( "DLQ Poller Thread does not exist: creating it.");
	dlqPoller = new DLQPollingThread( appConfig );
	}

if (dlqPoller.isAlive())
	logr.debug( "DLQ Poller Thread is already alive: nothing to do." );

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
		logr.debug( "DLQ Poller Thread is running." );
	
	else
		logr.error( "DLQ Poller Thread did not start." );
	}

logr.debug( "Exiting: startPollingThread" );
return;	
}




@SuppressWarnings("deprecation")
private void	stopPollingThread()
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
		logr.debug( "Thread is still alive after max-wait. Hard stopping it." );
		dlqPoller.stop();
		}
	
	dlqPoller = null;
	}

else
	logr.debug( "DLQ Poller Thread object does not exist: nothing to do." );

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