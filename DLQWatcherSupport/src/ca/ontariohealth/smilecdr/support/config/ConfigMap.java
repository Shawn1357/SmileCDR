/**
 * 
 */
package ca.ontariohealth.smilecdr.support.config;

import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;

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
 * @param cfgProp	The configuration property to test exists in the configuration.
 * @return <code>true</code> A configuration mapping entry exists. 
 *         <code>false</code> No configuration item matching the key could be found.
 * 
 */
public	boolean					hasConfigItem( ConfigProperty cfgProp );


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
 * Retrieve the value for the specified pre-defined configuration property.
 * 
 * @param cfgProp  The configuration property to look up in the underlying
 *                 configuration.
 * @return		   The value associated with the configuration property.
 *                 <code>null</code> if the property was not found.
 *                 
 */
public	String					configValue( ConfigProperty cfgProp );


/**
 * Retrieve the value for the specified pre-defined configuration property
 * and use the supplied default value if the config property could not be
 * found.
 * 
 * @param cfgProp      The configuration property to look up in the underlying
 *                     configuration.
 * @param defaultValue The value to be returned if the configuration property
 *                     could not be located.
 * @return		       The value associated with the configuration property.
 *                     The supplied default value if the property was not found.
 *                 
 */
public  String					configValue( ConfigProperty cfgProp,
											 String 		defaultValue );



/**
 * Retrieves the value for a particular configuration item.
 * 
 * @param configKey The configuration item to be looked up.
 * @return          The value associated with the configuration item.
 * 					<code>null</code> if the property could not be found.
 * 
 */
public  String					configValue( final String configKey );


/**
 * Retrieves the value for a particular configuration item and return the
 * default value if the property could not be located.
 * 
 * @param configKey	   The configuration item to be looked up.
 * @param defaultValue The value to be returned if the configuration property
 *                     could not be located.
 * @return             The value associated with the configuration property.
 * 					   The supplied default value if the property was not found.
 * 
 */
public	String					configValue( final String 	configKey, 
											 String 		defaultValue );


/**
 * Look up the supplied predefined configuration property and convert it
 * to a boolean value according to the rules described in the
 * {@link BooleanUtils} class.
 * 
 * @param cfgProp The predefined property whose value is to be converted into
 *                a Boolean value.
 * @return        <code>true</code> or <code>false</code> depending on how the
 *                named property's value is interpreted.
 *
 * @throws IllegalArgumentException The value either could not be found or
 *                                  could not be interpreted as a Boolean.
 *
 * @see org.apache.commons.lang3.BooleanUtils
 * 
 */
public	Boolean					configBool( ConfigProperty cfgProp ) 
		                        throws IllegalArgumentException;



/**
 * Look up the supplied predefined configuration property and convert it
 * to a boolean value according to the rules described in the
 * {@link BooleanUtils} class.
 * <p>
 * If the value could not be found or the value could not be intereted as
 * a Boolean value, the default value is returned (which may be <code>null</code>.
 * 
 * @param cfgProp      The predefined property whose value is to be converted
 *                     into a Boolean value.
 * @param defaultValue The value to be returned if the requested property could
 *                     not be returned or could not be interpreted as a Boolean
 *                     value (this parameter may be <code>null</code>).
 * @return             <code>true</code> or <code>false</code> depending on
 *                     how the property's value is interpreted. The default
 *                     value is returned if the property value is not found or
 *                     can not be interpreted as a Boolean.
 *                     
 * @see org.apache.commons.lang3.BooleanUtils
 * 
 */

public  Boolean					configBool( ConfigProperty cfgProp,
										    Boolean        defaultValue );



/**
 * Look up the supplied predefined configuration property and convert it
 * to a boolean value according to the rules described in the
 * {@link BooleanUtils} class.
 * 
 * @param cfgKey  The configuration key whose value is to be converted into
 *                a Boolean value.
 * @return        <code>true</code> or <code>false</code> depending on how the
 *                named property's value is interpreted.
 *
 * @throws IllegalArgumentException The value either could not be found or
 *                                  could not be interpreted as a Boolean.
 * 
 * @see org.apache.commons.lang3.BooleanUtils
 * 
 */
public  Boolean					configBool( final String   cfgKey ) 
								throws IllegalArgumentException;



/**
 * Look up the supplied configuration property name and convert it
 * to a boolean value according to the rules described in the
 * {@link BooleanUtils} class.
 * <p>
 * If the value could not be found or the value could not be intereted as
 * a Boolean value, the default value is returned (which may be <code>null</code>.
 * 
 * @param cfgProp      The name of the property whose value is to be converted
 *                     into a Boolean value.
 * @param defaultValue The value to be returned if the requested property could
 *                     not be returned or could not be interpreted as a Boolean
 *                     value (this parameter may be <code>null</code>).
 * @return             <code>true</code> or <code>false</code> depending on
 *                     how the property's value is interpreted. The default
 *                     value is returned if the property value is not found or
 *                     can not be interpreted as a Boolean.
 *                     
 * @see org.apache.commons.lang3.BooleanUtils
 * 
 */
public  Boolean                 configBool( final String   cfgKey,
		                                    Boolean        defaultValue );


/**
 * Look up the supplied predefined configuration property and convert it
 * to an integer value according to the rules described in the
 * {@link Integer} class.
 * 
 * @param cfgProp The predefined property whose value is to be converted into
 *                an Integer value.
 * @return        The converted integer value.
 *
 * @throws IllegalArgumentException The value either could not be found or
 *                                  could not be interpreted as an integer.
 *
 * @see java.lang.Integer
 * 
 */
public	Integer					configInt( ConfigProperty cfgProp ) 
		                        throws IllegalArgumentException;



/**
 * Look up the supplied predefined configuration property and convert it
 * to a integer value according to the rules described in the
 * {@link Integer} class.
 * <p>
 * If the value could not be found or the value could not be interpreted as
 * an integer value, the default value is returned (which may be <code>null</code>.
 * 
 * @param cfgProp      The predefined property whose value is to be converted
 *                     into an integer value.
 * @param defaultValue The value to be returned if the requested property could
 *                     not be returned or could not be interpreted as an integer
 *                     value (this parameter may be <code>null</code>).
 * @return             The converted integer value. The default value is
 *                     returned if the property value is not found or
 *                     can not be interpreted as an integer.
 *                     
 * @see java.lang.Integer
 * 
 */

public  Integer					configInt( ConfigProperty cfgProp,
										   Integer        defaultValue );



/**
 * Look up the named configuration property and convert it
 * to an integer value according to the rules described in the
 * {@link Integer} class.
 * 
 * @param cfgKey  The configuration key whose value is to be converted into
 *                an integer value.
 * @return        The converted integer value.
 *
 * @throws IllegalArgumentException The value either could not be found or
 *                                  could not be interpreted as an integer.
 * 
 * @see java.lang.Integer
 * 
 */
public  Integer					configInt( final String   cfgKey ) 
								throws IllegalArgumentException;



/**
 * Look up the supplied configuration property name and convert it
 * to an integer value according to the rules described in the
 * {@link Integer} class.
 * <p>
 * If the value could not be found or the value could not be interpreted as
 * an integer value, the default value is returned (which may be <code>null</code>.
 * 
 * @param cfgProp      The name of the property whose value is to be converted
 *                     into an integer value.
 * @param defaultValue The value to be returned if the requested property could
 *                     not be returned or could not be interpreted as an integer
 *                     value (this parameter may be <code>null</code>).
 * @return             The converted integer value. The default value is
 *                     returned if the property value is not found or
 *                     can not be interpreted as a Boolean.
 *                     
 * @see java.lang.Integer
 * 
 */
public  Integer                 configInt( final String   cfgKey,
		                                   Integer        defaultValue );


}
