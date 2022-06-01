/**
 * 
 */
package ca.ontariohealth.smilecdr.support.commands.response;

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
