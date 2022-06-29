/**
 * 
 */
package ca.ontariohealth.smilecdr.support.email;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.format.DateTimeFormatter;

import javax.mail.internet.AddressException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ontariohealth.smilecdr.support.MyInstant;
import ca.ontariohealth.smilecdr.support.commands.DLQRecordsInterpreter;
import ca.ontariohealth.smilecdr.support.commands.DLQResponseContainer;
import ca.ontariohealth.smilecdr.support.config.ConfigProperty;
import ca.ontariohealth.smilecdr.support.config.Configuration;

/**
 * @author windowsadmin
 *
 */
public abstract class EMailNotifier 
{
private static final Logger         logr                = LoggerFactory.getLogger( EMailNotifier.class );

protected   Configuration	        appConfig           = null;
private     EMailNotificationType   emailNotifictnType  = null;
private     DateTimeFormatter       tsFormatter         = null;
private     MyInstant               emailedAt           = null;

private     EMailConnectionDetails  connectionDetails   = null;
private     EMailTemplateDetails    templateDetails     = null;
private     ConfigProperty          emailTemplateProp   = null;



/**
 * Generate an EMailNotifier Instance based on the Fully Qualified Class Name
 * found in the supplied application configuration.
 * <p>
 * This routine will use <code>ConfigProperty.EMAIL_NOTIFIER_CLASS_NAME</code>
 * to look up the class name to load from the application configuration.
 * <p> 
 * @param appConfig The application configuration to get the EMailer class
 *                  name from.
 * @return          A new instance of the the named EMailNotifier class.
 * 
 * @see     ConfigProperty.EMAIL_NOTIFIER_CLASS_NAME
 * 
 */
public	static	EMailNotifier	getEmailer( Configuration appConfig, EMailNotificationType notificationType )
{
EMailNotifier	rtrn = null;

if (appConfig == null)
	throw new IllegalArgumentException( "appConfig parameter must not be null." );

String	emailerClassName = appConfig.configValue( ConfigProperty.EMAIL_NOTIFIER_CLASS_NAME );

if ((emailerClassName != null) && (emailerClassName.strip().length() > 0))
    {
    try
        {
        Class<?>        clazz = Class.forName( emailerClassName );
        Constructor<?>  ctor  = clazz.getConstructor( Configuration.class, EMailNotificationType.class );

        rtrn = (EMailNotifier) ctor.newInstance( new Object[] { appConfig, notificationType } );
        }

    catch (ClassNotFoundException   | 
           NoSuchMethodException    |
           SecurityException        |
           IllegalAccessException   |
           InstantiationException   |
           IllegalArgumentException |
           InvocationTargetException e)
       
        {
        throw new IllegalArgumentException( "Unable to create a class of instance: " + emailerClassName + " (which extends " + EMailNotifier.class.getName() + ")", e );
        }
    }

return rtrn;
}



public static   void    sendEMail( Configuration            appConfig,
                                   EMailNotificationType    notificationType,
                                   DLQResponseContainer     resp,
                                   DLQRecordsInterpreter    rcrds )
{
EMailNotifier   emailer = EMailNotifier.getEmailer( appConfig, notificationType );
emailer.sendEMail( resp, rcrds );
return;
}




public EMailNotifier( Configuration appCfg, EMailNotificationType notificationType ) 
            throws AddressException
{
if ((appCfg == null) || (notificationType == null))
    throw new IllegalArgumentException( EMailNotifier.class.getSimpleName() + " constructor parameters must not be null." );

appConfig          = appCfg;
emailNotifictnType = notificationType;
emailTemplateProp  = emailNotifictnType.templateNameConfigProperty();

emailedAt         = MyInstant.now();
tsFormatter       = DateTimeFormatter.ofPattern( appConfig.configValue( ConfigProperty.TIMESTAMP_FORMAT ) );

connectionDetails = new EMailConnectionDetails( appConfig );
templateDetails   = new EMailTemplateDetails( appConfig, emailTemplateProp );

return;
}




public MyInstant    emailedAtTimestamp()
{
return emailedAt;
}




public abstract void	sendEMail( DLQResponseContainer		resp,
								   DLQRecordsInterpreter	rcrds );





public EMailNotificationType    emailNotificationType()
{
return emailNotifictnType;
}




public EMailConnectionDetails   emailConnectionDetails()
{
return connectionDetails;
}




public EMailTemplateDetails     emailTemplateDetails()
{
return templateDetails;
}




protected String  loadFileIntoString( DLQRecordsInterpreter   dlqInterp )
{
logr.debug("Entering: loadFileIntoString");

String      rtrn         = "";
String      fileNm       = templateDetails.bodyTemplateFileName();
Boolean     inclComments = templateDetails.inclBodyCommentLines();

try (InputStream iStrm = ClassLoader.getSystemResourceAsStream( fileNm ) )
    {
    StringBuilder   content      = new StringBuilder();
    BufferedReader  rdr          = new BufferedReader( new InputStreamReader(iStrm) );

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




protected String      expandVariables( String line )
{
return expandVariables( line, null );
}




protected String      expandVariables( String line, DLQRecordsInterpreter dlqInterp )
{
String  expandedLine = line;

expandedLine = expandedLine.replace( "{{Now}}",                 emailedAt.asLocalDateTime().format( tsFormatter ) );

expandedLine = expandedLine.replace( "{{EnvironmentName}}",     appConfig.getEnvironmentName() != null ? appConfig.getEnvironmentName().envName() : "" );
expandedLine = expandedLine.replace( "{{InstanceName}}",        appConfig.getInstanceName()    != null ? appConfig.getInstanceName().instName()   : "" );
expandedLine = expandedLine.replace( "{{AllowedTimeOnDLQ}}",    appConfig.configValue( ConfigProperty.DLQ_PARK_ENTRIES_AFTER_HOURS, "unknown" ));
expandedLine = expandedLine.replace( "{{DLQPurgeTime}}",        appConfig.configValue( ConfigProperty.KAFKA_DLQ_RETENTION_HOURS, "unknown" ) );
expandedLine = expandedLine.replace( "{{ParkingLotTopicName}}", appConfig.configValue( ConfigProperty.KAFKA_PARK_TOPIC_NAME, "unknown" ) );
expandedLine = expandedLine.replace( "{{ParkingLotPurgeTime}}", appConfig.configValue( ConfigProperty.KAFKA_PARK_RETENTION_HOURS, "unknown" ) );

if (dlqInterp != null)
    {
    expandedLine = expandedLine.replace( "{{RecordCount}}",      Integer.toString( dlqInterp.recordCount() ) );
    expandedLine = expandedLine.replace( "{{RecordsCSVHeader}}", dlqInterp.csvHeaders() );
    expandedLine = expandedLine.replace( "{{RecordsAsCSV}}",     dlqInterp.asCSVReport() );
    }

else
    {
    expandedLine = expandedLine.replace( "{{RecordCount}}",      String.valueOf( 0 ) );
    expandedLine = expandedLine.replace( "{{RecordsCSVHeader}}", "" );
    expandedLine = expandedLine.replace( "{{RecordsAsCSV}}",     "" );
    }

return expandedLine;
}




}
