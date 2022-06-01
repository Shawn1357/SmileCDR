/**
 * 
 */
package ca.ontariohealth.smilecdr.support.kafka;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerPartitionAssignor;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.internals.ConsumerCoordinator;
import org.apache.kafka.clients.consumer.internals.ConsumerInterceptors;
import org.apache.kafka.clients.consumer.internals.ConsumerMetadata;
import org.apache.kafka.clients.consumer.internals.ConsumerNetworkClient;
import org.apache.kafka.clients.consumer.internals.Fetcher;
import org.apache.kafka.clients.consumer.internals.SubscriptionState;
import org.apache.kafka.common.metrics.Metrics;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.utils.LogContext;
import org.apache.kafka.common.utils.Time;
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




public static Consumer<String,String>     createConsumer( Configuration appConfig, String topicName )
{
Consumer<String,String> consumer = null;

logr.debug( "Entering: createConsumer" );

if (appConfig == null)
    throw new IllegalArgumentException( "Applciation Configuration parameter must not be null" );

else if ((topicName == null) || (topicName.length() == 0))
    throw new IllegalArgumentException( "Producer Topic Name must not be null or zero-length." );

else
    {
    consumer  = allConsumers.get( topicName );

    if (consumer == null)
        {
        Properties  props               = new Properties();
        
        String      groupID             = appConfig.configValue( ConfigProperty.KAFKA_CONTROL_GROUP_ID,
                                                                 appConfig.getApplicationName().appName() + ".control.group.id" );
        
        String      bootstrapServers    = appConfig.configValue( ConfigProperty.BOOTSTRAP_SERVERS );
        String      keyDeserializer     = Configuration.KAFKA_KEY_DESERIALIZER_CLASS_NAME;
        String      valueDeserializer   = Configuration.KAFKA_VALUE_DESERIALIZER_CLASS_NAME;
        
        logr.debug( "   Group ID:           {}", groupID );
        logr.debug( "   Bootstrap Servers:  {}", bootstrapServers );
        logr.debug( "   Key Deserializer:   {}", keyDeserializer );
        logr.debug( "   Value Deserializer: {}", valueDeserializer );
        
        props.put( ConsumerConfig.GROUP_ID_CONFIG,                 groupID );
        props.put( ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,        bootstrapServers );
        props.put( ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,   keyDeserializer );
        props.put( ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer );
        
        consumer = new KafkaConsumer<>( props );
        consumer.subscribe( Collections.singletonList( topicName ) );
        
        allConsumers.put( topicName,  consumer );
        }
    
    }

logr.debug( "Exiting: createConsumer" );
return consumer;
}

}
