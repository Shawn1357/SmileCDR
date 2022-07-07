/**
 * 
 */
package ca.ontariohealth.smilecdr.dlqwatcher;

import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ontariohealth.smilecdr.support.MyInstant;
import ca.ontariohealth.smilecdr.support.MyThread;
import ca.ontariohealth.smilecdr.support.commands.DLQRecordsInterpreter;
import ca.ontariohealth.smilecdr.support.config.ConfigProperty;
import ca.ontariohealth.smilecdr.support.config.Configuration;
import ca.ontariohealth.smilecdr.support.kafka.KafkaProducerHelper;
import ca.ontariohealth.smilecdr.support.kafka.KafkaTopicRecordParser;

/**
 * @author adminuser
 *
 */
public class DLQParkingThread extends MyThread
{
private static final    Logger          logr                = LoggerFactory.getLogger( DLQParkingThread.class );
private static final    String          SIMPLE_CLS_NM       = DLQParkingThread.class.getSimpleName();
private static final    Integer         DEFAULT_INTERVAL    = 500;

private Integer         parkingCheckIntervalMins    = null;
private Long            parkingCheckIntervalMillis  = null;
private Integer         pollingInterval             = 500;
private Duration        pollingDuration             = null;
private String          dlqTopicName                = null;
private String          parkingTopicName            = null;
private Integer         maxTimeOnDLQHours           = null;
private Long            maxTimeOnDLQMillis          = null;

private MyInstant       nextCheckOfDLQTime          = null;

private int             rollingIndex                = 0;

private Long            lastParkedOffset            = null;

private Consumer<String, String>    dlqConsumer     = null;
private Producer<String, String>    parkProducer    = null;


public  DLQParkingThread( Configuration appCfg )
{
super( appCfg );
    
setParkingCheckIntervalMins( appConfig().configInt( ConfigProperty.DLQ_PARK_CHECK_INTERVAL_MINS ) );
setMaxHoursOnDLQ(            appConfig().configInt( ConfigProperty.DLQ_PARK_ENTRIES_AFTER_HOURS ) );
                                        
pollingInterval           = appConfig().configInt( ConfigProperty.KAFKA_CONSUMER_POLL_INTERVAL );
pollingDuration           = Duration.ofMillis( pollingInterval );

dlqTopicName              = appConfig().configValue( ConfigProperty.KAFKA_DLQ_TOPIC_NAME );
parkingTopicName          = appConfig().configValue( ConfigProperty.KAFKA_PARK_TOPIC_NAME );

return;
}



@Override
public void run() 
{
logr.debug( "Entering: {}.run", SIMPLE_CLS_NM );

super.run();

nextCheckOfDLQTime = getThreadStartTime();

dlqConsumer  = createConsumer();

Properties props = new Properties();
//props.put( "transactional.id", UUID.randomUUID().toString() );

parkProducer = KafkaProducerHelper.createProducer( appConfig(), parkingTopicName, props );
//parkProducer.initTransactions();

logr.debug( "Subcribing to DLQ Topic: {}", dlqTopicName );
dlqConsumer.subscribe( Collections.singletonList( dlqTopicName ) );


do
    {
    MyInstant   crntTime = MyInstant.now();
    
    if (nextCheckOfDLQTime.compareTo( crntTime ) <= 0)
        {
        // Scan through the DLQ Entries looking for entries that can be moved
        // to the Parking Lot Queue
        MyInstant   expiryTime = new MyInstant( crntTime.getEpochMillis() - maxTimeOnDLQMillis );
        parkExpiringDLQEntries( crntTime, expiryTime );
        
        // Done checking, now compute when we are going to check again.
        nextCheckOfDLQTime = new MyInstant( crntTime.getEpochMillis() + parkingCheckIntervalMillis );
        }
    
    else
        {
        /*
         * We haven't reached the next Check DLQ Time yet so, sleep for a bit
         * and check again.
         * 
         * We don't want to sleep until the next Check DLQ Time because we may
         * have gotten a signal to terminate the thread and don't want to have to
         * wait potentially hours (or days) for the thread to cleanly exit.
         *
         */
        
        try
            {
            Thread.sleep( pollingInterval );
            }
        
        catch (InterruptedException e)
            {
            // Nothing to do.  Continue quietly
            }
        }
    
    if ((!threadIndicatedToStop()) && (scheduledEndTimeElapsed()))
        indicateToStopThread();
    }

while (!threadIndicatedToStop());


logr.debug( "Exiting: {}.run", SIMPLE_CLS_NM );
return;
}




private void    parkExpiringDLQEntries( MyInstant crntTime, MyInstant expiryTime )
{
logr.debug( "Entering {}.parkExpiringDLQEntries", SIMPLE_CLS_NM );

boolean continueChecking = true;
int     rcrdsFound       = 0;


// Starting at the beginning of all records on the DLQ.
dlqConsumer.seekToBeginning( dlqConsumer.assignment() );
dlqConsumer.commitSync();

while (continueChecking)
    {
    // Zero or one record because we set max poll records to one when creating the consumer.
    ConsumerRecords<String,String> dlqRecords = dlqConsumer.poll( pollingDuration );  
    
    if ((dlqRecords != null) && (dlqRecords.count() > 0)) // Zero or one record
        {
        logr.debug( "Received {} DLQ Record(s) by Parking Thread.", dlqRecords.count() );
        
        
        ConsumerRecord<String,String> dlqRecord = null;
        {
        Iterator<ConsumerRecord<String,String>>   iter = dlqRecords.records(dlqTopicName ).iterator();
        if (iter.hasNext())
            dlqRecord = iter.next();
        
        
        Long  rcrdOffset = dlqRecord.offset();
        if ((lastParkedOffset != null) && (lastParkedOffset >= rcrdOffset))
            {
            logr.debug( "Parkable record has already been parked. Skipping." );
            continue;
            }
        
        else
            lastParkedOffset = rcrdOffset;
        }
        
        DLQRecordsInterpreter dlqInterp  = new DLQRecordsInterpreter( dlqRecords, appConfig() );
        boolean               commitMove = false;
        
        if (dlqInterp.recordCount() > 0)
            {
            List<KafkaTopicRecordParser> interps = dlqInterp.getInterpretations();
            for (KafkaTopicRecordParser crntInterp : interps)  // There should be exactly one entry because we set max poll records to one when creating the consumer.
                {
                MyInstant   dlqEntryTime = crntInterp.dlqEntryTimestamp();
                if (dlqEntryTime.compareTo( expiryTime ) <= 0)
                    {
                    /*
                     * Move the one record over to the Parking topic.
                     * The process that monitors that topic will send out a
                     * notification once it wakes and sees something new.
                     * 
                     */
                    
//                    parkProducer.beginTransaction();
                    String                        msgID      = generateMsgID( crntTime );
                    ProducerRecord<String,String> parkedRcrd = new ProducerRecord<>( parkingTopicName,
                                                                                     msgID,
                                                                                     dlqRecord.value() );
                        
                    try
                        {
                    	logr.debug( "Sending Bundle ID {} to the Park Topic.", crntInterp.resourceID() );
                        RecordMetadata meta = parkProducer.send( parkedRcrd ).get();
                        commitMove          = true;
                        
                        if (meta == null)
                        	{
                        	// Not an error.
                        	}
                        }
                    
                    catch (InterruptedException e)
                        {
                        logr.error("Interupted Exception while sending to Kafka:", e );
                        commitMove       = false;
                        continueChecking = false;
                        } 

                    catch (ExecutionException e)
                        {
                        logr.error( "Execution Exception while sending to Kafka:", e );
                        commitMove       = false;
                        continueChecking = false;
                        }
                        
                    /*
                     * This record has been moved so let's not look at it again.
                     * The consumer is in the same Consumer Group as the DLQ
                     * Lister command, so it should now skip this record as
                     * well because it has been consumed in the Consumer Group.
                     * 
                     */
                    
                    if (commitMove)
                        {
                    	logr.debug( "Committing move to Park topic." );
//                        parkProducer.commitTransaction();
                        dlqConsumer.commitSync();
                        }
                    
                    else
                    	{
                    	logr.debug( "Aborting move to Park topic." );
//                        parkProducer.abortTransaction();
                    	}
                    }
                
                else
                    {
                    /*
                     * Because records are retrieved in chronological order,
                     * if we find one that is not yet expired, then all
                     * remaining must also be not expired so there is no point
                     * in looping back to check other DLQ entries.
                     * 
                     */
                    
                    continueChecking = false;
                    }
                }       
            }
        }
    
    rcrdsFound += dlqRecords.count();
    if (continueChecking)
        continueChecking = ((!threadIndicatedToStop())  && 
                            ((rcrdsFound > 0) || (dlqRecords.count() == 0))
                           );
    
    if (continueChecking)
        try
            {
            Thread.sleep( pollingInterval );
            }
        catch (InterruptedException e)
            {
            // Nothing to do.
            }
    }


logr.debug( "Exiting {}.parkExpiringDLQEntries", SIMPLE_CLS_NM );
return;
}



private String  generateMsgID( MyInstant crntTime )
{
String  msgID = String.format( "%s-%04d", String.valueOf( crntTime.getEpochMillis() ), rollingIndex++ );

if (rollingIndex >= 10000)
    rollingIndex = 0;

return msgID;
}

private Consumer<String, String>    createConsumer()
{
Consumer<String, String>  rtrn  = null;
Properties                props = new Properties();

String      groupID             = appConfig().configValue( ConfigProperty.KAFKA_DLQ_LISTER_GROUP_ID,
                                                           appConfig().getApplicationName().appName() + ".dlq.group.id" );

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
props.put( ConsumerConfig.MAX_POLL_RECORDS_CONFIG,         "1" );      // Only work one record at a time.
props.put( ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,       "false" );  // Force an explicit commit our scan of the DLQ so we can restart our scan if entries have not expired.

props.put( "auto.offset.reset",  "earliest" );

rtrn = new KafkaConsumer<>( props );

return rtrn;    
}



public void setParkingCheckIntervalMins( Integer minutesToCheckForParkableEntries )
{
if ((minutesToCheckForParkableEntries != null) && (minutesToCheckForParkableEntries > 0))
    {
    parkingCheckIntervalMins   = minutesToCheckForParkableEntries;
    parkingCheckIntervalMillis = Long.valueOf( parkingCheckIntervalMins.longValue() * 60L * 1000L );
    }

return;
}




public void setMaxHoursOnDLQ( Integer maxHoursOnDLQ )
{
if ((maxHoursOnDLQ != null) && (maxHoursOnDLQ > 0))
    {
    maxTimeOnDLQHours  = maxHoursOnDLQ;
    maxTimeOnDLQMillis = Long.valueOf( maxTimeOnDLQHours.longValue() * 60L * 60L * 1000L );
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



}
