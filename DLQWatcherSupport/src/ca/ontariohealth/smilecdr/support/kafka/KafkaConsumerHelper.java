/**
 * 
 */
package ca.ontariohealth.smilecdr.support.kafka;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ontariohealth.smilecdr.support.config.ConfigProperty;
import ca.ontariohealth.smilecdr.support.config.Configuration;

/**
 * Kafka 
 * @author adminuser
 *
 */
public class KafkaConsumerHelper
{
private static final    Logger                                  logr         = LoggerFactory.getLogger( KafkaConsumerHelper.class );
private static          Map<String, Consumer<String,String>>    allConsumers = new HashMap<>();


private KafkaConsumerHelper()
{
// Private to keep this class static only.
return;
}




public static Consumer<String,String>     createConsumer( Configuration appConfig,
                                                          String        groupID,
                                                          String        topicName )

{
return createConsumer( appConfig, groupID, topicName, null );
}




public static Consumer<String,String>     createConsumer( Configuration appConfig, 
                                                          String        groupID,
                                                          String        topicName,
                                                          Properties    kafkaProps )
{
Consumer<String,String> consumer = null;

logr.debug( "Entering: createConsumer" );

if (appConfig == null)
    throw new IllegalArgumentException( "Applciation Configuration parameter must not be null" );

else if ((groupID == null) || (groupID.length() == 0))
    throw new IllegalArgumentException( "Consumer Group Name must not be null or zero-length." );

else if ((topicName == null) || (topicName.length() == 0))
    throw new IllegalArgumentException( "Consumer Topic Name must not be null or zero-length." );

else
    {
    String  consumerKey = groupID.concat( "--" ).concat( topicName );
    
    consumer  = allConsumers.get( consumerKey );

    if (consumer == null)
        {
        Properties  props               = new Properties();
        
/*
        String      groupID             = appConfig.configValue( ConfigProperty.KAFKA_CONTROL_GROUP_ID,
                                                                 appConfig.getApplicationName().appName() + ".control.group.id" );
*/        
        String      bootstrapServers    = appConfig.configValue( ConfigProperty.BOOTSTRAP_SERVERS );
        String      keyDeserializer     = Configuration.KAFKA_KEY_DESERIALIZER_CLASS_NAME;
        String      valueDeserializer   = Configuration.KAFKA_VALUE_DESERIALIZER_CLASS_NAME;
        
        logr.debug( "Defining standard consumer properties..." );
        props.put( ConsumerConfig.GROUP_ID_CONFIG,                 groupID );
        props.put( ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,        bootstrapServers );
        props.put( ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,   keyDeserializer );
        props.put( ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer );
        
        if (kafkaProps != null)
            {
            logr.debug( "Adding caller provided properties..," );
            props.putAll( kafkaProps );
            }
        
        logr.debug( "Consumer Property Listing:" );
        for (Object propKey : props.keySet())
            {
            String  key = (String) propKey;
            if (propKey != null)
                {
                String  propVal = props.getProperty(key);
                logr.debug( "   {}: {}", propKey, propVal );
                }
            }
            
        consumer = new KafkaConsumer<>( props );
        
        logr.debug( "Subscribing to Topic: {}", topicName );
        consumer.subscribe( Collections.singletonList( topicName ) );
        
        allConsumers.put( consumerKey,  consumer );
        }
    }

logr.debug( "Exiting: createConsumer" );
return consumer;
}

}
