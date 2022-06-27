/**
 * 
 */
package ca.ontariohealth.smilecdr.support.commands;

import java.util.ArrayList;
import java.util.List;

/**
 * @author adminuser
 *
 */
public class DLQCommandParam
{
private String  paramName = null;
private String  paramVal  = null;



/**
 * Parses out the command line parameters from the supplied command line and
 * returns the (possibly empty) list of parameters associated with the command
 * line.
 * <p>
 * Command Line Commands have the form:
 * <pre>
 *    COMMAND:Param1=Value1,Param2,Param3=,Param4=Value4...
 * </pre>
 * This routine assumes the "<code>COMMAND:</code>" portion has been removed
 * leaving the string of zero or more parameters.
 * <p>
 * If a particular parameter does not have a value (e.g. Param2 above), the
 * key will be associated with a <code>null</code> value.
 * <p>
 * If a particular parameter has an equals sign but no value to the right of
 * it (e.g. Param3 above), the parameter will be set to a zero-length string.
 * <p>
 * All parameters and values are assumed to be Strings (even if they look like
 * integers, booleans, dates or whatever).
 * <p>
 * There is no checking as to whether particular parameters make sense for
 * a particular command; that task will be left to the command processor to
 * validate parameters.
 * 
 * @param  cmdLineParams  The string of zero or more command line parameters.
 *                        If <code>null</code> or zero-length, an empty list
 *                        of parameters will be returned.
 * @return                The list of parsed command line parameters in the
 *                        order presented. The return value is guaranteed to
 *                        be non-<code>null</code>.
 *                        
 */

public static  List<DLQCommandParam>    fromCommandLine( String cmdLineParams )
{
List<DLQCommandParam>   rtrn = new ArrayList<>();

if ((cmdLineParams != null) && (cmdLineParams.strip().length() > 0))
    {
    String[] paramSpecs = cmdLineParams.split( "," );
    
    if (paramSpecs != null)
        for (String crntSpec : paramSpecs)
            if (crntSpec.strip().length() > 0)
                rtrn.add( new DLQCommandParam( crntSpec.strip(), '=' ) );
    }

return rtrn;
}




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
        String[] args = complexParam.split( separator.toString(), 2 );
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
paramName = simpleParam;
paramVal  = null;

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
