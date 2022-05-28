/**
 * 
 */
package ca.ontariohealth.smilecdr.support.config.source;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Interface abstracting the entire configuration set for an application.
 * The design allows for a list of zero or more values that would be associated
 * with a particular key.  Implementations of this interface are free to
 * implement an ordered or unordered collection of these configuration values.
 *
 * @author Shawn Brant
 *
 * @param <M>	The mapping of all configuration items with its list of
 *              values.
 *
 */
public interface ConfigSource<M extends Map<String, ArrayList<String>>>
{
/**
 * Retrieve the entire mapping of the configuration source.  Each configuration
 * item may be associated with multiple configuration values which translates 
 * to the {@link ArrayList} listing of configuration values.
 * <p>
 * Each configuration item may be associated with one or more values, so this
 * routine would retrieve, for each configuration item, the list of all values
 * associated with the configuration item.
 * <p>
 * A configuration item may be associated with zero configuration values.
 * 
 * @return	The entire mapping of configuration items.
 *          Guaranteed to be not <code>null</code>.
 *          The returned {@link Map} may be empty.
 */
public	M  								getAllConfigValues();



/**
 * Retrieve all configuration values related to a specific configuration
 * property name.  Each configuration property may be associated with
 * multiple values.
 * 
 * @param configPropNm	The name of the of the property to get all associated
 *                      configuration values for.
 *                      If <code>null</code> or zero length, the returned 
 *                      map will be zero length.
 * @return				The map of all configuration values associated with
 *                      the supplied configuration property.  The key for this
 *                      map provides an ordering on individual lists of values
 *                      associated with the property name.
 *
 */
public  ArrayList<String>	getConfigValues( String configPropNm );
}
