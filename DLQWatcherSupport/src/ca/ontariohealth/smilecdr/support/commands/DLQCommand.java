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
LIST(    "LIST",    "Dump all known DLQWatcher commands to the logging system." ),
QUIT(    "QUIT",    "Causes the DLQWatcher to shutdown and exit." ),
START(   "START",   "Starts the DLQ Polling Thread if not already started." ),
STOP(    "STOP",    "Stops the DLQ Polling Thread if it is running." ),
HELLO(   "HELLO",   "A no-operation command that can be used to test connectivity." ),
UNKNOWN( "UNKNOWN", "Used when the supplied command is notr recognized." );

private	final	String	                                commandString;
private	final	String	                                usageDescription;

private DLQCommand( String cmdString, 
                    String usageDesc )
{
if ((cmdString == null) || (cmdString.length() == 0)   ||
	(usageDesc == null) || (usageDesc.length() == 0))
	
	throw new IllegalArgumentException( "Parameters must not be null or zero-length." );

commandString       = cmdString.toUpperCase().strip();
usageDescription    = usageDesc.strip();

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



public final String		commandStr()
{
return 	commandString;
}



public final String		usageStr()
{
return	usageDescription;
}
}
