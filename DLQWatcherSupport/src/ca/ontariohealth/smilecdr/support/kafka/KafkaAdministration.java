/**
 * 
 */
package ca.ontariohealth.smilecdr.support.kafka;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.AlterConfigsResult;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.config.TopicConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ontariohealth.smilecdr.support.config.ConfigProperty;
import ca.ontariohealth.smilecdr.support.config.Configuration;

/**
 * @author adminuser
 *
 */
public class KafkaAdministration
{
private static  final   Logger  logr = LoggerFactory.getLogger( KafkaAdministration.class );

Configuration   appConfig = null;
AdminClient     adminClnt = null;

public KafkaAdministration( Configuration appCfg )
{
if (appCfg == null)
    throw new IllegalArgumentException( "Application Configuration argument must not be null" );

appConfig = appCfg;
createAdminClient();

return;
}



private void createAdminClient()
{
Properties  props = new Properties();

props.put( "bootstrap.servers", appConfig.configValue( ConfigProperty.BOOTSTRAP_SERVERS ) );

adminClnt = AdminClient.create( props );
return;
}


public  AdminClient kafkaAdminClient()
{
return adminClnt;
}



public  boolean topicExists( String topicNm )
{
boolean exists = false;

if ((adminClnt != null) && (topicNm != null) && (topicNm.length() > 0))
    {
    ListTopicsResult    allTopics = adminClnt.listTopics();
    
    try
        {
        exists = allTopics.names().get().contains( topicNm );
        }
    
    catch (InterruptedException | ExecutionException e)
        {
        exists = false;
        }
    }

return exists;
}




public  void    ensureTopicExists( String topicNm )
{
if ((adminClnt != null)     && 
    (topicNm != null)       && 
    (topicNm.length() > 0)  && 
    (!topicExists( topicNm )))
    
    {
    int   partitions        = 1;
    short replicationFactor = 1;
    
    NewTopic topic = new NewTopic( topicNm, partitions, replicationFactor );
    
    CreateTopicsResult rslt = adminClnt.createTopics( Collections.singleton( topic ) );
    
    KafkaFuture<Void>   future = rslt.values().get( topicNm );
    try
        {
        future.get();
        }
    
    catch (InterruptedException | ExecutionException e)
        {
        e.printStackTrace();
        }
    }

return;
}


/**
 * Set the topic element retention period to the supplied number of hours.
 * 
 * @param topicNm           Name of the topic to be modified with a new
 *                          retention period.
 * @param retentionInHours  The number of hours to keep elements in the topic
 *                          before Kafka is free to dispose of them. Must be a
 *                          positive integer value.
 *                          If zero or negative or not an Integer, "FOREVER" is assumed.
 *                          If "FOREVER" (case-insensitive), the topic will be
 *                          configured to never dispose of elements.
 *                          If <code>null</code>, the default retention period
 *                          will be used from the config property:
 *                          ConfigProperty.KAFKA_DEFAULT_TOPIC_RETENTION
 */
public void     setTopicRetentionPeriod( String topicNm, String retentionInHours )
{
if ((topicNm != null) && (topicNm.length() > 0))
    {
    Integer millis = null;
    
    if ((retentionInHours == null) || (retentionInHours.length() == 0))
        retentionInHours = appConfig.configValue( ConfigProperty.KAFKA_DEFAULT_TOPIC_RETENTION );
    
    if ("FOREVER".equalsIgnoreCase(retentionInHours))
        millis = -1;
    
    else
        {
        try
            {
            millis = Integer.valueOf( retentionInHours );
            }
        
        catch (NumberFormatException e)
            {
            millis = -1;
            }
        
        if (millis <= 0)
            millis = -1;
    
        else
            millis *= 60 * 60 * 1000; // Convert hours to milliseconds.
        }

    ConfigResource    cfgRsrc      = new ConfigResource( ConfigResource.Type.TOPIC, topicNm );
    ConfigEntry       retentionCfg = new ConfigEntry( TopicConfig.RETENTION_MS_CONFIG, String.valueOf( millis ) );
    AlterConfigOp     cfgOp        = new AlterConfigOp( retentionCfg, AlterConfigOp.OpType.SET );
    
    Map<ConfigResource, Collection<AlterConfigOp>>  updateCfg = new HashMap<>();
    updateCfg.put( cfgRsrc, Collections.singleton( cfgOp ) );
    
    AlterConfigsResult                    alterCfgRslt = adminClnt.incrementalAlterConfigs( updateCfg );
    Map<ConfigResource,KafkaFuture<Void>> allRslts     = alterCfgRslt.values();
    KafkaFuture<Void>                     cfgRslts     = allRslts.get( cfgRsrc );
   
    try
        {
        cfgRslts.get();
        }
    
    catch (InterruptedException | ExecutionException e)
        {
        e.printStackTrace();
        }
    
    logr.debug( "Alter Retention Period for Topic: {} with retention: {}", topicNm, retentionInHours );
    logr.debug( "   Completed:       {}", cfgRslts.isDone() );
    logr.debug( "   Was Cancelled:   {}", cfgRslts.isCancelled() );
    logr.debug( "   With Exceptions: {}", cfgRslts.isCompletedExceptionally() );
    }


return;
}
}
