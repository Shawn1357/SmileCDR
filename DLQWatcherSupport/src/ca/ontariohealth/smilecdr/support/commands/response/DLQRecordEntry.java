/**
 * 
 */
package ca.ontariohealth.smilecdr.support.commands.response;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.json.JSONObject;

import ca.ontariohealth.smilecdr.support.MyInstant;

/**
 * @author adminuser
 *
 */
public class DLQRecordEntry
{
private MyInstant   entryTimestamp  = null;
private String      subscriptionID  = null;
private String      resourceType    = null;
private String      resourceID      = null;


public DLQRecordEntry()
{
return;
}



public DLQRecordEntry( ConsumerRecord<String,String> dlqRcrd )
{
fromDLQRecord( dlqRcrd );
return;
}



public DLQRecordEntry( MyInstant    entryTime, 
                       String       subscrID, 
                       String       rsrcType, 
                       String       rsrcID )
{
setEntryTimestamp( entryTime );
setSubscriptionID( subscrID );
setResourceType( rsrcType );
setResourceID( rsrcID );

return;
}




public void  fromDLQRecord( ConsumerRecord<String,String> dlqRcrd )
{
entryTimestamp  = null;
subscriptionID  = null;
resourceType    = null;
resourceID      = null;

if (dlqRcrd != null)
    {
    long    ts = dlqRcrd.timestamp();
    if (ts > 0)
        entryTimestamp = new MyInstant( ts );
    
    JSONObject  json    = parseFullPayload( dlqRcrd.value() );
    
    subscriptionID = extractSubscriptionID( json );
    if (json != null)
        {
        subscriptionID = extractSubscriptionID( json );
        resourceType   = extractResourceType( json );
        resourceID     = extractResourceID( json );
        }
    }

return;
}



private String      extractResourceType( JSONObject json )
{
String  rsrcType = null;

if (json != null)
    {
    JSONObject payload      = json.getJSONObject( "payload" );
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
        // Assume Bundle.
        rsrcType = "Bundle";
        }
    }


return rsrcType;
}





private String      extractResourceID( JSONObject json )
{
String  rsrcID = null;

if (json != null)
    {
    JSONObject payload      = json.getJSONObject( "payload" );
    String     bundleID     = null;
    
    if (payload != null)
        bundleID = payload.getString( "payloadId" );
    
    if ((bundleID != null) && (bundleID.length() > 0))
        rsrcID = bundleID.strip();
    }

return rsrcID;
}





private String      extractSubscriptionID( JSONObject json )
{
String  subID = null;

if (json != null)
    {
    JSONObject payload      = json.getJSONObject( "payload" );
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
            subID = rsrcIDParts[1];
        
        else if (rsrcIDParts.length == 1)
            subID = rsrcIDParts[0];
        
        else
            subID = subscrRsrc;
        }
    }

return subID;
}




private JSONObject  parseFullPayload( String payloadStr )
{
JSONObject  json = null;

if ((payloadStr != null) && (payloadStr.length() > 0))
    json = new JSONObject( payloadStr );

return json;
}



public MyInstant getEntryTimestamp()
{
return entryTimestamp;
}



public void setEntryTimestamp( MyInstant entryTimestamp )
{
this.entryTimestamp = entryTimestamp != null ? entryTimestamp : MyInstant.now();
return;
}



public String getSubscriptionID()
{
return subscriptionID;
}



public void setSubscriptionID( String subscriptionID )
{
this.subscriptionID = subscriptionID;
return;
}



public String getResourceType()
{
return resourceType;
}



public void setResourceType( String resourceType )
{
this.resourceType = resourceType;
return;
}



public String getResourceID()
{
return resourceID;
}



public void setResourceID( String resourceID )
{
this.resourceID = resourceID;
return;
}
}
