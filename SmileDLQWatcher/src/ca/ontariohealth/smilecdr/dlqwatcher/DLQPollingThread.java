/**
 * 
 */
package ca.ontariohealth.smilecdr.dlqwatcher;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;


import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ontariohealth.smilecdr.support.commands.DLQRecordsInterpreter;
import ca.ontariohealth.smilecdr.support.commands.EMailNotifier;
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
		ConsumerRecords<String,String> dlqRecords = dlqConsumer.poll( pollingDuration );
		
		if ((dlqRecords != null) && (dlqRecords.count() > 0))
			{
			logr.debug( "Received {} DLQ Record(s).", dlqRecords.count() );
			
			dlqInterp = new DLQRecordsInterpreter( dlqRecords, appConfig );
			EMailNotifier.sendEMail( appConfig, appConfig.configValue( ConfigProperty.EMAIL_NEWDLQ_TEMPLATE_NAME ), dlqInterp );
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



}
