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
private static String[]     CSV_HEADERS = { "KafkaOffset",
                                            "ResourceType",
                                            "ResourceID",
                                            "ParkingLotEntryEpochMillis",
                                            "ParkingLotEntryLocalTimeStamp",
                                            "DurationOnParkingLot"
                                          };


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



public static String[] csvColHeaders()
{
return MHAParkRecordEntry.CSV_HEADERS;
}



@Override
public String[] csvColumnHeaders()
{
return MHAParkRecordEntry.csvColHeaders();
}



}
