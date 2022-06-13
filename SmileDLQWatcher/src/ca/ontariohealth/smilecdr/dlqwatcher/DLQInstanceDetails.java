/**
 * 
 */
package ca.ontariohealth.smilecdr.dlqwatcher;

import java.time.Instant;

import ca.ontariohealth.smilecdr.support.MyInstant;

/**
 * @author adminuser
 *
 */
public class DLQInstanceDetails 
{
private MyInstant   rcrdInstant = null;
private	String	    subID		= null;
private	String	    resType		= null;
private String	    resID		= null;

public	DLQInstanceDetails( MyInstant   rcrdTimestamp,
                            String      subscriptionID,
		                    String      failedResourceType,
		                    String      failedResourceID )
{
if ((subscriptionID     		 == null) 	||
	(failedResourceType 		 == null)	||
	(failedResourceID   		 == null)	||
	(subscriptionID.length() 	 == 0)		||
	(failedResourceType.length() == 0)		||
	(failedResourceID.length()   == 0))
	
	throw new IllegalArgumentException( "No constructor arguments may be null or zero-length." );

rcrdInstant = rcrdTimestamp;
subID 	    = subscriptionID;
resType     = failedResourceType;
resID       = failedResourceID;

return;
}


public MyInstant  rcrdInstant()
{
return rcrdInstant;
}


public String subscriptionID()
{
return subID;
}



public String	failedResourceType()
{
return resType;
}



public String	failedResourceID()
{
return resID;
}


public String[]	instanceDetails()
{
String[]	details = { String.valueOf( rcrdInstant.getEpochMillis() ), subID, resType, resID };

return details;
}


public String asCSV()
{
return subID + ", " + resType + ", " + resID;
}
}
