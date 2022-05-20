/**
 * 
 */
package ca.ontariohealth.smilecdr.dlqwatcher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ontariohealth.smilecdr.support.config.ConfigProperty;
import ca.ontariohealth.smilecdr.support.config.Configuration;


/**
 * @author adminuser
 *
 */
public class DLQPollingThread extends Thread 
{
private static final 	Logger			logr 				= LoggerFactory.getLogger( DLQPollingThread.class );
private static final	String			SIMPLE_CLS_NM       = DLQPollingThread.class.getSimpleName();
private static final    Integer     	DEFAULT_INTERVAL	= 500;
private static final    String          DEFAULT_DLQ_TOPIC   = "KAFKA.DLQ";

private					Configuration	appConfig			= null;
private					boolean			continueRunning 	= true;
private					Integer			pollingInterval     = null;
private					Duration		pollingDuration 	= null;
private					Long			startTime       	= System.currentTimeMillis();
private					Long			maxRunTime			= null;

private					Consumer<String, String>		dlqConsumer = null;
private                 String                  		dlqTopic    = null;
private					ConsumerRecords<String, String> dlqRecords	= null;	
private					DLQRecordsInterpreter			dlqInterp   = null;


public	DLQPollingThread( Configuration appCfg )
{
super();
	
if (appCfg == null)
	throw new IllegalArgumentException( "Application COnfiguration parameter must not be null." );

appConfig 		   = appCfg;

pollingInterval    = appConfig.configInt( ConfigProperty.KAFKA_CONSUMER_POLL_INTERVAL,
									      DEFAULT_INTERVAL );
pollingDuration    = Duration.ofMillis( pollingInterval );

dlqTopic           = appConfig.configValue( ConfigProperty.KAFKA_DLQ_TOPIC_NAME, DEFAULT_DLQ_TOPIC );

return;
}




@Override
public void run() 
{
logr.debug( "Entering: {}.run", SIMPLE_CLS_NM );

dlqConsumer = createConsumer();

logr.debug( "Subcribing to DLQ Topic: {}", dlqTopic );
dlqConsumer.subscribe( Collections.singletonList( dlqTopic ) );

while (continueRunning)
	{
	if (maxRunTime != null)
		{
		Long	crntTime = System.currentTimeMillis();
		
		continueRunning = (crntTime <= (startTime + maxRunTime));
		}
	
	if (continueRunning)
		{
		dlqRecords = dlqConsumer.poll( pollingDuration );
		
		if ((dlqRecords != null) && (dlqRecords.count() > 0))
			{
			logr.debug( "Received {} DLQ Record(s).", dlqRecords.count() );
			
			dlqInterp = new DLQRecordsInterpreter( dlqRecords, appConfig );
			sendEMail( appConfig.configValue( ConfigProperty.EMAIL_TEMPLATE_NAME ));
			
			/*
			for (ConsumerRecord<String, String> crnt : rcrds)
				{
				if (crnt != null)
					processReceivedCommand( crnt.value() );
				}
			*/
			}
		
		dlqConsumer.commitAsync();
		}
	}


logr.debug( "Leaving: {}.run", SIMPLE_CLS_NM );
return;
}



private Consumer<String, String>	createConsumer()
{
Consumer<String, String>  rtrn  = null;
Properties				  props = new Properties();

String		groupID 		 	= appConfig.configValue( ConfigProperty.KAFKA_DLQ_GROUP_ID,
		                                              	 appConfig.getApplicationName().appName() + ".dlq.group.id" );

String		bootstrapServers 	= appConfig.configValue( ConfigProperty.BOOTSTRAP_SERVERS );
String      keyDeserializer  	= Configuration.KAFKA_KEY_DESERIALIZER_CLASS_NAME;
String      valueDeserializer	= Configuration.KAFKA_VALUE_DESERIALIZER_CLASS_NAME;

logr.debug( "   Group ID:           {}", groupID );
logr.debug( "   Bootstrap Servers:  {}", bootstrapServers );
logr.debug( "   Key Deserializer:   {}", keyDeserializer );
logr.debug( "   Value Deserializer: {}", valueDeserializer );

props.put( ConsumerConfig.GROUP_ID_CONFIG,                 groupID );
props.put( ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,        bootstrapServers );
props.put( ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,   keyDeserializer );
props.put( ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer );

rtrn = new KafkaConsumer<>( props );

return rtrn;	
}



public void indicateToStopThread() 
{
logr.debug( "Flagging thread to stop running." );
this.continueRunning = false;
}


public Integer getPollingInteval() 
{
return pollingInterval;
}


public void setPollingInteval( Integer newInterval ) 
{
if ((newInterval != null) && (newInterval >= 0))
	{
	pollingInterval = newInterval;
	pollingDuration = Duration.ofMillis( newInterval );
	}

else
	{
	pollingInterval = DEFAULT_INTERVAL;
	pollingDuration = Duration.ofMillis( DEFAULT_INTERVAL );
	}

logr.debug( "Polling Interval set to {} milliseconds", pollingInterval );
return;
}


public Long getMaxRunTime()
{
return maxRunTime;
}


public void setMaxRunTime(Long maxRunTime)
{
this.maxRunTime = maxRunTime;
}



private void sendEMail( String requestedTemplate )
{
logr.debug( "Entering: sendEMail" );

String		emailSrvr   = appConfig.configValue( ConfigProperty.EMAIL_SERVER );
String      emailPort   = appConfig.configValue( ConfigProperty.EMAIL_SMPT_PORT );
String      emailUser   = appConfig.configValue( ConfigProperty.EMAIL_USER_ID );
String      emailPass   = appConfig.configValue( ConfigProperty.EMAIL_PASSWORD );

String      templateNm  = requestedTemplate;

if ((templateNm == null) || (templateNm.length() == 0))
	templateNm = appConfig.configValue( ConfigProperty.EMAIL_TEMPLATE_NAME );


String      emailFrom   = appConfig.configValue( ConfigProperty.EMAIL_FROM_ADDR.propertyName() + "." + templateNm, "" );
String		addrTo  	= appConfig.configValue( ConfigProperty.EMAIL_TO_ADDRS.propertyName()  + "." + templateNm, "" );
String  	addrCC  	= appConfig.configValue( ConfigProperty.EMAIL_CC_ADDRS.propertyName()  + "." + templateNm, "" );
String  	addrBCC 	= appConfig.configValue( ConfigProperty.EMAIL_BCC_ADDRS.propertyName() + "." + templateNm, "" );
String      subject     = appConfig.configValue( ConfigProperty.EMAIL_SUBJECT.propertyName()   + "." + templateNm, "" );
 
String  	bodyFileNm	= appConfig.configValue( ConfigProperty.EMAIL_BODY_FILE_NM.propertyName() + "." + templateNm, "" );

String  	body    	= loadFileIntoString( bodyFileNm, templateNm );

Properties	emailProps = new Properties();
emailProps.put( "mail.smtp.auth", "true" );
emailProps.put( "mail.smtp.host", emailSrvr );
emailProps.put( "mail.smtp.port", emailPort );

try
	{
	Authenticator auth 	  = new SMTPAuthenticator( emailUser, emailPass );
    Session 	  session = Session.getInstance( emailProps, auth );
    Message 	  message = new MimeMessage( session );

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
    message.setText( body );

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




private String	loadFileIntoString( String fileNm, String template )
{
logr.debug("Entering: loadFileIntoString");
String	    rtrn  = "";

try (InputStream iStrm = ClassLoader.getSystemResourceAsStream( fileNm ) )
	{
	StringBuilder	content 	 = new StringBuilder();
	BufferedReader	rdr     	 = new BufferedReader( new InputStreamReader(iStrm) );
	Boolean			inclComments = appConfig.configBool(    ConfigProperty.EMAIL_INCL_HASHTAG_LINES.propertyName() 
			                                              + "." 
			                                              + template, 
			                                              Boolean.FALSE );

	String	line = null;
	while ((line = rdr.readLine()) != null)
		{
		if ((line != null) && 
			(inclComments || !line.strip().startsWith( "#")))
			{
			String	expandedLine = expandVariables( line );
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



private String		expandVariables( String line )
{
String 	expandedLine = line;

expandedLine = expandedLine.replace( "{{DLQRecordCount}}", Integer.toString( dlqRecords.count() ) );
expandedLine = expandedLine.replace( "{{DLQRecordsAsCSV}}", dlqInterp.asCSVReport() );
		
return expandedLine;
}



private class SMTPAuthenticator extends Authenticator 
{
String	emailUserID		= null;
String  userPassword	= null;

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
