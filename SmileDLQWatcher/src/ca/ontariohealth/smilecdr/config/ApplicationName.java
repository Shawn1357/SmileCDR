/**
 * 
 */
package ca.ontariohealth.smilecdr.config;

import java.util.HashMap;
import java.util.Map;

import ca.ontariohealth.cwm.support.NormalizedStringKey;

/**
 * Records the application name that is running.  Typically this would
 * be "DLQWatcher", "DLQRetrigger" or anything you need and is a short word 
 *  description of that application.
 * 
 * @author shawn.brant
 *
 */
public class ApplicationName 
{
/**
 * The name of the application as it was originally supplied to this class instance.
 * 
 */
private NormalizedStringKey		appName;


/**
 * Maintains the list of all defined application name instances so that exactly one
 * can be exist at one time for a specific process.
 * 
 */
private static Map<NormalizedStringKey, ApplicationName>	allEnvs = new HashMap<>();


/**
 * A factory to produce or return the already created ApplicationName instance
 * corresponding to the supplied name.  If there is already an ApplicationName
 * instance that matches, this routine will return that instance otherwise
 * a new instance is created.
 * <p>
 * To determine if an Application Name already exists, the supplied name is
 * normalized into a consistent format and that is what is compared.
 * 
 * @param envNm  The name of the application to be returned and/or created.
 *               If <code>null</code>, this parameter is interpreted as "".
 * @return       The Application Name instance corresponding to the name.
 * 
 */
public static ApplicationName getApplicationName( final String envNm )
{
ApplicationName rtrn = null;

NormalizedStringKey newEnvNm = new NormalizedStringKey( envNm );

if (allEnvs.containsKey( newEnvNm ))
	{
	rtrn = allEnvs.get( newEnvNm );
	}

else
	{
	rtrn = new ApplicationName( newEnvNm );
	allEnvs.put( newEnvNm, rtrn );
	}


return rtrn; 
}



/**
 * Constructs a new application name instance.
 * 
 * @param envNm
 * 
 */
private ApplicationName( NormalizedStringKey appNm )
{
if (appNm == null)
	throw new IllegalArgumentException( "Application Name parameter must not be null." );

appName = appNm;

return;
}



public	final String	appName()
{
return appName.getSupplied();
}



public	final String	nrmlzdAppName()
{
return appName.getNormalized();
}
}
