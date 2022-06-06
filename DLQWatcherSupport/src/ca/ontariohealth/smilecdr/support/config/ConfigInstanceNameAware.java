/**
 * 
 */
package ca.ontariohealth.smilecdr.support.config;

/**
 * @author shawn.brant
 *
 */
public interface ConfigInstanceNameAware 
{
/**
 * In the case of multiple instances of a single application running,
 * this class can be used as a prefix for that instance for configuration
 * value lookups.
 * 
 * @param instName The name of the application instance to be used as a prefix.
 *                 This name will be converted into lower-case.  This name
 *                 may be <code>null</code> or sero-length in which case no
 *                 instance name will be used to search for configuration
 *                 values.
 * 
 */
public void setInstanceName( final InstanceName instName );


/**
 * Retrieve the current instance name being used as a configuration key prefix.
 * 
 * @return The name of the current instance.  If <code>null</code> or zero-length,
 *         then no instance name is being used as part of the configuration lookup.
 */
public InstanceName	getInstanceName();
}
