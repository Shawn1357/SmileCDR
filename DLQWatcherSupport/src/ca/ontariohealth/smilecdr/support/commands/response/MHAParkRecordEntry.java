/**
 * 
 */
package ca.ontariohealth.smilecdr.support.commands.response;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import ca.ontariohealth.smilecdr.support.MyInstant;
import ca.ontariohealth.smilecdr.support.config.Configuration;

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
