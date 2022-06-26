/**
 * 
 */
package ca.ontariohealth.smilecdr.support.email;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ontariohealth.smilecdr.support.config.ConfigProperty;
import ca.ontariohealth.smilecdr.support.config.Configuration;

/**
 * @author adminuser
 *
 */
public class EMailConnectionDetails
{
private static final Logger     logr                    = LoggerFactory.getLogger( EMailConnectionDetails.class );

private Configuration           appConfig               = null;
private String                  emailNotifierClassNm    = null;
private String                  smtpServer              = null;
private Integer                 smtpPort                = null;
private Boolean                 requiresSSL             = Boolean.FALSE;
private String                  credentialsFileNm       = null;


public  EMailConnectionDetails( Configuration appCfg )
{
if (appCfg == null)
    throw new IllegalArgumentException( EMailTemplateDetails.class.getSimpleName() + " constructor parameters must not be null" );


logr.debug( "Entering: {} Constructor", EMailConnectionDetails.class.getSimpleName() );

appConfig = appCfg;
loadConnectionDetails();

logr.debug( "Exiting: {} Constructor", EMailConnectionDetails.class.getSimpleName() );
return;
}



public  String  emailNotifierClassName()
{
return emailNotifierClassNm;
}



public  String  smtpServerName()
{
return smtpServer;
}



public  Integer smtpPortNumber()
{
return smtpPort;
}



public  Boolean requiresSSLConnection()
{
return requiresSSL;
}



public  String  credentialsFileName()
{
return credentialsFileNm;
}




private void    loadConnectionDetails()
{
emailNotifierClassNm = appConfig.configValue( ConfigProperty.EMAIL_NOTIFIER_CLASS_NAME );
smtpServer           = appConfig.configValue( ConfigProperty.EMAIL_SERVER );
smtpPort             = appConfig.configInt(   ConfigProperty.EMAIL_SMTP_PORT );
requiresSSL          = appConfig.configBool(  ConfigProperty.EMAIL_REQUIRE_SSL_CONNECTION );
credentialsFileNm    = appConfig.configValue( ConfigProperty.EMAIL_CREDENTIALS_FILE );

return;
}
}
