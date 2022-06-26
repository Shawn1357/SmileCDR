/**
 * 
 */
package ca.ontariohealth.smilecdr.support.email;

import ca.ontariohealth.smilecdr.support.config.ConfigProperty;

/**
 * @author adminuser
 *
 */
public enum EMailNotificationType
{
NEW_DLQ_ENTRIES(  ConfigProperty.EMAIL_NEWDLQ_TEMPLATE_NAME ),
ALL_DLQ_ENTRIES(  ConfigProperty.EMAIL_DLQLIST_TEMPLATE_NAME ),
NEW_PARK_ENTRIES( ConfigProperty.EMAIL_NEWPARK_TEMPLATE_NAME ),
ALL_PARK_ENTRIES( ConfigProperty.EMAIL_PARKLIST_TEMPLATE_NAME ),
TEST_EMAIL(       ConfigProperty.EMAIL_TESTEMAIL_TEMPLATE_NAME );


private ConfigProperty  cfgProp = null;

private EMailNotificationType( ConfigProperty configProperty )
{
cfgProp = configProperty;
return;
}



public ConfigProperty   templateNameConfigProperty()
{
return cfgProp;
}
}
