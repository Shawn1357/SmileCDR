/**
 * 
 */
package ca.ontariohealth.smilecdr.support.email;

import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ontariohealth.smilecdr.support.commands.DLQCommandOutcome;
import ca.ontariohealth.smilecdr.support.commands.DLQRecordsInterpreter;
import ca.ontariohealth.smilecdr.support.commands.DLQResponseContainer;
import ca.ontariohealth.smilecdr.support.commands.ProcessingMessage;
import ca.ontariohealth.smilecdr.support.commands.ProcessingMessageCode;
import ca.ontariohealth.smilecdr.support.config.BasicAuthCredentials;
import ca.ontariohealth.smilecdr.support.config.ConfigProperty;
import ca.ontariohealth.smilecdr.support.config.Configuration;
import ca.ontariohealth.smilecdr.support.config.FileBasedCredentials;

/**
 * @author adminuser
 *
 */
public class AWSEMailNotifier extends EMailNotifier
{
private static final Logger                 logr        = LoggerFactory.getLogger( AWSEMailNotifier.class );

private              BasicAuthCredentials   creds       = null;


public AWSEMailNotifier( Configuration appCfg, EMailNotificationType notificationType ) 
            throws AddressException
{
super( appCfg, notificationType );
creds = new FileBasedCredentials( emailConnectionDetails().credentialsFileName() );

return;
}


@Override
public 	void	sendEMail( DLQResponseContainer		resp,						
						   DLQRecordsInterpreter	rcrds )

{
logr.debug( "Entering: sendEMail" );

Properties  emailProps = new Properties();
emailProps.put( "mail.smtp.auth", "true" );
emailProps.put( "mail.smtp.host", emailConnectionDetails().smtpServerName() );
emailProps.put( "mail.smtp.port", emailConnectionDetails().smtpPortNumber().toString() );

Authenticator auth    = new SMTPAuthenticator( creds );
String        msgBody = loadFileIntoString( rcrds );

try
    {
    Session       session = Session.getInstance( emailProps, auth );
    Message       message = new MimeMessage( session );
    Address[]     addrs   = null;

    message.setFrom( emailTemplateDetails().fromEMailAddress() );
    
    addrs = emailTemplateDetails().toEMailAddresses();
    if ((addrs != null) && (addrs.length > 0))
        message.setRecipients( Message.RecipientType.TO, addrs );

    addrs = emailTemplateDetails().ccEMailAddresses();
    if ((addrs != null) && (addrs.length > 0))
        message.setRecipients( Message.RecipientType.CC, addrs );

    addrs = emailTemplateDetails().bccEMailAddresses();
    if ((addrs != null) && (addrs.length > 0))
        message.setRecipients( Message.RecipientType.BCC, addrs ); 
 
    message.setSubject( expandVariables( emailTemplateDetails().subjectLine() ) );
    message.setContent( msgBody, "text/html" );

    Transport.send(message);

    if (resp != null)
        resp.setOutcome( DLQCommandOutcome.SUCCESS );

    logr.info( "EMail Message has been sent." );
    }

catch (MessagingException e) 
    {
    logr.error( "Unable to send email:", e );
    if (resp != null)
        {
        String msg = e.getMessage();

        resp.setOutcome( DLQCommandOutcome.ERROR );
        resp.addProcessingMessage( new ProcessingMessage( ProcessingMessageCode.DLQW_0007, appConfig, appConfig.configValue( ConfigProperty.EMAIL_SERVER ) ) );
        resp.addProcessingMessage( new ProcessingMessage( ProcessingMessageCode.DLQW_9999, appConfig, e.getClass().getSimpleName() ));
        if ((msg != null) && (msg.strip().length() > 0))
            resp.addProcessingMessage( new ProcessingMessage( ProcessingMessageCode.DLQW_9999, appConfig, msg.strip() ) );
        }
    }

logr.debug( "Exiting: sendEMail" );
return;	
}






private class SMTPAuthenticator extends Authenticator 
{
String  emailUserID     = null;
String  userPassword    = null;

public SMTPAuthenticator( BasicAuthCredentials creds )
{
if (creds == null)
    throw new IllegalArgumentException( SMTPAuthenticator.class.getSimpleName() + " constructor parameters must not be null" );

emailUserID  = creds.username();
userPassword = creds.password();
}



public PasswordAuthentication getPasswordAuthentication() 
{
return new PasswordAuthentication( emailUserID, userPassword );
}
}


}
