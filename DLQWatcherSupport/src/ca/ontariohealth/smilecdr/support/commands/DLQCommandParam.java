/**
 * 
 */
package ca.ontariohealth.smilecdr.support.commands;

/**
 * @author adminuser
 *
 */
public class DLQCommandParam
{
private String  paramName = null;
private String  paramVal  = null;

public DLQCommandParam( String parmNm, String parmVal )
{
paramName = parmNm;
paramVal  = parmVal;

return;
}



public String paramName()
{
return paramName;
}



public String paramValue()
{
return paramVal;
}
}
