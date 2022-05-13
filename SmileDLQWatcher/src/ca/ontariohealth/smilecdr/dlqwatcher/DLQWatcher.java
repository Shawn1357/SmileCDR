/**
 * 
 */
package ca.ontariohealth.smilecdr.dlqwatcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ontariohealth.smilecdr.support.config.ApplicationName;
import ca.ontariohealth.smilecdr.support.config.EnvironmentName;

/**
 * @author shawn.brant
 *
 */
public class DLQWatcher extends BaseApplication 
{
static final Logger 			logr      = LoggerFactory.getLogger(DLQWatcher.class);

/**
 * @param args
 * 
 */
public static void main(String[] args) 
	{
	logr.debug( "Entering: main" );
	
	DLQWatcher	watcher = new DLQWatcher();
	watcher.launch(args);
	
	logr.debug( "Exiting: main");
	return;
	}


	
@Override
protected	void launch()
{
logr.debug( "Entering: DLQWatcher.launch" );

sendEMail();

logr.debug( "Exiting: DLQWatcher.launch" );
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




private String	loadFileIntoString( String fileNm )
{
logr.debug("Entering: loadFileIntoString");
String	    rtrn  = "";

try (InputStream iStrm = ClassLoader.getSystemResourceAsStream( fileNm ) )
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