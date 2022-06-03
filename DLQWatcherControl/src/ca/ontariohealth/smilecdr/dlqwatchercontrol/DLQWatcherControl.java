/**
 * 
 */
package ca.ontariohealth.smilecdr.dlqwatchercontrol;

import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import ca.ontariohealth.smilecdr.BaseApplication;
import ca.ontariohealth.smilecdr.support.MyInstant;
import ca.ontariohealth.smilecdr.support.commands.DLQCommand;
import ca.ontariohealth.smilecdr.support.commands.DLQCommandContainer;
import ca.ontariohealth.smilecdr.support.commands.DLQCommandParam;
import ca.ontariohealth.smilecdr.support.commands.DLQResponseContainer;
import ca.ontariohealth.smilecdr.support.commands.json.CommandParamAdapter;
import ca.ontariohealth.smilecdr.support.commands.json.MyInstantAdapter;
import ca.ontariohealth.smilecdr.support.commands.json.ReportRecordAdapter;
import ca.ontariohealth.smilecdr.support.commands.response.ReportRecord;
import ca.ontariohealth.smilecdr.support.config.ConfigProperty;
import ca.ontariohealth.smilecdr.support.kafka.KafkaConsumerHelper;
import ca.ontariohealth.smilecdr.support.kafka.KafkaProducerHelper;



/**
 * @author adminuser
 *
 */
public class DLQWatcherControl extends BaseApplication
{
static final Logger 						    logr      		= LoggerFactory.getLogger(DLQWatcherControl.class);

protected static final	String 				    CLI_TOPIC_SHRT  = "t";
protected static final	String 				    CLI_TOPIC_LONG  = "topicNm";

protected static final  String				    CLI_OPERTN_SHRT = "o";
protected static final  String                  CLI_OPERTN_LONG = "operation";

private GsonBuilder                             jsonBuilder     = new GsonBuilder();

private int                                     rollingNdx      = 0;

private Map<String, Consumer<String,String>>    allConsumers    = new HashMap<>();


public static void main( String[] args )
{
logr.debug( "Entering: main" );

DLQWatcherControl	watcherCtrl = new DLQWatcherControl();
watcherCtrl.launch(args);

logr.debug( "Exiting: main");
return;
}



@Override
protected void launch() 
{
logr.debug("Entering: DLQWatcherControl.launch" );

jsonBuilder.registerTypeAdapter( DLQCommandParam.class, new CommandParamAdapter() );
jsonBuilder.registerTypeAdapter( ReportRecord.class,    new ReportRecordAdapter() );
jsonBuilder.registerTypeAdapter( MyInstant.class,       new MyInstantAdapter() );
jsonBuilder.setPrettyPrinting();

/*
 * Grab the command line options specific to this application.
 * 
 */
	
//String commandToSend = cmdLine.getOptionValue( CLI_OPERTN_LONG );
String[] cmdsToSend = cmdLine.getOptionValues( CLI_OPERTN_LONG );


String kafkaTopicName = null;
if (cmdLine.hasOption(CLI_TOPIC_LONG))
	kafkaTopicName = cmdLine.getOptionValue( CLI_TOPIC_LONG );

else
	kafkaTopicName = appConfig.configValue( ConfigProperty.CONTROL_TOPIC_NAME_COMMAND );

if (cmdsToSend != null)
    {
    Long   maxRespWait = appConfig.configLong( ConfigProperty.RESPONSE_WAIT_MILLIS );
    
	for (String crntCmd : cmdsToSend)
		if ((crntCmd != null) && (crntCmd.length() > 0))
		    {
		    DLQCommandContainer   cmdToSend   = null;
		    DLQResponseContainer  cmdResp     = null;
		    
		    cmdToSend = createWatcherCommand( crntCmd );
		    
			sendCommand( kafkaTopicName, cmdToSend );
			
			cmdResp = waitForResponse( cmdToSend, maxRespWait );
		    }
    }

logr.debug("Exiting: DLQWatcherControl.launch" );
return;	
}




private DLQResponseContainer    waitForResponse( DLQCommandContainer cmdSent, Long maxWaitMillis )
{
logr.debug( "Entering: waitForResponse" );

DLQResponseContainer    resp        = null;
String                  respChannel = (cmdSent != null) ? cmdSent.getResponseChannelName() : null;

if ((cmdSent != null) && (respChannel != null) && (respChannel.length() > 0))
    {
    MyInstant               startOfWait  = MyInstant.now();
    MyInstant               now          = null;
    Duration                pollInterval = Duration.ofMillis( appConfig.configLong( ConfigProperty.KAFKA_CONSUMER_POLL_INTERVAL ).longValue() );
    String                  groupNm      = appConfig.configValue( ConfigProperty.KAFKA_CONTROL_GROUP_ID );
    Consumer<String,String> consumer     = KafkaConsumerHelper.createConsumer( appConfig, groupNm, respChannel );
    UUID                    cmdID        = cmdSent.getCommandUUID();
   
    
    do
        {
        ConsumerRecords<String,String>  rcrds = consumer.poll( pollInterval );
        
        if ((rcrds != null) && (rcrds.count() > 0))
            {
            // Look for the specific repsonse pertaining to the command that
            // was sent.
            logr.debug( "Received {} record(s) from Kafka.", rcrds.count() );
            for (ConsumerRecord<String,String> crnt : rcrds)
                {
                DLQResponseContainer crntResp = DLQResponseContainer.fromJSON( crnt.value() );
                DLQCommandContainer  respCmd  = crntResp.getSourceCommand();
                UUID                 cmdUUID  = (respCmd != null) ? respCmd.getCommandUUID() : null;
                
                if ((cmdUUID != null) && (cmdID.compareTo( cmdUUID ) == 0))
                    {
                    logr.debug( "Received the response we were hoping for:" );
                    logr.debug( "\n{}", crnt.value() );
                    resp = crntResp;
                    break;
                    }
                
                else
                    {
                    logr.debug( "Unexpected response was retrieved not matching Cmd ID: {}", cmdID.toString() );
                    logr.debug( "\n{}", crnt.value() );
                    }
                }
            }
        
        else
            logr.debug( "No Response Records were returned in the last: {} milliseconds.", pollInterval.toMillis() );
            
        
        now = MyInstant.now();
        }
    while ((resp == null) && (now.getEpochMillis() < (startOfWait.getEpochMillis() + maxWaitMillis)));
    
    logr.debug( "Committing the topic events that have been read." );
    consumer.commitSync();
    }


logr.debug( "Exiting: waitForResponse" );
return resp;
}







private void sendCommand( final String            kafkaTopicName, 
		                  DLQCommandContainer     commandToSend )
{
logr.debug( "Entering: sendCommand" );
logr.debug(  "   Topic Name: {}", kafkaTopicName );
logr.debug(  "   Command:    {}", commandToSend );

final Producer<String, String> prdcr        = KafkaProducerHelper.createProducer( appConfig, kafkaTopicName );
long                           crntTime     = System.currentTimeMillis();
String                         msgID        = String.valueOf( crntTime ) + String.format( "-%04d", rollingNdx ++ );   
String                         jsonCmd      = commandToSend.toJSON( jsonBuilder );

if (rollingNdx >= 10000)
    rollingNdx = 0;

logr.debug( "About to send the following command to the DLQ Wastcher:" );
logr.debug( "\n{}", jsonCmd );

ProducerRecord<String, String> record = new ProducerRecord<>( kafkaTopicName,
		                                       				  msgID,
		                                       				  jsonCmd );
try 
	{
	logr.debug( "Sending Command to Kafka Topic" );
	RecordMetadata	metadata = prdcr.send( record ).get();
	
	prdcr.close();
	
	if (metadata != null)
		{
		logr.debug( "Metadata Returned:" );
		logr.debug( "   Has Offset:            {}", metadata.hasOffset()    ? "Yes" : "No" );
		logr.debug( "   Has Timestamp:         {}", metadata.hasTimestamp() ? "Yes" : "No" );
		
		if (metadata.hasOffset())
			logr.debug( "   Offset Value:          {}", metadata.offset() );
		
		if (metadata.hasTimestamp())
			logr.debug( "   Timestamp:             {}", metadata.timestamp() );
		
		logr.debug( "   Partition:             {}", metadata.partition() );
		logr.debug( "   Serialized Key Size:   {}", metadata.serializedKeySize() );
		logr.debug( "   Serialized Value Size: {}", metadata.serializedValueSize() );
		}
	
	else
		logr.error( "Kafka Send Request resulted in null Metadata" );
	}

catch (InterruptedException e)
	{
	logr.error("Interupted Exception while sending to Kafka:", e );
	} 

catch (ExecutionException e)
	{
	logr.error( "Execution Exception while sending to Kafka:", e );
	}

logr.debug( "Exiting: sendCommand" );
return;
}




private DLQCommandContainer createWatcherCommand( String cmdLine )
{
DLQCommandContainer rtrn = null;

if ((cmdLine != null) && (cmdLine.length() > 0))
    {
    String                  issueChannel = appConfig.configValue( ConfigProperty.CONTROL_TOPIC_NAME_COMMAND );
    String                  respChannel  = appConfig.configValue( ConfigProperty.CONTROL_TOPIC_NAME_RESPONSE );
    String[]                args         = cmdLine.split( " " );
    DLQCommand              cmd          = null;
    List<DLQCommandParam>   parms        = new LinkedList<>();
    int                     ndx          = 0;
    
    for (String crnt : args)
        {
        if (ndx++ == 0)
            cmd = DLQCommand.valueOf( crnt );
        
        else
            {
            DLQCommandParam newParm = new DLQCommandParam( crnt );
            parms.add( newParm );
            }
        }
    
    if ((cmd != null) && (cmd != DLQCommand.UNKNOWN))
        {
        rtrn = new DLQCommandContainer( issueChannel,
                                        respChannel,
                                        cmd,
                                        parms );
        }
    }

return rtrn;
}



@Override
protected void createCLIOptions( Options cmdLineOpts ) 
{
super.createCLIOptions( cmdLineOpts );

Option	topic = new Option( CLI_TOPIC_SHRT,  CLI_TOPIC_LONG,  true, "The Kafka Topic Name to receive the command." );
Option  oprtn = new Option( CLI_OPERTN_SHRT, CLI_OPERTN_LONG, true, "The operation to be loaded into the Kafka Topic" );

oprtn.setRequired( true );

cmdLineOpts.addOption( topic ).addOption( oprtn );

return;
}



}
