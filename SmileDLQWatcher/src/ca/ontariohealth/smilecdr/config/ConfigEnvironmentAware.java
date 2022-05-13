/**
 * 
 */
package ca.ontariohealth.smilecdr.config;

/**
 * @author shawn.brant
 *
 */
public interface ConfigEnvironmentAware 
{
/**
 * The name of the the environment (e.g. dev, prod, etc.) to be used as a prefix for
 * configuration value lookups.
 * 
 * @param envName  The name of the environment to be used as a prefix. This name will
 *                 be recorded converted to lower-case.  This name may be <code>null</code>
 *                 or zero length in which case no environment name will be used to search
 *                 for configuration values.
 * 
 */
public void setEnvironmentName( final EnvironmentName envName );


/**
 * Retrieve the current environment name being used as a configuration key prefix.
 * 
 * @return The name of the current environment.  If <code>null</code> or zero-length,
 *         then no environment name is being used as part of the configuration lookup.
 */
public EnvironmentName	getEnvironmentName();
}
