/**
 * 
 */
package ca.ontariohealth.smilecdr.support.config;

import java.time.LocalDate;
import java.util.Optional;

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

DATE_FORMAT(                    "date.format",                   "yyyy-MM-dd" ),
TIMESTAMP_FORMAT(               "timestamp.format",              "yyyy-MM-dd HH:mm:ss" ),

BOOTSTRAP_SERVERS(              "bootstrap.servers" ),
KAFKA_CONTROL_GROUP_ID(         "control.group.id" ),
KAFKA_DLQ_GROUP_ID(             "dlq.group.id" ),
KAFKA_DLQ_LISTER_GROUP_ID(      "dlq.lister.group.id" ),
CONTROL_TOPIC_NAME_COMMAND(     "topic.name.command" ),
CONTROL_TOPIC_NAME_RESPONSE(    "topic.name.response" ),
KAFKA_CONSUMER_POLL_INTERVAL(   "consumer.poll.interval",        "500" ),
KAFKA_DLQ_TOPIC_NAME(           "kafka.dlq.topic.name",          "KAFKA.DLQ" ),

QUIT_AFTER_MILLIS(              "quit.after.millis" ),
PAUSE_BEFORE_WAIT_FOR_RESPONSE( "pause.before.response",         "0" ),
RESPONSE_WAIT_MILLIS(           "response.wait.millis",          "30000" ),

EMAIL_SERVER(                   "email.server.smtp" ),
EMAIL_SMPT_PORT(                "email.server.smtp.port",        "25" ),
EMAIL_CREDENTIALS_FILE(         "email.credentials.file" ),

EMAIL_TEMPLATE_NAME(            "email.template.nm" ),
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
private final Optional<String>	defaultValue;



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
defaultValue = Optional.ofNullable( hardCodedDefault );

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
return defaultValue.get();
}
}
