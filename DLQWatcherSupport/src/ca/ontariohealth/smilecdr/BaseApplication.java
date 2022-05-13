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

protected				Configuration		appConfig 		= new Configuration();

protected				ApplicationName		appName			= null;
protected				EnvironmentName		envName			= null;


public BaseApplication() 
{
super();
}


protected void launch( String[] args )
{
logr.debug("Enmtering: launch");

String	 clsPath    	 = System.getProperty("java.class.path");
String[] pathElems		 = clsPath.split(File.pathSeparator);
boolean	 startProcessing = true;

logr.debug( "Class Path Elements:" );
for (String pathElem : pathElems)
	logr.debug( "    {}", pathElem );


createCLIOptions();

try
	{
	cmdLine = parser.parse( cliOpts, args );
	}

catch (ParseException pe)
	{
	startProcessing = false;
	}


if ((!startProcessing) ||
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




protected void	createCLIOptions()
{
cliOpts.addOption( CLI_CFG_NM_SHRT, CLI_CFG_NM_LONG, true,  "Configuration Properties file name");
cliOpts.addOption( CLI_APP_NM_SHRT, CLI_APP_NM_LONG, true,  "Set the name of the application");
cliOpts.addOption( CLI_ENV_NM_SHRT, CLI_ENV_NM_LONG, true,  "Set Operating Environment Name (DEV, tst01, ...)" );
cliOpts.addOption( CLI_HLP_NM_SHRT, CLI_HLP_NM_LONG, false, "Display Command Line Usage information.");

return;
}




protected void displayUsage() 
{
HelpFormatter	frmtr = new HelpFormatter();
frmtr.printHelp( "Commandline syntax", cliOpts );
	
return;
}

}