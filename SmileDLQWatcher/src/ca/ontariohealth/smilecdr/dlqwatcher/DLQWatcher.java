/**
 * 
 */
package ca.ontariohealth.smilecdr.dlqwatcher;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

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

/**
 * @author shawn.brant
 *
 */
public class DLQWatcher 
{
private				 Configuration		appConfig = new Configuration();
private static final Logger 			logr      = LoggerFactory.getLogger(DLQWatcher.class);

private static final String				CLI_APP_NM_SHRT = "a";
private static final String				CLI_APP_NM_LONG = "appName";
private static final String				CLI_CFG_NM_SHRT = "c";
private static final String             CLI_CFG_NM_LONG = "cfgFile";
private static final String				CLI_ENV_NM_SHRT = "e";
private static final String             CLI_ENV_NM_LONG = "envName";
private static final String             CLI_HLP_NM_SHRT = "h";
private static final String             CLI_HLP_NM_LONG = "help";


private              Options			cliOpts	  = new Options();
private				 CommandLineParser	parser	  = new DefaultParser();
private				 CommandLine		cmdLine   = null;

/**
 * @param args
 */
public static void main(String[] args) 
	{
	logr.debug( "Entering: main" );
	DLQWatcher	watcher = new DLQWatcher();

	watcher.launch( args );
	
	logr.debug( "Exiting: main");
	return;
	}



public	void launch(String[] args)
{
logr.debug( "Entering: launch" );

boolean	startProcessing = true;

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
	String cliAppName = DLQWatcher.class.getSimpleName();
	String cliEnvName = null;
	String cliCfgFile = DLQWatcher.class.getSimpleName() + ".properties";
	
	
	ApplicationName appName       = null;
	EnvironmentName envName       = null;
	
	if (cmdLine.hasOption(CLI_APP_NM_LONG))
		cliAppName = cmdLine.getOptionValue( CLI_APP_NM_LONG );
	
	if (cmdLine.hasOption(CLI_ENV_NM_LONG))
		cliEnvName = cmdLine.getOptionValue( CLI_ENV_NM_LONG );
	
	if ((cliAppName != null) && (cliAppName.length() > 0)) appName = ApplicationName.getApplicationName( cliAppName );
	if ((cliEnvName != null) && (cliEnvName.length() > 0)) envName = EnvironmentName.getEnvironment( cliEnvName );
	
	appConfig.loadConfiguration( appName, envName, cliCfgFile );
	sendEMail();
	}

logr.debug( "Exiting: launch" );
return;
}




private void	createCLIOptions()
{
cliOpts.addOption( CLI_CFG_NM_SHRT, CLI_CFG_NM_LONG, true,  "Configuration Properties file name");
cliOpts.addOption( CLI_APP_NM_SHRT, CLI_APP_NM_LONG, true,  "Set the name of the application");
cliOpts.addOption( CLI_ENV_NM_SHRT, CLI_ENV_NM_LONG, true,  "Set Operating Environment Name (DEV, tst01, ...)" );
cliOpts.addOption( CLI_HLP_NM_SHRT, CLI_HLP_NM_LONG, false, "Display Command Line Usage information.");

return;
}



private void sendEMail()
{
logr.debug( "Entering: sendEMail" );

String		emailSrvr   = appConfig.configValue( "email.server" );
String      emailUser   = appConfig.configValue( "email.userID" );
String      emailPass   = appConfig.configValue( "email.password" );

String      templateNm  = appConfig.configValue( "email.template" );

String      emailFrom   = appConfig.configValue( "email.from." + templateNm, "" );
String		addrTo  	= appConfig.configValue( "email.to." + templateNm,   "" );
String  	addrCC  	= appConfig.configValue( "email.cc." + templateNm,   "" );
String  	addrBCC 	= appConfig.configValue( "email.bcc." + templateNm,  "" );
String      subject     = appConfig.configValue( "email.subj." + templateNm, "" );
 
String  	bodyFileNm	= appConfig.configValue( "email.body." + templateNm, "" );

String  	body    	= loadFileIntoString( bodyFileNm );

Properties	emailProps = new Properties();
emailProps.put( "mail.smtp.auth", "true" );
emailProps.put( "mail.smtp.host", emailSrvr );

try
	{
	Authenticator auth 	  = new SMTPAuthenticator( emailUser, emailPass );
    Session 	  session = Session.getInstance( emailProps, auth );
    Message 	  message = new MimeMessage( session );

    message.setFrom( new InternetAddress( emailFrom ) );
    if ((addrTo != null) && (addrTo.length() > 0))
    	message.setRecipients( Message.RecipientType.TO, 
    						   InternetAddress.parse( addrTo ) );

    if ((addrCC != null) && (addrCC.length() > 0))
    	message.setRecipients( Message.RecipientType.CC, 
    						   InternetAddress.parse( addrCC ) );

    if ((addrBCC != null) && (addrBCC.length() > 0))
    	message.setRecipients( Message.RecipientType.BCC, 
    						   InternetAddress.parse( addrBCC ) );
 
    message.setSubject( subject );
    message.setText( body );

    Transport.send(message);
    logr.info( "EMail Message has been sent." );
	}

catch (Exception e)
	{
	logr.error( "Unable to send email: ", e );
	}


logr.debug( "Exiting: sendEMail" );
return;
}




private void  displayUsage()
{
HelpFormatter	frmtr = new HelpFormatter();
frmtr.printHelp( "Commandline syntax", cliOpts );

return;
}




private String	loadFileIntoString( String fileNm )
{
logr.debug("Entering: loadFileIntoString");
String	    rtrn  = "";

try (FileInputStream iStrm = new FileInputStream(fileNm) )
	{
	StringBuilder	content = new StringBuilder();
	BufferedReader	rdr     = new BufferedReader( new InputStreamReader(iStrm) );

	String	line = null;
	while ((line = rdr.readLine()) != null)
		{
		content.append( line + System.lineSeparator() );
		}
	
	rtrn = content.toString();
	}

catch (IOException ioe)
	{
	logr.error( "Unable to read from: {}", fileNm, ioe );
	}


logr.debug("Exiting: loadFileIntoString");
return rtrn;
}




private class SMTPAuthenticator extends Authenticator 
{
String	emailUserID		= null;
String  userPassword	= null;

public SMTPAuthenticator( String emailID, String passwd )
{
emailUserID  = emailID;
userPassword = passwd;
}


public PasswordAuthentication getPasswordAuthentication() 
	{
    return new PasswordAuthentication(emailUserID, 
    		userPassword);
	}
}


}