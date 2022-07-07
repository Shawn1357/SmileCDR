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
import ca.ontariohealth.smilecdr.support.config.StandardExpansionVariable;

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

for (StandardExpansionVariable crnt : StandardExpansionVariable.values())
	{
	if (expandedLine.contains( crnt.expansionVariable() ))
		{
		switch (crnt)
			{
			case	NOW:
				expandedLine = expandedLine.replace( crnt.expansionVariable(), emailedAt.asLocalDateTime().format( tsFormatter ) );
				break;

			case	ENV_NAME_VAR:
				expandedLine = expandedLine.replace( crnt.expansionVariable(), appConfig.getEnvironmentName() != null ? appConfig.getEnvironmentName().envName() : "" );
				break;

			case	INST_NAME_VAR:
				expandedLine = expandedLine.replace( crnt.expansionVariable(), appConfig.getInstanceName()    != null ? appConfig.getInstanceName().instName()   : "" );
				break;

			case	DLQ_TOPIC:
				expandedLine = expandedLine.replace( crnt.expansionVariable(), appConfig.configValue( ConfigProperty.KAFKA_DLQ_TOPIC_NAME, "unknown" ) );
				break;

			case	DLQ_MAX_TIME:
				expandedLine = expandedLine.replace( crnt.expansionVariable(), appConfig.configValue( ConfigProperty.DLQ_PARK_ENTRIES_AFTER_HOURS, "unknown" ));
				break;
				
			case	DLQ_PURGE_TIME:
				expandedLine = expandedLine.replace( crnt.expansionVariable(), appConfig.configValue( ConfigProperty.KAFKA_DLQ_RETENTION_HOURS, "unknown" ) );
				break;

			case	PARKING_LOT_TOPIC:
				expandedLine = expandedLine.replace( crnt.expansionVariable(), appConfig.configValue( ConfigProperty.KAFKA_PARK_TOPIC_NAME, "unknown" ));
				break;

			case	PARKING_LOT_PURGE_TIME:
				expandedLine = expandedLine.replace( crnt.expansionVariable(), appConfig.configValue( ConfigProperty.KAFKA_PARK_RETENTION_HOURS, "unknown" ));
				break;

			case	RECORD_COUNT:
				expandedLine = expandedLine.replace( crnt.expansionVariable(), dlqInterp != null ? Integer.toString( dlqInterp.recordCount() ) : "" );
				break;
					
			case	RECORDS_CSV_HEADER:
				expandedLine = expandedLine.replace( crnt.expansionVariable(), dlqInterp != null ? dlqInterp.csvHeaders() : "" );
				break;
					
			case	RECORDS_AS_CSV:
				expandedLine = expandedLine.replace( crnt.expansionVariable(), dlqInterp != null ? dlqInterp.asCSVReport() : "" );
				break;
					
			default:
				// Unhandled expansion variable. Leave it as is.
				break;
			}
		}
	}

return expandedLine;
}




}
