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
public class CWMDLQRecordEntry extends KafkaTopicRecordParser
{
String  subscrID    = null;

private static String[]     CSV_HEADERS = { "SubscriptionID",
                                            "ResourceType",
                                            "ResourceID",
                                            "DLQEntryEpochMillis",
                                            "DLQEntryLocalTimeStamp",
                                            "DurationOnDLQ"
                                          };


public CWMDLQRecordEntry( ConsumerRecord<String,String> dlqRcrd, Configuration appCfg )
{
super( dlqRcrd, appCfg );
fromDLQRecord( dlqRcrd, appCfg );

return;
}



public CWMDLQRecordEntry( MyInstant dlqEntryTS,
                          String    subscriptionID,
                          String    resourceType,
                          String    resourceID )

{
super( dlqEntryTS, resourceType, resourceID );
subscrID = subscriptionID;

return;
}


protected void  fromDLQRecord( ConsumerRecord<String,String> dlqRcrd, Configuration appCfg )
{
super.fromDLQRecord( dlqRcrd, appCfg );

extractSubscriptionID();

return;
}



protected void      extractResourceType()
{
rsrcType = null;

if (parsedJSON != null)
    {
    JSONObject payload      = parsedJSON.getJSONObject( "payload" );
    String     dlqString    = null;
    JSONObject dlqPayload   = null;
    String     rsrcTypeStr  = null;
    
    if (payload != null)
        {
        dlqString = payload.getString( "payload" );
        }
    
    if ((dlqString != null) && (dlqString.length() > 0))
        {
        dlqPayload = new JSONObject( dlqString );
        }
    
    if (dlqPayload != null)
        {
        rsrcTypeStr = dlqPayload.getString( "resourceType" ); 
        }
    
    if (rsrcTypeStr != null)
        {
        rsrcType = rsrcTypeStr;
        }
    
    else
        {
        // If all else fails, assume Bundle.
        rsrcType = "Bundle";
        }
    }


return;
}





@Override
protected void     extractResourceID()
{
rsrcID = null;
if (parsedJSON != null)
    {
    JSONObject payload      = parsedJSON.getJSONObject( "payload" );
    String     bundleID     = null;
    
    if (payload != null)
        bundleID = payload.getString( "payloadId" );
    
    if ((bundleID != null) && (bundleID.length() > 0))
        rsrcID = bundleID.strip();
    }

return;
}





@Override
protected void      extractSubscriptionID()
{
subscrID = null;
if (parsedJSON != null)
    {
    JSONObject payload      = parsedJSON.getJSONObject( "payload" );
    JSONObject subscr       = null;
    String     subscrRsrc   = null;
    
    if (payload != null)
        subscr = payload.getJSONObject( "canonicalSubscription" );
    
    if (subscr != null)
        subscrRsrc = subscr.getString( "id" );
    
    if ((subscrRsrc != null) && (subscrRsrc.length() > 0))
        {
        String[] rsrcIDParts = subscrRsrc.split( "/" );
        
        if (rsrcIDParts.length > 1)
            subscrID = rsrcIDParts[1];
        
        else if (rsrcIDParts.length == 1)
            subscrID = rsrcIDParts[0];
        
        else
            subscrID = subscrRsrc;
        }
    }

return;
}



//@Override
public  String      subscriptionID()
{
return subscrID;
}






@Override
public  MyInstant               topicEntryTimestamp()
{
return topicEntryTS;
}


@Override
public  MyInstant               dlqEntryTimestamp()
{
//Because this record came from the DLQ Topic, the topic timestamp and the
//DLQ Entry timestamp will be the same.
return topicEntryTS;
}



@Override
protected void extractDLQEntryTimestamp()
{
// Nothing to do, the topic entry timestamp and the DLQ entry timestamp are one
// and the same.
dlqEntryTS = topicEntryTimestamp();

return;
}



@Override
public String[] csvColumnHeaders()
{
return CWMDLQRecordEntry.CSV_HEADERS;
}



@Override
public String[] csvColumnValues()
{
String              dtTmFmt         = appConfig.configValue( ConfigProperty.TIMESTAMP_FORMAT );
DateTimeFormatter   frmtr           = DateTimeFormatter.ofPattern( dtTmFmt ).withZone( ZoneId.systemDefault() );
String              lclTimeStamp    = frmtr.format( dlqEntryTimestamp().asInstant() );

String[]            values          = new String[] { subscriptionID(),
                                                     resourceType(),
                                                     resourceID(),
                                                     String.valueOf( dlqEntryTimestamp().getEpochMillis() ),
                                                     lclTimeStamp,
                                                     elapsedTimeInTopic()
                                                   };

return values;
}

}
