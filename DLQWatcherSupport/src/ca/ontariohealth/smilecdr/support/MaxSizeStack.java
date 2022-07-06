/**
 * 
 */
package ca.ontariohealth.smilecdr.support;

import java.util.Stack;

/**
 * @author shawn.brant
 *
 */
public class MaxSizeStack<E> extends Stack<E> 
{
/**
 * 
 */

private static final long serialVersionUID = 34953617795884742L;

private	Integer	maxSz = null;

public MaxSizeStack( Integer maxStackSize )
{
super();

if (maxStackSize == null)
	throw new IllegalArgumentException( "Max Stack Size argument must not be null" );

else if (maxStackSize <= 0)
	throw new IllegalArgumentException( "Max Stack Size must be a positive integer." );

maxSz = maxStackSize;

return;
}




public Integer	maxSize()
{
return this.maxSz;
}




@Override
public E push(E item) 
{
E	rtrn = super.push(item);

if (this.size() > maxSize())
	removeElementAt( 0 );

return rtrn;
}
}
