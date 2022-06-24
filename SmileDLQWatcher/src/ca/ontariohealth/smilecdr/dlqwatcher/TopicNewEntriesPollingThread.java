/**
 * 
 */
package ca.ontariohealth.smilecdr.dlqwatcher;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

import javax.mail.MessagingException;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ontariohealth.smilecdr.support.commands.DLQCommandOutcome;
import ca.ontariohealth.smilecdr.support.commands.DLQRecordsInterpreter;
import ca.ontariohealth.smilecdr.support.commands.DLQResponseContainer;
import ca.ontariohealth.smilecdr.support.commands.EMailNotifier;
import ca.ontariohealth.smilecdr.support.commands.ProcessingMessage;
import ca.ontariohealth.smilecdr.support.commands.ProcessingMessageCode;
import ca.ontariohealth.smilecdr.support.config.ConfigProperty;
import ca.ontariohealth.smilecdr.support.config.Configuration;

/**
 * @author adminuser
 *
 */
public abstract class TopicNewEntriesPollingThread extends MyThread
{
private static final    Logger                          logr                = LoggerFactory.getLogger( TopicNewEntriesPollingThread.class );

private static final    Integer                         DEFAULT_INTERVAL    = 500;

private                 Integer                         pollingInterval     = null;
private                 Duration                        pollingDuration     = null;

private                 String                          dlqTopic            = null;

private                 Consumer<String, String>        dlqConsumer         = null;
private                 DLQRecordsInterpreter           dlqInterp           = null;




public  TopicNewEntriesPollingThread( Configuration appCfg )
{
super( appCfg );
    
pollingInterval    = appConfig().configInt( ConfigProperty.KAFKA_CONSUMER_POLL_INTERVAL );
pollingDuration    = Duration.ofMillis( pollingInterval );

dlqTopic           = kafkaTopicToWatch();

return;
}



@Override
public void run() 
{
logr.debug( "Entering: {}.run", this.getClass().getSimpleName() );

super.run();

dlqConsumer = createConsumer();

logr.debug( "Subcribing to DLQ Topic: {}", dlqTopic );
dlqConsumer.subscribe( Collections.singletonList( dlqTopic ) );

while (!threadIndicatedToStop())
    {
    if (scheduledEndTimeElapsed())
        indicateToStopThread();
    
    else
        {
        ConsumerRecords<String,String> dlqRecords = dlqConsumer.poll( pollingDuration );
        
        if ((dlqRecords != null) && (dlqRecords.count() > 0))
            {
            logr.debug( "Received {} DLQ Record(s).", dlqRecords.count() );
            
            dlqInterp = new DLQRecordsInterpreter( dlqRecords, appConfig() );
            sendEMail( null, notificationEmailTempateName(), dlqInterp );
            }
        
        dlqConsumer.commitAsync();
        }
    }


logr.debug( "Leaving: {}.run", this.getClass().getSimpleName() );
return;
}



private void sendEMail( DLQResponseContainer	resp,
						String					emailTemplateNm,
						DLQRecordsInterpreter	rcrds )

{
try
	{
	EMailNotifier.sendEMail( appConfig(), emailTemplateNm, rcrds );
	}

catch (MessagingException e) 
	{
	logr.error( "Unable to send email:", e );
	if (resp != null)
		{
		String msg = e.getMessage();

		resp.setOutcome( DLQCommandOutcome.ERROR );
		resp.addProcessingMessage( new ProcessingMessage( ProcessingMessageCode.DLQW_0007, appConfig(), appConfig().configValue( ConfigProperty.EMAIL_SERVER ) ) );
		resp.addProcessingMessage( new ProcessingMessage( ProcessingMessageCode.DLQW_9999, appConfig(), e.getClass().getSimpleName() ));
		if ((msg != null) && (msg.strip().length() > 0))
			resp.addProcessingMessage( new ProcessingMessage( ProcessingMessageCode.DLQW_9999, appConfig(), msg.strip() ) );
		}
	}

return;	
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



private Consumer<String, String>    createConsumer()
{
Consumer<String, String>  rtrn  = null;
Properties                props = new Properties();

String      groupID             = kafkaConsumerGroupName();

String      bootstrapServers    = appConfig().configValue( ConfigProperty.BOOTSTRAP_SERVERS );
String      keyDeserializer     = Configuration.KAFKA_KEY_DESERIALIZER_CLASS_NAME;
String      valueDeserializer   = Configuration.KAFKA_VALUE_DESERIALIZER_CLASS_NAME;

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


public abstract String  kafkaTopicToWatch();
public abstract String  kafkaConsumerGroupName();
public abstract String  notificationEmailTempateName();
}
