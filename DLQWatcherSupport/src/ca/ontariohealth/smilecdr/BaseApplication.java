package ca.ontariohealth.smilecdr;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;

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



public Optional<String>              appName()
{
Optional<String>    appNm = Optional.empty();

if (appConfig != null)
    {
    String appNmStr = appConfig.configValue( ConfigProperty.APP_NAME, (String) null );
    appNm           = Optional.ofNullable( appNmStr );
    }

return appNm;
}




public Optional<String>             appDescription()
{
Optional<String>    appDesc = Optional.empty();

if (appConfig != null)
    {
    String appDescStr = appConfig.configValue( ConfigProperty.APP_DESCRIPTION, (String) null );
    appDesc           = Optional.ofNullable( appDescStr );
    }

return appDesc;
}




public Optional<Runtime.Version>     appVersion()
{
Optional<Runtime.Version> version = Optional.empty();

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
            version = Optional.ofNullable( ver );        
            }
        }
    }


return version;
}




public Optional<LocalDate>             buildDate()
{
Optional<LocalDate>    bldDate = Optional.empty();

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
            bldDate = Optional.ofNullable( bldDt );
            }
        }
    }

return bldDate;
}



public Optional<Integer>        copyrightYearStart()
{
Optional<Integer>    copyrightStartYear = Optional.empty();

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
    
    copyrightStartYear = Optional.ofNullable( startYr );
    }

return copyrightStartYear;
}
            



public Optional<Integer>        copyrightYearEnd()
{
Optional<Integer>    copyrightEndYear = Optional.empty();

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

    copyrightEndYear = Optional.ofNullable( endYr );
    }

return copyrightEndYear;
}
            


public String     copyrightNotice()
{
String              copyright = "";
StringBuilder       bldr      = new StringBuilder();

bldr.appendCodePoint( 169 );  // Copyright symbol.
bldr.append( " " );

Optional<Integer> fromYear = copyrightYearStart();
Optional<Integer> toYear   = copyrightYearEnd();
Optional<String>  holder   = copyrightHolder();

if (fromYear.isPresent())
    {
    bldr.append( String.valueOf( fromYear.get() ) );
    
    if (toYear.isPresent())
        {
        bldr.append( "-" ).append( String.valueOf( toYear.get() ) );
        }
    
    if (holder.isPresent())
        bldr.append( ", " );
    }

if (holder.isPresent())
    bldr.append( holder.get().strip() );

copyright = bldr.toString();
return copyright;
}




public String     appSignature()
{
String  signature = "";

StringBuilder   bldr = new StringBuilder();

Optional<String> appNm = appName();
if (appNm.isPresent())
    {
    bldr.append( appNm.get().strip() );
    
    Optional<Runtime.Version> ver = appVersion();
    if (ver.isPresent())
        {
        if (bldr.length() > 0)
            bldr.append( " " );
        
        bldr.append( ver.toString() );
        }
    
    Optional<String> desc = appDescription();
    if (desc.isPresent())
        {
        if (bldr.length() > 0)
            bldr.append( " - " );
        
        bldr.append( desc.get().strip() );
        }
    
    bldr.append( "\n" );
    }

bldr.append( copyrightNotice() );

signature = bldr.toString();
return signature;
}




public Optional<String>        copyrightHolder()
{
Optional<String>    holder = Optional.empty();

if (appConfig != null)
    {
    String holderStr = appConfig.configValue( ConfigProperty.COPYRIGHT_HOLDER, (String) null );
    holder           = Optional.ofNullable( holderStr );
    }

return holder;
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
