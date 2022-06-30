package ca.ontariohealth.smilecdr;

import java.io.File;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ontariohealth.smilecdr.support.config.ApplicationName;
import ca.ontariohealth.smilecdr.support.config.ConfigProperty;
import ca.ontariohealth.smilecdr.support.config.Configuration;
import ca.ontariohealth.smilecdr.support.config.EnvironmentName;
import ca.ontariohealth.smilecdr.support.config.InstanceName;

public abstract class BaseApplication 
{
static final Logger 						logr      		  = LoggerFactory.getLogger(BaseApplication.class);

protected static final	String 				CLI_APP_NM_SHRT   = "a";
protected static final	String 				CLI_APP_NM_LONG   = "appName";
protected static final	String 				CLI_CFG_NM_SHRT   = "c";
protected static final	String 				CLI_CFG_NM_LONG   = "cfgFile";
protected static final  String              CLI_STRT_DEL_SHRT = "d";
protected static final  String				CLI_STRT_DEL_LONG = "startup-delay";
protected static final	String 				CLI_ENV_NM_SHRT   = "e";
protected static final	String 				CLI_ENV_NM_LONG   = "envName";
protected static final	String 				CLI_HLP_NM_SHRT   = "h";
protected static final	String 				CLI_HLP_NM_LONG   = "help";
protected static final  String              CLI_INST_NM_SHRT  = "n";
protected static final  String              CLI_INST_NM_LONG  = "instName";
protected static final  String              CLI_SLNT_NM_SHRT  = "q";
protected static final  String              CLI_SLNT_NM_LONG  = "quiet";

protected               Instant             appStartTime     = Instant.now();

protected 				Options				cliOpts			 = new Options();
protected				CommandLineParser	parser			 = new DefaultParser();
protected 				CommandLine			cmdLine			 = null;
protected				String[]			origCmdLine		 = null;

protected				Configuration		appConfig 		 = new Configuration();

protected				ApplicationName		appName			 = null;
protected				EnvironmentName		envName			 = null;


public BaseApplication() 
{
super();
return;
}



public String              appName()
{
String    appNm = null;

if (appConfig != null)
    appNm = appConfig.configValue( ConfigProperty.APP_NAME, (String) null );

return appNm;
}




public String             appDescription()
{
String    appDesc = null;

if (appConfig != null)
    appDesc = appConfig.configValue( ConfigProperty.APP_DESCRIPTION, (String) null );

return appDesc;
}




public Runtime.Version     appVersion()
{
Runtime.Version version = null;

if (appConfig != null)
    {
    String  verStr = appConfig.configValue( ConfigProperty.APP_VERSION, (String) null );
    
    if (verStr != null)
        {
        Runtime.Version ver = null;
        try
            {
            ver = Runtime.Version.parse( verStr );
            }
        
        catch (IllegalArgumentException | NullPointerException e)
            {
            ver = null;
            }
        
        finally
            {
            version = ver;        
            }
        }
    }


return version;
}




public LocalDate             buildDate()
{
LocalDate    bldDate = null;

if (appConfig != null)
    {
    String bldDtStr = appConfig.configValue( ConfigProperty.BUILD_DATE, (String) null );
    
    if (bldDtStr != null)
        {
        LocalDate    bldDt = null;
        
        try
            {
            bldDt = LocalDate.parse( bldDtStr );
            }
        
        catch (DateTimeParseException e)
            {
            bldDt = null;
            }
        
        finally
            {
            bldDate = bldDt;
            }
        }
    }

return bldDate;
}



public Integer        copyrightYearStart()
{
Integer    copyrightStartYear = null;

if (appConfig != null)
    {
    Integer startYr = null;
    
    try
        {
        startYr = appConfig.configInt( ConfigProperty.COPYRIGHT_YEAR_START, null );
        }
    
    catch(NumberFormatException e)
        {
        startYr = null;
        }
    
    copyrightStartYear = startYr;
    }

return copyrightStartYear;
}
            



public Integer        copyrightYearEnd()
{
Integer    copyrightEndYear = null;

if (appConfig != null)
    {
    Integer endYr    = null;
    
    try
        {
        endYr = appConfig.configInt( ConfigProperty.COPYRIGHT_YEAR_END, null );
        }
    
    catch (NumberFormatException e)
        {
        endYr = null;
        }

    copyrightEndYear = endYr;
    }

return copyrightEndYear;
}
            


public String     copyrightNotice()
{
String              copyright = "";
StringBuilder       bldr      = new StringBuilder();

bldr.appendCodePoint( 169 );  // Copyright symbol.
bldr.append( " " );

Integer fromYear = copyrightYearStart();
Integer toYear   = copyrightYearEnd();
String  holder   = copyrightHolder();

if (fromYear != null)
    {
    bldr.append( String.valueOf( fromYear ) );
    
    if (toYear != null)
        {
        bldr.append( "-" ).append( String.valueOf( toYear ) );
        }
    
    if (holder != null)
        bldr.append( ", " );
    }

if (holder != null)
    bldr.append( holder.strip() );

copyright = bldr.toString();
return copyright;
}




public String     appSignature()
{
String  signature = "";

StringBuilder   bldr = new StringBuilder();

String appNm = appName();
if (appNm != null)
    {
    bldr.append( appNm.strip() );
    
    Runtime.Version ver = appVersion();
    if (ver != null)
        {
        if (bldr.length() > 0)
            bldr.append( " " );
        
        bldr.append( ver.toString() );
        }
    
    String desc = appDescription();
    if (desc != null)
        {
        if (bldr.length() > 0)
            bldr.append( " - " );
        
        bldr.append( desc.strip() );
        }
    
    bldr.append( "\n" );
    }

bldr.append( copyrightNotice() );

signature = bldr.toString();
return signature;
}




public String        copyrightHolder()
{
String    holder = null;

if (appConfig != null)
    holder = appConfig.configValue( ConfigProperty.COPYRIGHT_HOLDER, (String) null );

return holder;
}





protected void launch( String[] args )
{
logr.debug("Entering: launch");

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
 * thrown and we miss the opportunity to display usage.
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
	String cliAppName  = this.getClass().getSimpleName();
	String cliEnvName  = null;
	String cliInstName = null;
	String cliCfgFile  = cliAppName + ".properties";
	
	
	ApplicationName appName       = null;
	EnvironmentName envName       = null;
	InstanceName    instName      = null;
	
	if (cmdLine.hasOption(CLI_APP_NM_LONG))
		cliAppName = cmdLine.getOptionValue( CLI_APP_NM_LONG );
	
	if (cmdLine.hasOption(CLI_ENV_NM_LONG))
		cliEnvName = cmdLine.getOptionValue( CLI_ENV_NM_LONG );
	
	if (cmdLine.hasOption(CLI_CFG_NM_LONG))
		cliCfgFile = cmdLine.getOptionValue( CLI_CFG_NM_LONG );
	
	if (cmdLine.hasOption(CLI_INST_NM_LONG))
	    cliInstName = cmdLine.getOptionValue( CLI_INST_NM_LONG );
	
	
	if ((cliAppName  != null) && (cliAppName.length()  > 0)) appName  = ApplicationName.getApplicationName( cliAppName );
	if ((cliEnvName  != null) && (cliEnvName.length()  > 0)) envName  = EnvironmentName.getEnvironment( cliEnvName );
	if ((cliInstName != null) && (cliInstName.length() > 0)) instName = InstanceName.getInstanceName( cliInstName );
	
	logr.debug( "About to load configuration from: {}", cliCfgFile );
	appConfig.loadConfiguration( appName, envName, instName, cliCfgFile );
	}

/*
 * Check if we should delay processing start.
 * 
 */

if (!displayUsage)
	{
	Integer	startDelay = appConfig.configInt( ConfigProperty.STARTUP_DELAY_SECS );
	if (cmdLine.hasOption( CLI_STRT_DEL_LONG ))
		startDelay = Integer.parseInt( cmdLine.getOptionValue( CLI_STRT_DEL_LONG ) );
	
	if (startDelay != null)
		{
		if (startDelay < 0)
			startDelay = 0;
		
		if (startDelay > 0)
			{
			logr.debug( "An application startup delay has been specified for {} second(s)...", startDelay );
			try 
				{
				Thread.sleep( startDelay * 1000 );
				} 
			catch (InterruptedException e)
				{
				// Nothing to do... ignore the fact we didn't finish the delay.
				logr.warn( "The startup delay was interrupted because: {}", e.getMessage() );
				}
			logr.debug( "Startup delay is complete. Application startup is continuing." );
			}
		}
	}


/*
 * Start the application for real...
 * 
 */

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
cmdLineOpts.addOption( CLI_CFG_NM_SHRT,   CLI_CFG_NM_LONG,   true,  "Configuration Properties file name");
cmdLineOpts.addOption( CLI_STRT_DEL_SHRT, CLI_STRT_DEL_LONG, true,  "Specify (in seconds) a delay in processing on start up." ); 
cmdLineOpts.addOption( CLI_APP_NM_SHRT,   CLI_APP_NM_LONG,   true,  "Set the name of the application");
cmdLineOpts.addOption( CLI_ENV_NM_SHRT,   CLI_ENV_NM_LONG,   true,  "Set Operating Environment Name (DEV, tst01, ...)" );
cmdLineOpts.addOption( CLI_INST_NM_SHRT,  CLI_INST_NM_LONG,  true,  "Set the instance name of multiple instance of an app." );
cmdLineOpts.addOption( CLI_SLNT_NM_SHRT,  CLI_SLNT_NM_SHRT,  false, "Start the instance with no banner information." );
cmdLineOpts.addOption( CLI_HLP_NM_SHRT,   CLI_HLP_NM_LONG,   false, "Display Command Line Usage information.");

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
