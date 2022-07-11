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
public class CWMParkRecordEntry extends CWMDLQRecordEntry
{
private static String[]     CSV_HEADERS = { "KafkaOffset",
                                            "SubscriptionID",
                                            "ResourceType",
                                            "ResourceID",
                                            "ParkingLotEntryEpochMillis",
                                            "ParkingLotEntryLocalTimeStamp",
                                            "DurationOnParkingLot"
                                          };


public CWMParkRecordEntry( ConsumerRecord<String,String> dlqRcrd, Configuration appCfg )
{
super( dlqRcrd, appCfg );
return;
}



public CWMParkRecordEntry( MyInstant dlqEntryTS,
                           String    subscriptionID,
                           String    resourceType,
                           String    resourceID )

{
super( dlqEntryTS, subscriptionID, resourceType, resourceID );
return;
}




public static String[]  csvColHeaders()
{
return CWMParkRecordEntry.CSV_HEADERS;
}



@Override
public String[] csvColumnHeaders()
{
return CWMParkRecordEntry.csvColHeaders();
}



}
