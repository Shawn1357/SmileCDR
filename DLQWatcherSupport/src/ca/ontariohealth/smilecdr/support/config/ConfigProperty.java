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
KAFKA_CONTROL_GROUP_ID( "control.group.id" ),
KAFKA_DLQ_GROUP_ID( "dlq.group.id" ),
DEFAULT_CONTROL_TOPIC_NAME( "topic.name.default" ),
KAFKA_CONSUMER_POLL_INTERVAL( "consumer.poll.interval" ),
KAFKA_DLQ_TOPIC_NAME( "kafka.dlq.topic.name" ),

STOP_AFTER_MILLIS( "stop.after.millis" ),

EMAIL_SERVER( "email.server" ),
EMAIL_SMPT_PORT( "email.server.smtp.port" ),
EMAIL_USER_ID( "email.userid" ),
EMAIL_PASSWORD( "email.password" ),

EMAIL_TEMPLATE( "email.template" ),
EMAIL_FROM_ADDR( "email.from" ),
EMAIL_TO_ADDRS( "email.to" ),
EMAIL_CC_ADDRS( "email.cc" ),
EMAIL_BCC_ADDRS( "email.bcc" ),
EMAIL_SUBJECT( "email.subject" ),
EMAIL_INCL_HASHTAG_LINES( "email.body.include.hashtag.lines" ),
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
