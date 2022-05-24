/**
 * 
 */
package ca.ontariohealth.smilecdr.dlqwatcher;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ontariohealth.smilecdr.BaseApplication;
import ca.ontariohealth.smilecdr.support.config.ConfigProperty;
import ca.ontariohealth.smilecdr.support.config.Configuration;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;


/**
 * @author shawn.brant
 *
 */
public class DLQWatcher extends BaseApplication 
{
static private final Logger 			logr      = LoggerFactory.getLogger(DLQWatcher.class);

static private final String				CLI_INITIAL_CMD_SHRT = "i";
static private final String             CLI_INITIAL_CMD_LONG = "initCmd";

static private final Long				KILL_POLLER_MAX_WAIT = 10000L; // milliseconds

private	Consumer<String, String>		controlConsumer 	 = null;
private	boolean							exitWatcher     	 = false;
private	DLQPollingThread				dlqPoller       	 = null;


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

/*
 * Create the consume that will be waiting for commands to appear on the
 * Control Kafka topic.
 * 
 */

String	controlTopic = appConfig.configValue( ConfigProperty.DEFAULT_CONTROL_TOPIC_NAME );

controlConsumer = createConsumer();
controlConsumer.subscribe( Collections.singletonList( controlTopic ) );

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
 * Check for and process the inital command.
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
			processReceivedCommand( initCmd );
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



protected	void	processReceivedCommand( String cmd )
{
if (cmd != null)
	{
	String	 controlCommand = cmd.strip();
	
	if ((controlCommand != null) &&  (controlCommand.length() > 0))
		{
		logr.debug( "Received Control Command: {}", controlCommand );

		String[] args = controlCommand.split( "\s+", 0 );
		processReceivedCommand( args );
		}				
	}
	
	
return;	
}



protected	void	processReceivedCommand( String[] args )
{
if ((args != null) && (args.length > 0))
	{
	DLQWatcherCommand cmd = DLQWatcherCommand.getCommand( args[0] );
	
	logr.debug( "Processing received command: '{}' translated to DLQWatcherCommand: '{}'",
			    args[0],
			    cmd.toString() );
	
	
	switch (cmd)
		{
		case	HELLO:
			// Nothing to do.  Already acknowledged in the logs.
			break;
			
		case	LIST:
			logr.info("All known {} Commands:", appConfig.getApplicationName().appName() );
			for (DLQWatcherCommand crnt : DLQWatcherCommand.values())
				if (crnt != DLQWatcherCommand.UNKNOWN)
					logr.info( "   {} - {}", crnt.commandStr(), crnt.usageStr() );
				
			break;
			
		case	START:
			logr.info( "Starting the DLQ Poller Thread if it is not already running." );
			startPollingThread();
			break;
				
		case	STOP:
			logr.info( "Stopping the DLQ Poller Thread if it is running." );
			stopPollingThread();
			break;
				
		case	QUIT:
			logr.info( "Triggering Exit of: {}", appConfig.getApplicationName().appName() );
			exitWatcher = true;
			break;
			
		case	UNKNOWN:
		default:
			logr.error( "Received unknown command: {}", args[0] );
		}
	}

return;	
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




private Consumer<String, String>	createConsumer()
{
Consumer<String, String>  rtrn  = null;
Properties				props = new Properties();

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
for (DLQWatcherCommand crnt : DLQWatcherCommand.values())
	if (crnt != DLQWatcherCommand.UNKNOWN)
		{
		String	cmdHelp = "   " + crnt.commandStr() + " - " + crnt.usageStr();
		System.out.println( cmdHelp );
		}
System.out.println( "" );
return;
}
}