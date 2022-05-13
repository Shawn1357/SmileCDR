/**
 * 
 */
package ca.ontariohealth.smilecdr.config;

import java.util.Map;

/**
 * @author shawn.brant
 *
 */
public interface ConfigMap 
{
/**
 * Trigger a load (or reload) of configuration 
 * @param appName           The name of the application to be used as a prefix for looking up properties.
 * @param environmentName   The name of the environment (e.g. dev, prod, etc.) to be used as a prefix for looking up properties.
 * @param fileName          The name of the file to be searched for using the classloader rules for searching.
 * 
 */
public	void		loadConfiguration( ApplicationName	appName,
									   EnvironmentName	environmentName,
									   final String		fileName );



/**
 * Retrieve all the current mappings of Configuration Keys to Configuration Values.
 * 
 * @return The mapping of Configuration Values.  This routine is guaranteed to not
 *         return <code>null</code>, but will instead return an empty <code>Map</code>.
 *
 */
public	Map<String, String>		configMap();



/**
 * Determines if the configuration has a config mapping entry.
 * 
 * @param configKey  The configuration item to test exists in the configuration.
 * @return <code>true</code> A configuration mapping entry exists. 
 *         <code>false</code> No configuration item matching the key could be found.
 * 
 */
public	boolean					hasConfigItem( final String configKey );


/**
 * Retrieves the value for a particular configuration item.
 * 
 * @param configKey The configuration item to be looked up.
 * @return The value associated with the configuration item.
 */
public  String					configValue( final String configKey );


public	String					configValue( final String configKey, String defaultValue );
}
