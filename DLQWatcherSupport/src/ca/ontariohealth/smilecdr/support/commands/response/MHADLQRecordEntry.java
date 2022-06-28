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
public class MHADLQRecordEntry extends KafkaTopicRecordParser
{
private static String[]     CSV_HEADERS = { "ResourceType",
                                            "ResourceID",
                                            "DLQEntryEpochMillis",
                                            "DLQEntryLocalTimeStamp",
                                            "DurationOnDLQ"
                                          };


public MHADLQRecordEntry( ConsumerRecord<String,String> dlqRcrd, 
                          Configuration                 appCfg )
{
super( dlqRcrd, appCfg );
fromDLQRecord( dlqRcrd, appCfg );

return;
}



public MHADLQRecordEntry( MyInstant dlqEntryTS,
                          String    resourceType,
                          String    resourceID )

{
super( dlqEntryTS, resourceType, resourceID );
return;
}


protected void  fromDLQRecord( ConsumerRecord<String,String> dlqRcrd, Configuration appCfg )
{
super.fromDLQRecord( dlqRcrd, appCfg );
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
    	{
    	String[] bundleIDParts = bundleID.strip().split("/");
    	
    	switch (bundleIDParts.length)
    		{
    		case 0:
    			rsrcID = bundleID;
    			break;
    			
    		case 1:
    			rsrcID = bundleIDParts[0];
    			break;
    			
    		default:
    			rsrcID = bundleIDParts[1];
    			break;
    		}
    	}
    }

return;
}



@Override
protected void extractSubscriptionID()
{
// Nothing to do. MHA does not have a Subscription ID.
return;
}





//@Override
public  String      subscriptionID()
{
return "n/a";
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
return MHADLQRecordEntry.CSV_HEADERS;
}



@Override
public String[] csvColumnValues()
{
String              dtTmFmt         = appConfig.configValue( ConfigProperty.TIMESTAMP_FORMAT );
DateTimeFormatter   frmtr           = DateTimeFormatter.ofPattern( dtTmFmt ).withZone( ZoneId.systemDefault() );
String              lclTimeStamp    = frmtr.format( dlqEntryTimestamp().asInstant() );

String[]            values          = new String[] { resourceType(),
                                                     resourceID(),
                                                     String.valueOf( dlqEntryTimestamp().getEpochMillis() ),
                                                     lclTimeStamp,
                                                     elapsedTimeInTopic()
                                                   };

return values;
}
}
