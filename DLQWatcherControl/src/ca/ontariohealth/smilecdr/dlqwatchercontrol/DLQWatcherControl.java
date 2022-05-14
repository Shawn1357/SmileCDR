/**
 * 
 */
package ca.ontariohealth.smilecdr.dlqwatchercontrol;

import java.util.Properties;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;

import ca.ontariohealth.smilecdr.BaseApplication;
import ca.ontariohealth.smilecdr.support.config.ConfigProperty;


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

private 				String				kafkaTopicName  = null;
private					String				commandToSend   = null;

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
/*
 * Grab the command line options specific to this application.
 * 
 */
	
commandToSend = cmdLine.getOptionValue( CLI_OPERTN_LONG );

if (cmdLine.hasOption(CLI_TOPIC_LONG))
	kafkaTopicName = cmdLine.getOptionValue( CLI_TOPIC_LONG );

else
	kafkaTopicName = appConfig.configValue( ConfigProperty.DEFAULT_CONTROL_TOPIC_NAME );

return;	
}



private Producer<Long, String> createProducer()
{
Producer<Long, String>	rtrn = null;

logr.debug( "Entering: createProducer" );

Properties	props = new Properties();

props.put( ProducerConfig.CLIENT_ID_CONFIG,             	appConfig.getApplicationName().toString() );
props.put( ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,     	appConfig.configValue( ConfigProperty.BOOTSTRAP_SERVERS ) );
props.put( ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,  	appConfig.configValue( ConfigProperty.KEY_SERIALIZER ) );
props.put( ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,	appConfig.configValue( ConfigProperty.VALUE_SERIALIZER ) );

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
