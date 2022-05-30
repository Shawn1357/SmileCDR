/**
 * 
 */
package ca.ontariohealth.smilecdr.support.commands;

/**
 * @author adminuser
 *
 */
public enum DLQCommand 
{
LIST(    "LIST",    "Dump all known DLQWatcher commands to the logging system.",     DLQCommandContainer.class ),
QUIT(    "QUIT",    "Causes the DLQWatcher to shutdown and exit.",                   DLQCommandContainer.class ),
START(   "START",   "Starts the DLQ Polling Thread if not already started.",         DLQCommandContainer.class ),
STOP(    "STOP",    "Stops the DLQ Polling Thread if it is running.",                DLQCommandContainer.class ),
HELLO(   "HELLO",   "A no-operation command that can be used to test connectivity.", DLQCommandHELLO.class ),
UNKNOWN( "UNKNOWN", "Used when the supplied command is notr recognized.",            DLQCommandContainer.class );

private	final	String	                                commandString;
private	final	String	                                usageDescription;
private final   Class<? extends DLQCommandContainer>    implementingCls;

private DLQCommand( String cmdString, 
                    String usageDesc, 
                    Class<? extends DLQCommandContainer>  implClass )
{
if ((cmdString == null) || (cmdString.length() == 0)   ||
	(usageDesc == null) || (usageDesc.length() == 0)   ||
	(implClass == null))
	
	throw new IllegalArgumentException( "Parameters must not be null or zero-length." );

commandString       = cmdString.toUpperCase().strip();
usageDescription    = usageDesc.strip();
implementingCls     = implClass;

return;
}



public static	DLQCommand	getCommand( final String  cmd )
{
DLQCommand	rtrn = null;

String	str = (cmd != null) ? cmd.strip().toUpperCase() : "";

if ((str != null) && (str.length() > 0))
	{
	try
		{
		rtrn = DLQCommand.valueOf( str );
		}
	
	catch (IllegalArgumentException iae)
		{
		for (DLQCommand crnt : DLQCommand.values())
			{
			if (crnt.commandString.equals( str ))
				{
				rtrn = crnt;
				break;
				}
			}
		}
	}

if (rtrn == null)
	rtrn = DLQCommand.UNKNOWN;
	
return rtrn;
}



public Class<? extends DLQCommandContainer>     implementingClass()
{
return implementingCls;
}



public final String		commandStr()
{
return 	commandString;
}



public final String		usageStr()
{
return	usageDescription;
}
}
