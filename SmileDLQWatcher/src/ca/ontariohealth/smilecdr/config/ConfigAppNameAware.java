/**
 * 
 */
package ca.ontariohealth.smilecdr.config;

/**
 * @author shawn.brant
 *
 */
public interface ConfigAppNameAware 
{
/**
 * The name of the the application (e.g. dlqwatcher) to be used as a prefix for
 * configuration value lookups.
 * 
 * @param envName  The name of the application to be used as a prefix. This name will
 *                 be recorded converted to lower-case.  This name may be <code>null</code>
 *                 or zero length in which case no application name will be used to search
 *                 for configuration values.
 * 
 */
public void setApplicationName( final ApplicationName envName );


/**
 * Retrieve the current application name being used as a configuration key prefix.
 * 
 * @return The name of the current application.  If <code>null</code> or zero-length,
 *         then no application name is being used as part of the configuration lookup.
 */
public ApplicationName	getApplicationName();
}
