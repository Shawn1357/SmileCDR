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
recordTwoFieldParam( parmNm, parmVal );
return;
}


public DLQCommandParam( String complexParam, Character separator )
{
if ((complexParam != null) && (complexParam.length() > 0))
    {
    if (separator != null)
        {
        String[] args = complexParam.split( separator.toString(), 1 );
        recordTwoFieldParam( args );
        }
    }

return;
}



public DLQCommandParam( String simpleParam )
{
recordSimpleParam( simpleParam );
return;
}




private void recordSimpleParam( String simpleParam )
{
recordSimpleParam( simpleParam );
return;
}



private void recordTwoFieldParam( String[] args )
{
if (args != null)
    {
    if ((args != null) && (args.length > 1))
        recordTwoFieldParam( args[0], args[1] );
    
    else if (args.length == 1)
        recordSimpleParam( args[0] );
    }

return;
}



private void recordTwoFieldParam( String parmName, String parmVal )
{
paramName = parmName;
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
