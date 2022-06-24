package ca.ontariohealth.smilecdr.support.commands;

import ca.ontariohealth.smilecdr.support.config.Configuration;

public enum ProcessingMessageCode
{
DLQW_0000( ProcessingMessageSeverity.INFO,  "DLQW-0000", "Success" ),
DLQW_0001( ProcessingMessageSeverity.INFO,  "DLQW-0001", "%s Thread is already alive: nothing to do." ),
DLQW_0002( ProcessingMessageSeverity.ERROR, "DLQW-0002", "%s Thread did not start." ),
DLQW_0003( ProcessingMessageSeverity.INFO,  "DLQW-0003", "%s Thread is running." ),
DLQW_0004( ProcessingMessageSeverity.INFO,  "DLQW-0004", "%s Thread is not running: nothing to do." ),
DLQW_0005( ProcessingMessageSeverity.WARN,  "DLQW-0005", "%s Thread is running but Configuration indicates it should not be." ),
DLQW_0006( ProcessingMessageSeverity.WARN,  "DLQW-0006", "%s Thread is still alive after %s milliseconds. Killing it." ),
DLQW_0007( ProcessingMessageSeverity.ERROR, "DLQW-0007", "Unable to send email through SMTP EMail Server: %s" ),
DLQW_9999( ProcessingMessageSeverity.INFO,  "DLQW-9999", "Additional Info: %s" );

private ProcessingMessageSeverity   defaultSev;
private String                      msgCode;
private String                      defaultFormat;

private static final String         SEV_PROPERTY_PFX = "processing.severity.";
private static final String         FMT_PROPERTY_PFX = "processing.format.";



public static ProcessingMessageCode fromString( String codeStr )
                    throws IllegalArgumentException, NullPointerException
{
ProcessingMessageCode rtrn = null;

if ((codeStr != null) && (codeStr.length() > 0))
    {
    for (ProcessingMessageCode crnt : ProcessingMessageCode.values())
        {
        if (crnt.getCode().equalsIgnoreCase( codeStr ))
            {
            rtrn = crnt;
            break;
            }
        }
    
    if (rtrn == null)
        rtrn = ProcessingMessageCode.valueOf( codeStr );
    }

else
    throw new IllegalArgumentException( "Code string parameter must not be null or zero length." );

return rtrn;
}




private ProcessingMessageCode( ProcessingMessageSeverity sev,
                               String                    code,
                               String                    defaultFmt )
{
defaultSev    = sev;
msgCode       = code;
defaultFormat = defaultFmt;

return;
}


public ProcessingMessageSeverity    getSeverity()
{
return defaultSev;
}



public ProcessingMessageSeverity    getSeverity( Configuration  appConfig )
{
ProcessingMessageSeverity sev = defaultSev;

if (appConfig != null)
    {
    String sevString = appConfig.configValue( SEV_PROPERTY_PFX + msgCode, defaultSev.name() );
    
    try
        {
        sev = ProcessingMessageSeverity.valueOf( sevString );
        }
    
    catch (IllegalArgumentException | NullPointerException e)
        {
        sev = defaultSev;
        }
    }

return sev;
}



public void     setSeverity( ProcessingMessageSeverity newSev )
{
defaultSev = newSev;
return;
}


public String       getCode()
{
return msgCode;
}



public String       getFormat()
{
return defaultFormat;
}



public String       getFormat( Configuration    appConfig )
{
String  fmt = defaultFormat;

if (appConfig != null)
    fmt = appConfig.configValue( FMT_PROPERTY_PFX + msgCode, defaultFormat );

return fmt;
}



@Override
public String toString()
{
return getCode();
}
}
