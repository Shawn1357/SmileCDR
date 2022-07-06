/**
 * 
 */
package ca.ontariohealth.smilecdr.support.commands.response;

/**
 * @author shawn.brant
 *
 */
public interface CSVTableGenerator 
{
public    abstract String[]    csvColumnHeaders();
public    abstract String[]    csvColumnValues();


public static String	rightJustify( int fldWidth, String fldValue )
{
String 	rtrn = null;

if (fldValue == null)
	fldValue = "";

if ((fldWidth >= 1) && (fldWidth >= fldValue.length()))
	rtrn = String.format( String.format( "%%%ds", fldWidth ), fldValue );

else
	rtrn = fldValue;

return rtrn;
}



public static String	leftJustify( int fldWidth, String fldValue )
{
String	rtrn = null;

if (fldValue == null)
	fldValue = "";

if ((fldWidth >= 1) && (fldWidth >= fldValue.length()))
	rtrn = String.format( String.format( "%%-%ds",  fldWidth ),  fldValue );

else
	rtrn = fldValue;

return rtrn;
}
}
