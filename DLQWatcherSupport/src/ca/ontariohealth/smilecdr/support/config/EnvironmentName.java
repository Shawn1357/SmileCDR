/**
 * 
 */
package ca.ontariohealth.smilecdr.support.config;

import java.util.HashMap;
import java.util.Map;

import ca.ontariohealth.smilecdr.support.NormalizedStringKey;

/**
 * Records the environment name an application is running within.  Typically this would
 * be "DEV", "tst01" or "prod" or anything you need and is a short word description of that
 * environment.
 * 
 * You
 * 
 * @author shawn.brant
 *
 */
public class EnvironmentName 
{
/**
 * The name of the environment as it was originally supplied to this class instance.
 * 
 */
private NormalizedStringKey		envName;


/**
 * Maintains the list of all defined environment instances so that exactly one
 * can be exist at one time for a specific environment.
 * 
 */
private static Map<NormalizedStringKey, EnvironmentName>	allEnvs = new HashMap<>();


/**
 * A factory to produce or return the already created Environment instance
 * corresponding to the supplied name.  If there is already an Environment
 * instance that matches, this routine will return that instance otherwise
 * a new instance is created.
 * <p>
 * To determine if an environment already exists, the supplied environment
 * name is normalized into a consistent format and that is what is compared.
 * 
 * @param envNm  The name of the environment to be returned and/or created.
 *               If <code>null</code>, this parameter is interpreted as "".
 * @return       The environment instance corresponding to the name.
 * 
 */
public static EnvironmentName getEnvironment( final String envNm )
{
EnvironmentName rtrn = null;

NormalizedStringKey newEnvNm = new NormalizedStringKey( envNm );

if (allEnvs.containsKey( newEnvNm ))
	{
	rtrn = allEnvs.get( newEnvNm );
	}

else
	{
	rtrn = new EnvironmentName( newEnvNm );
	allEnvs.put( newEnvNm, rtrn );
	}


return rtrn; 
}



/**
 * Constructs a new environment instance.
 * 
 * @param envNm
 * @param nrmlzdEnvNm
 * 
 */
private EnvironmentName( NormalizedStringKey envNm )
{
if (envNm == null)
	throw new IllegalArgumentException( "Environment Name parameter must not be null." );

envName   = envNm;

return;
}



public	final String	envName()
{
return envName.getSupplied();
}



public	final String	nrmlzdEnvName()
{
return envName.getNormalized();
}
}
