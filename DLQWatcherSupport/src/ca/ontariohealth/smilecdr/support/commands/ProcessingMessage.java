/**
 * 
 */
package ca.ontariohealth.smilecdr.support.commands;

import java.util.IllegalFormatException;

import ca.ontariohealth.smilecdr.support.config.Configuration;

/**
 * @author adminuser
 *
 */
public class ProcessingMessage
{
private Configuration               appCfg  = null;
private ProcessingMessageCode       msgCode = null;
private String[]                    args    = null;
private String                      msgDesc = null;


public   ProcessingMessage( boolean genDescr, ProcessingMessageCode messageCode, String... messageArgs )
{
setMsgCode( messageCode );
args = messageArgs;

if (genDescr)
    genMsgDesc();

return;
}




public   ProcessingMessage( ProcessingMessageCode   messageCode, String...  messageArgs )
{
setMsgCode( messageCode );
args = messageArgs;
genMsgDesc();

return;
}



public  ProcessingMessage( ProcessingMessageCode    messageCode,
                           Configuration            appConfig,
                           String...                messageArgs )
{
appCfg = appConfig;
args   = messageArgs;
setMsgCode( messageCode );
genMsgDesc();

return;
}



public ProcessingMessageCode getMsgCode()
{
return msgCode;
}





public void setMsgCode( ProcessingMessageCode messageCode )
{
if (messageCode == null)
    throw new IllegalArgumentException( "messageCode argument must not be null" );

msgCode = messageCode;
genMsgDesc();

return;
}




public ProcessingMessageSeverity    getMsgSeverity()
{
return msgCode.getSeverity( appCfg );
}



private void genMsgDesc()
{
if (msgCode != null)
    {
    String  fmt      = msgCode.getFormat( appCfg );
    String  origDesc = msgDesc;
    try
        {
        msgDesc = String.format( fmt, (Object[]) args );
        }
    
    catch (IllegalFormatException e)
        {
        msgDesc = origDesc;
        }
    }



return;
}




public String getMsgDesc()
{
return msgDesc;
}




public void setMsgDesc( String newDesc )
{
msgDesc = newDesc;
return;
}
}
