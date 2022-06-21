/**
 * 
 */
package ca.ontariohealth.smilecdr.support.config;

import java.time.LocalDate;

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
APP_NAME(                       "application.name" ),
APP_DESCRIPTION(                "application.desc" ),
APP_VERSION(                    "version.num" ),
BUILD_DATE(                     "build.date" ),
COPYRIGHT_HOLDER(               "copyright.holder",              "Accenture, Inc." ),
COPYRIGHT_YEAR_START(           "copyright.year.start",          String.valueOf( LocalDate.now().getYear() ) ),
COPYRIGHT_YEAR_END(             "copyright.year.end" ),

DLQ_PARSER_FQCN_CLASS(          "dlq.parser.class.name" ),

DATE_FORMAT(                    "date.format",                   "yyyy-MM-dd" ),
TIMESTAMP_FORMAT(               "timestamp.format",              "yyyy-MM-dd HH:mm:ss" ),

BOOTSTRAP_SERVERS(              "bootstrap.servers" ),
KAFKA_DEFAULT_TOPIC_RETENTION(  "kafka.default.rentention.hours","168" ),

START_DLQ_POLL_THREAD(          "start.cmd.include.dlqpoll.thread",        "true" ),
START_POLL_PARK_THREAD(         "start.cmd.include.parkpoll.thread",       "true" ),
START_DLQ_PARK_THREAD(          "start.cmd.include.parkdlqentries.thread", "true" ),

STOP_THREAD_MAX_WAIT_MILLIS(    "stop.cmd.max.wait.for.thread.millis",     "10000" ),

KAFKA_CONTROL_GROUP_ID(         "control.group.id" ),
KAFKA_DLQ_GROUP_ID(             "dlq.group.id" ),
KAFKA_DLQ_LISTER_GROUP_ID(      "dlq.lister.group.id" ),
CONTROL_TOPIC_NAME_COMMAND(     "topic.name.command" ),
CONTROL_TOPIC_NAME_RESPONSE(    "topic.name.response" ),
KAFKA_CONSUMER_POLL_INTERVAL(   "consumer.poll.interval",              "500" ),
KAFKA_DLQ_TOPIC_NAME(           "kafka.dlq.topic.name",                "KAFKA.DLQ" ),
KAFKA_DLQ_RETENTION_HOURS(      "dlq.entry.retention.hours",           "168" ),  // 7 days
DLQ_PARK_ENTRIES_AFTER_HOURS(   "dlq.park.after.hours",                "150" ),  // 6.25 days
DLQ_PARK_CHECK_INTERVAL_MINS(   "dlq.frequency.mins.to.park.entries",  "120" ),

KAFKA_PARK_WATCHER_GROUP_ID(    "kafka.park.watcher.group.id"),
KAFKA_PARK_LISTER_GROUP_ID(     "kafka.park.lister.group.id" ),
KAFKA_PARK_TOPIC_NAME(          "kafka.park.topic.name",         "KAFKA.PARK" ),
KAFKA_PARK_RETENTION_HOURS(     "parked.entry.retention.hours",  "Forever" ),

QUIT_AFTER_MILLIS(              "quit.after.millis" ),
PAUSE_BEFORE_WAIT_FOR_RESPONSE( "pause.before.response",         "0" ),
RESPONSE_WAIT_MILLIS(           "response.wait.millis",          "30000" ),

EMAIL_SERVER(                   "email.server.smtp" ),
EMAIL_SMPT_PORT(                "email.server.smtp.port",        "25" ),
EMAIL_CREDENTIALS_FILE(         "email.credentials.file" ),

EMAIL_NEWDLQ_TEMPLATE_NAME(     "email.newdlq.template.nm" ),
EMAIL_NEWPARK_TEMPLATE_NAME(    "email.newpark.template.nm" ),
EMAIL_DLQLIST_TEMPLATE_NAME(    "email.dlqlist.template.nm" ),
EMAIL_PARKLIST_TEMPLATE_NAME(   "email.parklist.template.nm" ),

EMAIL_FROM_ADDR(                "email.from" ),
EMAIL_TO_ADDRS(                 "email.to" ),
EMAIL_CC_ADDRS(                 "email.cc",                      "" ),
EMAIL_BCC_ADDRS(                "email.bcc",                     "" ),
EMAIL_SUBJECT(                  "email.subject" ),
EMAIL_INCL_HASHTAG_LINES(       "email.body.incl.hashtag.lines", "false" ),
EMAIL_BODY_FILE_NM(             "email.body.file.nm" );


	
/**
 * The internal property base name for this predefined configuration
 * property. This property name is possibly further decorated with an
 * application name and environment name to come up with a specific property
 * in the configuration file to load for this predefined property item.
 * 
 * @see Configuration
 * 
 */
private final String	propertyName;


/**
 * Holds the default value for the predefined configuration property (if any).
 * 
 */
private final String	defaultValue;



/**
 * Private constructor for a ConfigProperty element.  This form of the
 * constructor does not allow for a hard coded default property value
 * for a configuration property if the configuration property item could
 * not be found in the configuration file (see: {@link Configuration}.
 * 
 * @param propNm			The base name of the configuration property to be
 *                          used as the looking up in the configuration 
 *                          properties file see {@link Configuration}
 *
 * @throws IllegalArgumentException  The property name parameter is 
 * 									 <code>null</code> or zero length.
 * 
 * @see Configuration
 */
private ConfigProperty( String propNm ) throws IllegalArgumentException
{
if ((propNm == null) || (propNm.length() == 0))
	throw new IllegalArgumentException( "Property Name must not be null or zero length." );

propertyName = propNm;
defaultValue = null;

return;	
}




/**
 * Private constructor for a ConfigProperty element allowing for a hard coded
 * default value for a property that could not be found in the configuration
 * file.
 * 
 * @param propNm			The base name of the configuration property to be
 *                          used as the looking up in the configuration 
 *                          properties file see {@link Configuration}
 * @param hardCodedDefault	Sets the hard coded default value for the
 *                          configuration property.  This hard coded default
 *                          value is used when the corresponding property
 *                          can not be found in the application configuration
 *                          file.  This hard coded default value may be
 *                          <code>null</code> and would be interpreted as
 *                          the default value.  If you do not want to have
 *                          a hard coded default value, use the other form
 *                          of the constructor.
 * 
 * @throws IllegalArgumentException  The property name parameter is 
 * 									 <code>null</code> or zero length.
 * 
 * @see ConfigProperty#ConfigProperty(String)
 * @see Configuration
 */
private ConfigProperty( String propNm, String hardCodedDefault )
{
if ((propNm == null) || (propNm.length() == 0))
	throw new IllegalArgumentException( "Property Name must not be null or zero length." );

propertyName = propNm;
defaultValue = hardCodedDefault;

return;
}


/**
 * Retrieve the property base name for this predefined configuration property.
 * This base name may be decorated with an application name and/or an
 * environment name in order to find a property value associated with this
 * predefined configuration item.
 * 
 * @return  The predefined property base name.  Guaranteed to be not-null
 *          and non-zero length.
 *
 * @see Configuration
 * 
 */
public final String		propertyName()
{
return propertyName;
}



/**
 * Indicates whether the enumeration value has a hard coded default.
 * 
 * @return <code>true</code>  A hard coded default value exists for the
 *                            predefined configuration property.<p>
 *         <code>false</code> No hard coded default value exists for the
 *                            predefined configuration property.
 *
 * @see ConfigProperty#ConfigProperty(String, String)
 * @see ConfigProperty#hardCodedDefaultValue()
 * @see ConfigProperty#defaultValue
 * 
 */
public	boolean		hasHardCodedDefault()
{
boolean rtrn = defaultValue != null;

return rtrn;
}




/**
 * Retrieve the hard-coded default value associated with the configuration
 * property (if any).
 * <p>
 * In order to have a hard coded default, this enumeration constant <b>must</b>
 * have been created with the two argument form of the constructor.  If it was
 * created with the single argument form, the exception will get thrown.
 * 
 * @return The hard coded default value for the configuration property<p>
 *         This hard-coded default may be <code>null</code>.
 *
 * @throws NullPointerException  There is no hard coded default value for this
 *                               predefined property.  This call should be
 *                               guarded with calls to <code>hasHardCodedDefault</code>.
 * 
 * @see ConfigProperty#ConfigProperty(String, String)
 * @see ConfigProperty#hasHardCodedDefault()
 * @see ConfigProperty#defaultValue
 * 
 */
public final String		hardCodedDefaultValue()
				throws NullPointerException
				
{
return defaultValue;
}
}
