/**
 * 
 */
package ca.ontariohealth.smilecdr.support.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ontariohealth.smilecdr.support.config.BasicAuthCredentials;
import ca.ontariohealth.smilecdr.support.config.ConfigProperty;
import ca.ontariohealth.smilecdr.support.config.Configuration;
import ca.ontariohealth.smilecdr.support.config.FileBasedCredentials;

/**
 * @author adminuser
 *
 */
public class EMailNotifier
{
private static final Logger                 logr        = LoggerFactory.getLogger( EMailNotifier.class );

private              Configuration          appConfig   = null;
private              DateTimeFormatter      tsFormatter = null;
private              LocalDateTime          emailedAt   = null;

private              String                 emailSrvr   = null;
private              String                 emailPort   = null;
private              String                 emailCreds  = null;
private              BasicAuthCredentials   creds       = null;

private              String                 templateNm  = null;
private              String                 emailFrom   = null;
private              String                 addrTo      = null;
private              String                 addrCC      = null;
private              String                 addrBCC     = null;
private              String                 subject     = null;
private              String                 bodyFileNm  = null;
private              String                 body        = null;




public static void sendEMail( Configuration         appCfg,
                              String                requestedTemplateNm,
                              DLQRecordsInterpreter dlqInterp )
{
EMailNotifier   email = new EMailNotifier( appCfg, requestedTemplateNm );

email.body = email.loadFileIntoString( email.bodyFileNm, email.templateNm, dlqInterp );
email.sendEMail();

return;
}


public static void sendEMail( Configuration appCfg,
                              String        requestedTemplateNm,
                              String        emailBody )

{
EMailNotifier   email = new EMailNotifier( appCfg, requestedTemplateNm );

email.body = emailBody;
email.sendEMail();

return;
}
        


public EMailNotifier( Configuration appCfg )
{
appConfig = appCfg;
loadTemplateItems( null );

return;
}



public EMailNotifier( Configuration appCfg, String requestedTemplateNm )
{
appConfig  = appCfg;
loadTemplateItems( requestedTemplateNm );

return;
}



private void    loadTemplateItems( String requestedTemplateNm )
{
templateNm = requestedTemplateNm;
if ((templateNm == null) || (templateNm.length() == 0))
    templateNm = appConfig.configValue( ConfigProperty.EMAIL_NEWDLQ_TEMPLATE_NAME );

tsFormatter = DateTimeFormatter.ofPattern( appConfig.configValue( ConfigProperty.TIMESTAMP_FORMAT ) );

emailSrvr   = appConfig.configValue( ConfigProperty.EMAIL_SERVER );
emailPort   = appConfig.configValue( ConfigProperty.EMAIL_SMPT_PORT );
emailCreds  = appConfig.configValue( ConfigProperty.EMAIL_CREDENTIALS_FILE );

creds = new FileBasedCredentials( emailCreds );

emailFrom   = appConfig.configValue( ConfigProperty.EMAIL_FROM_ADDR.propertyName()    + "." + templateNm, "" );
addrTo      = appConfig.configValue( ConfigProperty.EMAIL_TO_ADDRS.propertyName()     + "." + templateNm, "" );
addrCC      = appConfig.configValue( ConfigProperty.EMAIL_CC_ADDRS.propertyName()     + "." + templateNm, "" );
addrBCC     = appConfig.configValue( ConfigProperty.EMAIL_BCC_ADDRS.propertyName()    + "." + templateNm, "" );
subject     = appConfig.configValue( ConfigProperty.EMAIL_SUBJECT.propertyName()      + "." + templateNm, "" );
 
bodyFileNm  = appConfig.configValue( ConfigProperty.EMAIL_BODY_FILE_NM.propertyName() + "." + templateNm, "" );

emailedAt   = LocalDateTime.now();

return;
}


public void sendEMail()
{
logr.debug( "Entering: sendEMail" );

Properties  emailProps = new Properties();
emailProps.put( "mail.smtp.auth", "true" );
emailProps.put( "mail.smtp.host", emailSrvr );
emailProps.put( "mail.smtp.port", emailPort );

try
    {
    Authenticator auth    = new SMTPAuthenticator( creds.username(), creds.password() );
    Session       session = Session.getInstance( emailProps, auth );
    Message       message = new MimeMessage( session );

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
    message.setContent( body, "text/html" );

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




private String  loadFileIntoString( String                  fileNm, 
                                    String                  template, 
                                    DLQRecordsInterpreter   dlqInterp )
{
logr.debug("Entering: loadFileIntoString");
String      rtrn  = "";

try (InputStream iStrm = ClassLoader.getSystemResourceAsStream( fileNm ) )
    {
    StringBuilder   content      = new StringBuilder();
    BufferedReader  rdr          = new BufferedReader( new InputStreamReader(iStrm) );
    Boolean         inclComments = appConfig.configBool(    ConfigProperty.EMAIL_INCL_HASHTAG_LINES.propertyName() 
                                                          + "." 
                                                          + template, 
                                                          Boolean.FALSE );

    String  line = null;
    while ((line = rdr.readLine()) != null)
        {
        if ((line != null) && 
            (inclComments || !line.strip().startsWith( "#")))
            {
            String  expandedLine = expandVariables( line, dlqInterp );
            content.append( expandedLine + System.lineSeparator() );
            }
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



private String      expandVariables( String line, DLQRecordsInterpreter dlqInterp )
{
String  expandedLine = line;

expandedLine = expandedLine.replace( "{{Now}}",                 emailedAt.format( tsFormatter ) );
expandedLine = expandedLine.replace( "{{DLQRecordCount}}",      Integer.toString( dlqInterp.recordCount() ) );
expandedLine = expandedLine.replace( "{{DLQRecordsCSVHeader}}", dlqInterp.csvHeaders() );
expandedLine = expandedLine.replace( "{{DLQRecordsAsCSV}}",     dlqInterp.asCSVReport() );
        
return expandedLine;
}



private class SMTPAuthenticator extends Authenticator 
{
String  emailUserID     = null;
String  userPassword    = null;

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