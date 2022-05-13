/**
 * 
 */
package ca.ontariohealth.cwm.support;

import java.util.function.Function;

/**
 * @author shawn.brant
 *
 */
public class NormalizedStringKey extends NormalizedKey<String> 
{
/**
 * The default string normalizer is to strip all white space and convert to lower case.
 * This normalizer will be used if no normalizer function has been provided.
 *  
 */
private static	Function<String,String> DEFAULT_NORMALIZER = (String s)->{ return s != null ? s.replaceAll("\s", "").toLowerCase() : ""; };


/**
 * Construct a Normalized String Key using the default Normalizer function.
 * @param keyVal The value to be used as a key value.
 * 
 */
public NormalizedStringKey(String keyVal )
{
super( keyVal, DEFAULT_NORMALIZER );
}



/**
 * Construct a Normalized String Key using the supplied Normalizer function.
 * 
 * @param keyVal The value to be used as a key value.
 * @param nrmlzFunc The function which normalizes the key value.
 *                  If <code>null</code>, the default normalizer function will
 *                  be used.
 *                  
 * @see #DEFAULT_NORMALIZER
 * 
 */
public NormalizedStringKey(String keyVal, Function<String, String> nrmlzFunc) 
{
super(keyVal, nrmlzFunc != null ? nrmlzFunc : DEFAULT_NORMALIZER );
}
}
