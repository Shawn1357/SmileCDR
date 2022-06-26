/**
 * 
 */
package ca.ontariohealth.smilecdr.dlqwatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ontariohealth.smilecdr.support.config.ConfigProperty;
import ca.ontariohealth.smilecdr.support.config.Configuration;
import ca.ontariohealth.smilecdr.support.email.EMailNotificationType;


/**
 * @author adminuser
 *
 */
public class DLQPollingThread extends TopicNewEntriesPollingThread 
{
private static final 	Logger			logr 				= LoggerFactory.getLogger( DLQPollingThread.class );


public	DLQPollingThread( Configuration appCfg )
{
super( appCfg );
	
return;
}


@Override
public String kafkaTopicToWatch()
{
String  kafkaTopic = appConfig().configValue( ConfigProperty.KAFKA_DLQ_TOPIC_NAME );

logr.debug( "Kafka Topic to Watch for New Entries: {}", kafkaTopic );
return kafkaTopic;
}


@Override
public String kafkaConsumerGroupName()
{
String consumerGroup = appConfig().configValue( ConfigProperty.KAFKA_DLQ_GROUP_ID,
                                                appConfig().getApplicationName().appName() + ".dlq.group.id" );

logr.debug( "Kafka Consumer Group for new Entries: {}", consumerGroup );
return consumerGroup;
}


@Override
public EMailNotificationType emailTypeToSend()
{
return EMailNotificationType.NEW_DLQ_ENTRIES;
}



}
