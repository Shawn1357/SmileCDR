/**
 * 
 */
package ca.ontariohealth.smilecdr.support.commands;

/**
 * @author adminuser
 *
 */
public class ProcessingMessage
{
private ProcessingMessageSeverity   sev     = null;
private String                      msgCode = null;
private String                      desc    = null;

public static final     ProcessingMessage   DLQW_0000 = new ProcessingMessage( ProcessingMessageSeverity.INFO,
                                                                               "DLQW-0000",
                                                                               "Success" );

public  ProcessingMessage()
{
return;
}



public  ProcessingMessage( ProcessingMessageSeverity    msgSev,
                           String                       code,
                           String                       descr )
{
setSev( msgSev );
setMsgCode( code );
setDesc( descr );

return;
}




public ProcessingMessageSeverity getSev()
{
return sev;
}





public void setSev( ProcessingMessageSeverity sev )
{
this.sev = sev;
return;
}





public String getMsgCode()
{
return msgCode;
}





public void setMsgCode( String msgCode )
{
this.msgCode = msgCode;
return;
}





public String getDesc()
{
return desc;
}




public void setDesc( String desc )
{
this.desc = desc;
return;
}
}
