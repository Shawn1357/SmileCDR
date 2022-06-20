/**
 * 
 */
package ca.ontariohealth.smilecdr.support.kafka;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ontariohealth.smilecdr.support.config.ConfigProperty;
import ca.ontariohealth.smilecdr.support.config.Configuration;

/**
 * @author adminuser
 *
 */
public class KafkaProducerHelper
{
private static final    Logger                                  logr         = LoggerFactory.getLogger( KafkaProducerHelper.class );
private static          Map<String, Producer<String,String>>    allProducers = new HashMap<>();


private KafkaProducerHelper()
{
// Private to keep this class static only.
return;
}



public static Producer<String, String> createProducer( Configuration appConfig,
                                                       String        topicName )
{
return KafkaProducerHelper.createProducer( appConfig,  topicName, null );
}



public static Producer<String, String> createProducer( Configuration appConfig, 
                                                       String        topicName,
                                                       Properties    kafkaProps )
{
Producer<String, String>    producer = null;

logr.debug( "Entering: createProducer" );

if (appConfig == null)
    throw new IllegalArgumentException( "Applciation Configuration parameter must not be null" );

else if ((topicName == null) || (topicName.length() == 0))
    throw new IllegalArgumentException( "Producer Topic Name must not be null or zero-length." );

else
    {
    producer  = allProducers.get( topicName );

    if (producer == null)
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
        
        if (kafkaProps != null)
            props.putAll( kafkaProps );
        
        producer = new KafkaProducer<>( props );
        
        allProducers.put( topicName, producer );
        }
    }

logr.debug( "Exiting: createProducer" );;
return producer;
}

}
