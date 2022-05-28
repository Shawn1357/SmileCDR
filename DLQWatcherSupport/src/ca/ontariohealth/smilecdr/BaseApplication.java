package ca.ontariohealth.smilecdr;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ontariohealth.smilecdr.support.config.ApplicationName;
import ca.ontariohealth.smilecdr.support.config.Configuration;
import ca.ontariohealth.smilecdr.support.config.EnvironmentName;

public abstract class BaseApplication 
{
static final Logger 						logr      		= LoggerFactory.getLogger(BaseApplication.class);

protected static final	String 				CLI_APP_NM_SHRT = "a";
protected static final	String 				CLI_APP_NM_LONG = "appName";
protected static final	String 				CLI_CFG_NM_SHRT = "c";
protected static final	String 				CLI_CFG_NM_LONG = "cfgFile";
protected static final	String 				CLI_ENV_NM_SHRT = "e";
protected static final	String 				CLI_ENV_NM_LONG = "envName";
protected static final	String 				CLI_HLP_NM_SHRT = "h";
protected static final	String 				CLI_HLP_NM_LONG = "help";

protected 				Options				cliOpts			= new Options();
protected				CommandLineParser	parser			= new DefaultParser();
protected 				CommandLine			cmdLine			= null;
protected				String[]			origCmdLine		= null;

protected				Configuration		appConfig 		= new Configuration();

protected				ApplicationName		appName			= null;
protected				EnvironmentName		envName			= null;


public BaseApplication() 
{
super();
return;
}


protected void launch( String[] args )
{
logr.debug("Enmtering: launch");

String	 clsPath    	 = System.getProperty("java.class.path");
String[] pathElems		 = clsPath.split(File.pathSeparator);
boolean  displayUsage    = false;
boolean	 startProcessing = true;

logr.debug( "Class Path Elements:" );
for (String pathElem : pathElems)
	logr.debug( "    {}", pathElem );


/*
 * Make the client supplied command line available in case it is ever
 * required.
 * 
 */
origCmdLine = args;


/*
 * Define the command line options.
 * 
 */

createCLIOptions( cliOpts );

/*
 * Before anything else, parse the command line in a throw-away fashion
 * only checking for the display usage option.  We need to do this because
 * if the client application defines 'required' parameters an exception gets
 * thrown and we miss the opportunity to dispay usage.
 * 
 */

for (String crnt : args)
	{
	String 	hlpShort = "-"  + CLI_HLP_NM_SHRT;
	String  hltLong  = "--" + CLI_HLP_NM_LONG;
	
	if ((crnt != null) && 
		(crnt.equals( hlpShort ) || crnt.equals( hltLong )))
		{
		startProcessing = false;
		displayUsage    = true;
		}
	}

/*
 * Now parse the command line for real.
 * 
 */
if (!displayUsage)
	{
	try
		{
		cmdLine = parseCommandLine( cliOpts, parser, args );
		}
	
	catch (ParseException pe)
		{
		startProcessing = false;
		logr.error( "Unable to parse the Command Line:", pe );
		}
	}


if ((!startProcessing) 	||
	(displayUsage)		||
	((cmdLine != null) && cmdLine.hasOption( CLI_HLP_NM_LONG )))
		{
		startProcessing = false;
		displayUsage();
		}

if (startProcessing)
	{
	String cliAppName = this.getClass().getSimpleName();
	String cliEnvName = null;
	String cliCfgFile = cliAppName + ".properties";
	
	
	ApplicationName appName       = null;
	EnvironmentName envName       = null;
	
	if (cmdLine.hasOption(CLI_APP_NM_LONG))
		cliAppName = cmdLine.getOptionValue( CLI_APP_NM_LONG );
	
	if (cmdLine.hasOption(CLI_ENV_NM_LONG))
		cliEnvName = cmdLine.getOptionValue( CLI_ENV_NM_LONG );
	
	if (cmdLine.hasOption(CLI_CFG_NM_LONG))
		cliCfgFile = cmdLine.getOptionValue( CLI_CFG_NM_LONG );
	
	if ((cliAppName != null) && (cliAppName.length() > 0)) appName = ApplicationName.getApplicationName( cliAppName );
	if ((cliEnvName != null) && (cliEnvName.length() > 0)) envName = EnvironmentName.getEnvironment( cliEnvName );
	
	logr.debug( "About to load configuration from: {}", cliCfgFile );
	appConfig.loadConfiguration( appName, envName, cliCfgFile );
	}

if (startProcessing)
	this.launch();


logr.debug("Exiting: launch");
return;	
}
	

protected abstract void launch();






private CommandLine		parseCommandLine( Options 			cliOpts, 
								  		  CommandLineParser	parser,
								  		  String[]			args )
			throws ParseException
			
{
CommandLine	parsedCmdLine = null;

parsedCmdLine = parser.parse( cliOpts, args );

return parsedCmdLine;
}





protected void	createCLIOptions( Options cmdLineOpts )
{
cmdLineOpts.addOption( CLI_CFG_NM_SHRT, CLI_CFG_NM_LONG, true,  "Configuration Properties file name");
cmdLineOpts.addOption( CLI_APP_NM_SHRT, CLI_APP_NM_LONG, true,  "Set the name of the application");
cmdLineOpts.addOption( CLI_ENV_NM_SHRT, CLI_ENV_NM_LONG, true,  "Set Operating Environment Name (DEV, tst01, ...)" );
cmdLineOpts.addOption( CLI_HLP_NM_SHRT, CLI_HLP_NM_LONG, false, "Display Command Line Usage information.");

return;
}




protected void displayUsage() 
{
HelpFormatter	frmtr = new HelpFormatter();
frmtr.printHelp( "Commandline syntax", cliOpts );
	
return;
}



public	boolean	cmdLineUsageRequested()
{
return cmdLine.hasOption( CLI_HLP_NM_LONG );
}
}
