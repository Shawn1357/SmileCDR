/**
 * 
 */
package ca.ontariohealth.smilecdr.support.config;

import java.util.HashMap;
import java.util.Map;

import ca.ontariohealth.smilecdr.support.NormalizedStringKey;

/**
 * Records the name of the instance of the application that is running.
 * For instance, if the DLQ Watcher were running two instances: to watch the 
 * KAFKA.DLQ queue and another instance to watch the Parking Lot Queue, then
 * this Instance Name class can be used to differentiate configuration items
 * specific to each.
 * 
 * @author shawn.brant
 *
 */
public class InstanceName 
{
/**
 * The name of the instance as it was originally supplied to this class instance.
 * 
 */
private NormalizedStringKey		instName;


/**
 * Maintains the list of all defined application name instances so that exactly one
 * can be exist at one time for a specific process.
 * 
 */
private static Map<NormalizedStringKey, InstanceName>	allInstances = new HashMap<>();


/**
 * A factory to produce or return the already created InstanceName instance
 * corresponding to the supplied name.  If there is already an InstanceName
 * instance that matches, this routine will return that instance otherwise
 * a new instance is created.
 * <p>
 * To determine if an Instance Name already exists, the supplied name is
 * normalized into a consistent format and that is what is compared.
 * 
 * @param instNm The name of the instance to be returned and/or created.
 *               If <code>null</code>, this parameter is interpreted as "".
 * @return       The Instance Name instance corresponding to the name.
 * 
 */
public static InstanceName getInstanceName( final String instNm )
{
InstanceName rtrn = null;

NormalizedStringKey newInstNm = new NormalizedStringKey( instNm );

if (allInstances.containsKey( newInstNm ))
	{
	rtrn = allInstances.get( newInstNm );
	}

else
	{
	rtrn = new InstanceName( newInstNm );
	allInstances.put( newInstNm, rtrn );
	}


return rtrn; 
}



/**
 * Constructs a new instance name instance.
 * 
 * @param instNm
 * 
 */
private InstanceName( NormalizedStringKey instNm )
{
if (instNm == null)
	throw new IllegalArgumentException( "Application Name parameter must not be null." );

instName = instNm;

return;
}



public	final String	instName()
{
return instName.getSupplied();
}



public	final String	nrmlzdInstName()
{
return instName.getNormalized();
}
}
