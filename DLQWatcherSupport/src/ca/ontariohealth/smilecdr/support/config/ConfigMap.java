/**
 * 
 */
package ca.ontariohealth.smilecdr.support.config;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;

import ca.ontariohealth.smilecdr.support.config.source.ConfigSource;

/**
 * @author shawn.brant
 *
 */
public interface ConfigMap< Src extends ConfigSource<Map<String, ArrayList<String> > > >
{
/**
 * Retrieve all the current mappings of Configuration Keys to Configuration Values.
 * 
 * @return The mapping of Configuration Values.  This routine is guaranteed to not
 *         return <code>null</code>, but will instead return an empty <code>Map</code>.
 *
 */
public	Map<String, ArrayList<String>>		configMap();



/**
 * Returns the number of configuration items matching the supplied
 * configuration property.
 * <p>
 * Configuration items may be repeated to create an array of related
 * configuration items.  These configuration items are considered a set and
 * may be referenced as an array using a numeric one-based index.
 * 
 * @param cfgProp	The predefined configuration property to count the entries
 *                  in the configuration source.
 * @return			The count of entries matching the configuration property.
 *                  0 if the configuration property could not be located.
 *
 */
public	Integer					configValueCount( ConfigProperty cfgProp );



/**
 * Returns the number of configuration items matching the supplied named
 * configuration property.
 * <p>
 * Configuration items may be repeated to create an array of related
 * configuration items.  These configuration items are considered a set and
 * may be referenced as an array using a numeric base-0 index.
 * 
 * @param cfgProp	The predefined configuration property to count the entries
 *                  in the configuration source.
 * @return			The count of entries matching the configuration property.
 *                  0 if the configuration property could not be located.
 *
 */
public	Integer					configValueCount( final String configKey );




/**
 * Determines if the configuration has a config mapping entry.
 * <p>
 * Equivalent to: <code>hasConfigItem( cfgProp, 1 );</code>
 * 
 * @param cfgProp	The configuration property to test exists in the configuration.
 * @return <code>true</code>  A configuration mapping entry exists. 
 *         <code>false</code> No configuration item matching the key could be found.
 * 
 * 
 */
public	boolean					hasConfigItem( ConfigProperty cfgProp );



/**
 * Determines if the configuration has a configuration item entry at the
 * supplied one-based index.
 * 
 * @param cfgProp	The configuration property to test exists in the configuration.
 * @param ndx		The one-based index to the configuration item within the
 *                  range of similar configuration items.  If <code>null</code>,
 *                  0 or negative, will be treated as 1.
 * @return			<code>true</code>  A configuration mapping entry exists.
 * 					<code>false</code> A configuration item could not be found.
 * 
 */
public	boolean					hasConfigItem( ConfigProperty cfgProp, 
		                                       Integer        ndx );




/**
 * Determines if the configuration has a config mapping entry.
 * <p>
 * Equivalent to: <code>hasConfigItem( configKey, 1 );</code>
 * 
 * @param configKey  The configuration item to test exists in the configuration.
 * @return <code>true</code>  A configuration mapping entry exists. 
 *         <code>false</code> No configuration item matching the key could be found.
 * 
 */
public	boolean					hasConfigItem( final String configKey );




/**
 * Determines if the configuration has a config mapping entry at the supplied
 * one-based index.
 * 
 * @param configKey  The configuration item to test exists in the configuration.
 * @param ndx        The one-based index to the configuration item within the
 *                   range of similar configuration items.  If <code>null</code>,
 *                   0 or negative, will be treated as 1.
 * @return           <code>true</code>  A configuration mapping entry exists.
 *                   <code>false</code> A configuration item could not bve found.
 *
 */
public  boolean					hasConfigItem( final String configKey,
		                                       Integer      ndx );



/**
 * Retrieve the set of entries corresponding to the supplied configuration
 * property as a list that can be accessed and iterated over.
 * <p>
 * If the requested configuration property is just a single value (and not
 * a list of properties), this routine will return a list of a single element.
 * 
 * @param cfgProp 	The configuration property that its values is being
 *                 	requested for.
 * @return 			The list of values founds in the configuration source.
 *                  If the configuration source does not include values
 *                  corresponding to that config property, the list will be
 *                  empty.
 *                  Guaranteed to be not <code>null</code> but the list may
 *                  be emtpy.
 */
public	ArrayList<String>			configValues( ConfigProperty cfgProp );




/**
 * Retrieve the set of entries corresponding to the named configuration
 * key as a list that can be accessed and iterated over.
 * <p>
 * If the requested configuration key is just a single value (and not
 * a list of properties), this routine will return a list of a single element.
 * 
 * @param configKey	The configuration property name that its values is being
 *                 	requested for.
 * @return 			The list of values founds in the configuration source.
 *                  If the configuration source does not include values
 *                  corresponding to that config property, the list will be
 *                  empty.
 *                  Guaranteed to be not <code>null</code> but the list may
 *                  be empty.
 */
public	ArrayList<String>			configValues( final String	configKey );




/**
 * Retrieve the value for the specified pre-defined configuration property.
 * <p>
 * Equivalent to: <code>configValue( cfgProp, 1 );</code>
 * 
 * @param cfgProp  The configuration property to look up in the underlying
 *                 configuration.
 * @return		   The value associated with the configuration property.
 *                 <code>null</code> if the property was not found.
 *                 
 */
public	String					configValue( ConfigProperty cfgProp );



/**
 * Retrieve the specific element from the array of similar configuration items
 * related to the supplied Configuration Property.
 * <p>
 * A check is made to locate the zero-based indexed element of the configuration
 * group and return that.  If that index lies outside the range of available
 * configuration elements, an exception is thrown.
 * 
 * @param cfgProp  The configuration property to retrieve the 
 * @param ndx      The one-based index of the element to be retrieved.
 *                 If <code>null</code>, 0 or negative, 1 is assumed.
 * @return         The value associated with the configuration property.
 *
 */
public	String					configValue( ConfigProperty cfgProp, 
		                                     Integer        ndx );



/**
 * Retrieve the value for the specified pre-defined configuration property
 * and use the supplied default value if the config property could not be
 * found.
 * <p>
 * Equivalent to: <code>configValue( cfgProp, 1, defaultValue );</code>
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
 * Retrieve the value for the specified pre-defined configuration property at
 * the requested index.  Returns the supplied default value if the requested
 * property could not be found.
 * 
 * @param cfgProp       The configuration property to look up in the underlying
 *                      configuration.
 * @param ndx           The one-based index of the element to be retrieved.
 *                      If <code>null</code>, 0 or negative, 1 is assumed.
 * @param defaultValue  The value to be returned if the configuration property
 *                      could not be located.
 * @return              The value associated with the configuration property.
 *                      The supplied default value if the property was not found.
 */
public	String					configValue( ConfigProperty cfgProp,
		                                     Integer        ndx,
		                                     String         defaultValue );


/**
 * Retrieves the value for a particular configuration item.
 * <p>
 * Equivalent to: <code>configValue( configKey, 1 );</code>
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
 * <p>
 * Equivalent to: <code>configValue( configKey, 1, default );</code>
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
 * Retrieve the value for the specified configuration property name at
 * the requested index.  Returns the supplied default value if the requested
 * property could not be found.
 * 
 * @param configKey The configuration item to be looked up.
 * @param ndx           The one-based index of the element to be retrieved.
 *                      If <code>null</code>, 0 or negative, 1 is assumed.
 * @param defaultValue  The value to be returned if the configuration property
 *                      could not be located.
 * @return              The value associated with the configuration property.
 *                      The supplied default value if the property was not found.
 */
public	String                  configValue( final String    configKey,
                                             Integer         ndx,
                                             String          defaultValue );



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
