/**
 * 
 */
package ca.ontariohealth.smilecdr.support.commands.response;

import java.time.Instant;

/**
 * @author adminuser
 *
 */
public class DLQRecordEntry
{
private Instant     entryTimestamp  = null;
private String      subscriptionID  = null;
private String      resourceType    = null;
private String      resourceID      = null;


public DLQRecordEntry()
{
return;
}



public DLQRecordEntry( Instant entryTime, 
                       String  subscrID, 
                       String  rsrcType, 
                       String  rsrcID )
{
setEntryTimestamp( entryTime );
setSubscriptionID( subscrID );
setResourceType( rsrcType );
setResourceID( rsrcID );

return;
}



public Instant getEntryTimestamp()
{
return entryTimestamp;
}



public void setEntryTimestamp( Instant entryTimestamp )
{
this.entryTimestamp = entryTimestamp != null ? entryTimestamp : Instant.now();
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
