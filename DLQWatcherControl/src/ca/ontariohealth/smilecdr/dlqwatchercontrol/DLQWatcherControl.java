/**
 * 
 */
package ca.ontariohealth.smilecdr.dlqwatchercontrol;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import ca.ontariohealth.smilecdr.BaseApplication;
import ca.ontariohealth.smilecdr.support.config.ConfigProperty;
import ca.ontariohealth.smilecdr.support.config.Configuration;


/**
 * @author adminuser
 *
 */
public class DLQWatcherControl extends BaseApplication
{
static final Logger 						logr      		= LoggerFactory.getLogger(DLQWatcherControl.class);

protected static final	String 				CLI_TOPIC_SHRT  = "t";
protected static final	String 				CLI_TOPIC_LONG  = "topicNm";

protected static final  String				CLI_OPERTN_SHRT = "o";
protected static final  String              CLI_OPERTN_LONG = "operation";

public static void main( String[] args )
{
logr.debug( "Entering: main" );

DLQWatcherControl	watcherCtrl = new DLQWatcherControl();
watcherCtrl.launch(args);

logr.debug( "Exiting: main");
return;
}



@Override
protected void launch() 
{
logr.debug("Entering: DLQWatcherControl.launch" );

/*
 * Grab the command line options specific to this application.
 * 
 */
	
//String commandToSend = cmdLine.getOptionValue( CLI_OPERTN_LONG );
String[] cmdsToSend = cmdLine.getOptionValues( CLI_OPERTN_LONG );


String kafkaTopicName = null;
if (cmdLine.hasOption(CLI_TOPIC_LONG))
	kafkaTopicName = cmdLine.getOptionValue( CLI_TOPIC_LONG );

else
	kafkaTopicName = appConfig.configValue( ConfigProperty.DEFAULT_CONTROL_TOPIC_NAME );

if (cmdsToSend != null)
	for (String crntCmd : cmdsToSend)
		if ((crntCmd != null) && (crntCmd.length() > 0))
			sendCommand( kafkaTopicName, crntCmd );

logr.debug("Exiting: DLQWatcherControl.launch" );
return;	
}



private void sendCommand( final String kafkaTopicName, 
		                  final String commandToSend )
{
logr.debug( "Entering: sendCommand" );
logr.debug(  "   Topic Name: {}", kafkaTopicName );
logr.debug(  "   Command:    {}", commandToSend );
final Producer<String, String> prdcr    = createProducer();
long                           crntTime = System.currentTimeMillis();

final ProducerRecord<String, String> record = new ProducerRecord<>( kafkaTopicName,
		                                       					    String.valueOf( crntTime ),
		                                       					    commandToSend );
try 
	{
	logr.debug( "Sending Command to Kafka Topic" );
	RecordMetadata	metadata = prdcr.send( record ).get();
	
	prdcr.close();
	
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

catch (InterruptedException e)
	{
	logr.error("Interupted Exception while sending to Kafka:", e );
	} 

catch (ExecutionException e)
	{
	logr.error( "Execution Exception while sending to Kafka:", e );
	}

logr.debug( "Exiting: sendCommand" );
return;
}




private Producer<String, String> createProducer()
{
Producer<String, String>	rtrn = null;

logr.debug( "Entering: createProducer" );

Properties	props = new Properties();

String		clientID 		 = appConfig.getApplicationName().appName();
String		bootstrapServers = appConfig.configValue( ConfigProperty.BOOTSTRAP_SERVERS );
String      keySerializer    = Configuration.KAFKA_KEY_SERIALIZER_CLASS_NAME;
String      valueSerializer  = Configuration.KAFKA_VALUE_SERIALIZER_CLASS_NAME;

logr.debug( "   Client ID:         {}", clientID );
logr.debug( "   Bootstrap Servers: {}", bootstrapServers );
logr.debug( "   Key Serializer:    {}", keySerializer );
logr.debug( "   Value Serializer:  {}", valueSerializer );

props.put( ProducerConfig.CLIENT_ID_CONFIG,             	clientID );
props.put( ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,     	bootstrapServers );
props.put( ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,  	keySerializer );
props.put( ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,	valueSerializer );

rtrn = new KafkaProducer<>( props );

logr.debug( "Exiting: createProducer" );;
return rtrn;
}





@Override
protected void createCLIOptions( Options cmdLineOpts ) 
{
super.createCLIOptions( cmdLineOpts );

Option	topic = new Option( CLI_TOPIC_SHRT,  CLI_TOPIC_LONG,  true, "The Kafka Topic Name to receive the command." );
Option  oprtn = new Option( CLI_OPERTN_SHRT, CLI_OPERTN_LONG, true, "The operation to be loaded into the Kafka Topic" );

oprtn.setRequired( true );

cmdLineOpts.addOption( topic ).addOption( oprtn );

return;
}



}
