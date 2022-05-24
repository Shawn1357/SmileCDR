/**
 * 
 */
package ca.ontariohealth.smilecdr.dlqretrigger;

import java.util.Properties;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ontariohealth.smilecdr.BaseApplication;
import ca.ontariohealth.smilecdr.support.config.ConfigProperty;
import ca.ontariohealth.smilecdr.support.config.Configuration;

/**
 * @author adminuser
 *
 */
public class DLQRetrigger extends BaseApplication 
{
static private final Logger 			logr      = LoggerFactory.getLogger(DLQRetrigger.class);

static private final String				CLI_MAX_DLQ_COUNT_SHRT = "m";
static private final String             CLI_MAX_DLQ_COUNT_LONG = "maxCount";


private				 Integer			maxDLQCount 		= null;
private				 boolean			displayCmdLineUsage	= false;

/**
 * @param args
 */
public static void main(String[] args)
{
logr.debug( "Entering: main" );

DLQRetrigger	retrigger = new DLQRetrigger();
retrigger.launch(args);

logr.debug( "Exiting: main");
return;
}


/*
 * This is the main launch point for the application.
 * 
 */
@Override
protected void launch() 
{
logr.debug( "Entering: {}.launch", DLQRetrigger.class.getSimpleName() );

boolean	continueProcessing  = validateCommandLine();


if (continueProcessing)
	{
	/*
	 * Create a Kafka Consumer to process a potential maximum number of
	 * bundles that may be residing in teh Dead Letter Queue.
	 * 
	 * We will make a single poll of the Kafka Broker and pull down the
	 * desired records and send individuals.
	 * 
	 */
	}

if (!displayCmdLineUsage)
	displayCmdLineUsage = cmdLineUsageRequested();


if (displayCmdLineUsage)
	displayUsage();


logr.debug( "Exiting: {}.launch", DLQRetrigger.class.getSimpleName() );
return;
}



private Consumer<String, String>	createConsumer()
{
Consumer<String, String>  rtrn  = null;
Properties				  props = new Properties();

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

if ((maxDLQCount != null) && (maxDLQCount > 0))
	{
	props.put( ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxDLQCount );
	}

rtrn = new KafkaConsumer<>( props );

return rtrn;	
}




private boolean validateCommandLine()
{
logr.debug( "Entering: validateCommandLine" );
boolean	isValid = true;

if (cmdLine.hasOption( CLI_MAX_DLQ_COUNT_LONG ))
	{
	String	maxCntStr = cmdLine.getOptionValue( CLI_MAX_DLQ_COUNT_LONG );
	
	if ((maxCntStr != null) && (maxCntStr.length() > 0))
		{
		try
			{
			maxDLQCount = Integer.valueOf( maxCntStr );
			logr.debug( "Retrieved Max DLQ Count from command line: {}", maxDLQCount );
			
			if (maxDLQCount < 0)
				{
				logr.error( "Max DLQ Bundle Count command line parameter must not be negative.");
				isValid = false;
				}
			}
		
		catch (NumberFormatException nfe)
			{
			logr.error( "Unable to interpret the Max DLQ Bundle Count command line parametrer as a non-negative integer: '{}'", maxCntStr );
			isValid = false;
			}
		}
	
	else
		{
		isValid = false;
		logr.error( "Max DLQ Bundle Count must be a non-negative integer." );
		}
	}

if (!displayCmdLineUsage)
	displayCmdLineUsage = !isValid;


logr.debug( "Exiting: validateCommandLine" );
return isValid;
}



@Override
protected void createCLIOptions( Options cmdLineOpts ) 
{
super.createCLIOptions( cmdLineOpts );

Option	initCmd = new Option( CLI_MAX_DLQ_COUNT_SHRT,  CLI_MAX_DLQ_COUNT_LONG,  true, "Max number of bundles to re-send from DLQ if more are available. Default is All bundles." );

cmdLineOpts.addOption( initCmd );

return;
}





}
