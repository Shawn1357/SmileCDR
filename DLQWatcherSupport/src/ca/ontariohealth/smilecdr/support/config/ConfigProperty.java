/**
 * 
 */
package ca.ontariohealth.smilecdr.support.config;

/**
 * The list of predefined configuration entries.
 * Other ad-hoc properties may be defined simply by referring to them
 * by their name rather than being forced to this set of configuration
 * values.
 * 
 * @author adminuser
 *
 */
public enum ConfigProperty 
{
BOOTSTRAP_SERVERS( "bootstrap.servers" ),
KAFKA_GROUP_ID( "group.id" ),
KEY_SERIALIZER( "key.serializer" ),
KEY_DESERIALIZER( "key.deserialier" ),
VALUE_SERIALIZER( "value.serializer" ),
VALUE_DESERIALIZER( "value.deserializer" ),
DEFAULT_CONTROL_TOPIC_NAME( "topic.name.default" ),
	
EMAIL_SERVER( "email.server" ),
EMAIL_USER_ID( "email.userid" ),
EMAIL_PASSWORD( "email.password" ),

EMAIL_TEMPLATE( "email.template" ),
EMAIL_FROM_ADDR( "email.from" ),
EMAIL_TO_ADDRS( "email.to" ),
EMAIL_CC_ADDRS( "email.cc" ),
EMAIL_BCC_ADDRS( "email.bcc" ),
EMAIL_SUBJECT( "email.subject" ),
EMAIL_BODY_FILE_NM( "email.body" );


	
private final String	propertyName;



private ConfigProperty( String propNm )
{
if ((propNm == null) || (propNm.length() == 0))
	throw new IllegalArgumentException( "Property Name must not be null or zero length." );

propertyName = propNm;
return;
}


public final String		propertyName()
{
return propertyName;
}

}
