/**
 * 
 */
package ca.ontariohealth.smilecdr.support.commands.response;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.json.JSONObject;

import ca.ontariohealth.smilecdr.support.MyInstant;
import ca.ontariohealth.smilecdr.support.config.ConfigProperty;
import ca.ontariohealth.smilecdr.support.config.Configuration;
import ca.ontariohealth.smilecdr.support.kafka.KafkaTopicRecordParser;

/**
 * @author adminuser
 *
 */
public class MHAParkRecordEntry extends MHADLQRecordEntry
{
public MHAParkRecordEntry( ConsumerRecord<String,String> dlqRcrd, Configuration appCfg )
{
super( dlqRcrd, appCfg );
return;
}



public MHAParkRecordEntry( MyInstant dlqEntryTS,
                           String    resourceType,
                           String    resourceID )

{
super( dlqEntryTS, resourceType, resourceID );
return;
}


}
