/**
 * 
 */
package ca.ontariohealth.smilecdr.dlqwatcher;

/**
 * @author adminuser
 *
 */
public enum DLQWatcherCommand 
{
LIST(    "LIST",    "Dump all known DLQWatcher commands to the logging system." ),
QUIT(    "QUIT",    "Causes the DLQWatcher to shutdown and exit." ),
START(   "START",   "Starts the DLQ Polling Thread if not already started." ),
STOP(    "STOP",    "Stops the DLQ Polling Thread if it is running." ),
HELLO(   "HELLO",   "A no-operation command that can be used to test connectivity." ),
UNKNOWN( "UNKNOWN", "Used when the supplied command is notr recognized." );

private	final	String	commandString;
private	final	String	usageDescription;

private DLQWatcherCommand( String cmdString, String usageDesc )
{
if ((cmdString == null) || (cmdString.length() == 0) ||
	(usageDesc == null) || (usageDesc.length() == 0))
	
	throw new IllegalArgumentException( "Parameters must not be null or zero-length." );

commandString    = cmdString.toUpperCase().strip();
usageDescription = usageDesc.strip();

return;
}



public static	DLQWatcherCommand	getCommand( final String  cmd )
{
DLQWatcherCommand	rtrn = null;

String	str = (cmd != null) ? cmd.strip().toUpperCase() : "";

if ((str != null) && (str.length() > 0))
	{
	try
		{
		rtrn = DLQWatcherCommand.valueOf( str );
		}
	
	catch (IllegalArgumentException iae)
		{
		for (DLQWatcherCommand crnt : DLQWatcherCommand.values())
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
	rtrn = DLQWatcherCommand.UNKNOWN;
	
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
