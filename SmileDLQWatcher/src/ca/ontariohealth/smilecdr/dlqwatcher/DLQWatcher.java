/**
 * 
 */
package ca.ontariohealth.smilecdr.dlqwatcher;

import java.io.BufferedReader;
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
import javax.sound.sampled.Line;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ontariohealth.smilecdr.BaseApplication;
import ca.ontariohealth.smilecdr.support.config.ConfigProperty;

import org.apache.kafka.clients.consumer.Consumer;


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

//sendEMail();

logr.debug( "Exiting: DLQWatcher.launch" );
return;
}



private Consumer<Long, String>

private void sendEMail( String requestedTemplate )
{
logr.debug( "Entering: sendEMail" );

String		emailSrvr   = appConfig.configValue( ConfigProperty.EMAIL_SERVER );
String      emailUser   = appConfig.configValue( ConfigProperty.EMAIL_USER_ID );
String      emailPass   = appConfig.configValue( ConfigProperty.EMAIL_PASSWORD );

String      templateNm  = requestedTemplate;

if ((templateNm == null) || (templateNm.length() == 0))
	templateNm = appConfig.configValue( ConfigProperty.EMAIL_TEMPLATE );


String      emailFrom   = appConfig.configValue( ConfigProperty.EMAIL_FROM_ADDR.propertyName() + "." + templateNm, "" );
String		addrTo  	= appConfig.configValue( ConfigProperty.EMAIL_TO_ADDRS.propertyName()  + "." + templateNm, "" );
String  	addrCC  	= appConfig.configValue( ConfigProperty.EMAIL_CC_ADDRS.propertyName()  + "." + templateNm, "" );
String  	addrBCC 	= appConfig.configValue( ConfigProperty.EMAIL_BCC_ADDRS.propertyName() + "." + templateNm, "" );
String      subject     = appConfig.configValue( ConfigProperty.EMAIL_SUBJECT.propertyName()   + "." + templateNm, "" );
 
String  	bodyFileNm	= appConfig.configValue( ConfigProperty.EMAIL_BODY_FILE_NM.propertyName() + "." + templateNm, "" );

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
		if ((line != null) && (!line.strip().startsWith( "#")))
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