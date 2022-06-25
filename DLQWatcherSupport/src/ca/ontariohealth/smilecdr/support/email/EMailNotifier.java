/**
 * 
 */
package ca.ontariohealth.smilecdr.support.email;

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
private	Configuration	appCfg = null;



public	static	EMailNotifier	getEmailer( Configuration appConfig )
{
EMailNotifier	rtrn = null;

if (appConfig == null)
	throw new IllegalArgumentException( "appConfig parameter must not be null." );

String	emailerClassName = appConfig.configValue( ConfigProperty.EMAIL_NOTIFIER_CLASS_NAME );

return rtrn;
}




public abstract void	sendEMail( DLQResponseContainer		resp,
		                           Configuration            appConfig,
								   String					emailTemplateNm,
								   DLQRecordsInterpreter	rcrds );
}
