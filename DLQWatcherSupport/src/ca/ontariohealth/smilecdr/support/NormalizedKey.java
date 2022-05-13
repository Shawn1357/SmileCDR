/**
 * 
 */
package ca.ontariohealth.smilecdr.support;

import java.util.function.Function;

/**
 * @author shawn.brant
 *
 */
public class NormalizedKey<K extends Comparable<K>> implements Comparable<K> 
{
private	K	            asSupplied = null;
private K	            nrmlzdKey  = null;
private Function<K, K>	nrmlzFunc  = null;


public NormalizedKey( K keyVal, Function<K, K> nrmlzFunc )
{
if (nrmlzFunc == null)
	throw new IllegalArgumentException( "Normalize Function parameter must not be null" );

asSupplied = keyVal;
nrmlzdKey  = nrmlzFunc.apply( asSupplied );

return;
}



public Function<K, K>	getNormalizer()
{
return nrmlzFunc;
}



public	final K		getSupplied()
{
return asSupplied;
}


public	final K		getNormalized()
{
return nrmlzdKey;
}


@Override
public int hashCode() 
{
return nrmlzdKey.hashCode();
}



@Override
public boolean equals(Object obj) 
{
return nrmlzdKey.equals(obj);
}


@Override
public String toString() 
{
return nrmlzdKey.toString();
}


@Override
public int compareTo(K o) 
{
return nrmlzdKey.compareTo( o );
}
}
