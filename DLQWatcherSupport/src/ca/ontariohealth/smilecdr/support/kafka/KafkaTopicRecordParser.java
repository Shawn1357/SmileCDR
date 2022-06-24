/**
 * 
 */
package ca.ontariohealth.smilecdr.support.kafka;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.Duration;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ontariohealth.smilecdr.support.MyInstant;
import ca.ontariohealth.smilecdr.support.config.ConfigProperty;
import ca.ontariohealth.smilecdr.support.config.Configuration;

/**
 * @author adminuser
 *
 */
public abstract class KafkaTopicRecordParser
{
private   static    Logger                    logr          = LoggerFactory.getLogger(KafkaTopicRecordParser.class);

protected ConsumerRecord<String,String>       origRecord    = null;
protected Configuration                       appConfig     = null;
protected String                              srcTopicNm    = null;
protected JSONObject                          parsedJSON    = null;
protected MyInstant                           topicEntryTS  = null;
protected MyInstant                           dlqEntryTS    = null;
protected String                              rsrcType      = null;
protected String                              rsrcID        = null;



public static   KafkaTopicRecordParser fromKafkaRecord( ConsumerRecord<String,String> srcKafkaRcrd, 
                                                        Configuration                 appCfg,
                                                        String                        parserClassName )
                                                                
{
KafkaTopicRecordParser  rtrn = null;

logr.debug( "Entering: KafkaTopicRecordParser.fromKafkaRecord" );

if ((srcKafkaRcrd != null) && (appCfg != null))
    {
    logr.debug("About to create an instance of class: {}", parserClassName );   
    
    if ((parserClassName != null) && (parserClassName.length() > 0))
        {
        try
            {
            Class<?>        clazz = Class.forName( parserClassName );
            Constructor<?>  ctor  = clazz.getConstructor( ConsumerRecord.class, Configuration.class );
        
            rtrn = (KafkaTopicRecordParser) ctor.newInstance( new Object[] { srcKafkaRcrd, appCfg } );
            }
        
        catch (ClassNotFoundException   | 
               NoSuchMethodException    |
               SecurityException        |
               IllegalAccessException   |
               InstantiationException   |
               IllegalArgumentException |
               InvocationTargetException e)
               
            {
            throw new IllegalArgumentException( "Unable to create a class of instance: " + parserClassName, e );
            }
        }
    
    else
        throw new IllegalArgumentException( "Could not find a relevant and populated configuration property for: " +
                                            ConfigProperty.DLQ_PARSER_FQCN_CLASS.propertyName() );
    
/*
    String  srcTopic = srcKafkaRcrd.topic();
    String  dlqTopic = appCfg.configValue( ConfigProperty.KAFKA_DLQ_TOPIC_NAME );
    
    if ((srcTopic != null) && (dlqTopic != null) && (dlqTopic.equals( srcTopic )))
        rtrn = new CWMDLQRecordEntry( srcKafkaRcrd, appCfg );
*/
    }

logr.debug( "Exiting: KafkaTopicRecordParser.fromKafkaRecord" );
return rtrn;
}



public KafkaTopicRecordParser( ConsumerRecord<String,String> srcKafkaRcrd, 
                               Configuration                 appCfg )
{
fromDLQRecord( srcKafkaRcrd, appCfg );

return;
}



public KafkaTopicRecordParser( MyInstant  dlqEntryTS,
                               String     resourceType,
                               String     resourceID )

{
this.topicEntryTS = dlqEntryTS;
this.dlqEntryTS   = dlqEntryTS;
this.rsrcType     = resourceType;
this.rsrcID       = resourceID;

return;
}



protected void  fromDLQRecord( ConsumerRecord<String,String> srcKafkaRcrd,
                               Configuration                 appCfg )
{
appConfig  = appCfg;
origRecord = srcKafkaRcrd;

fromDLQRecord();
return;
}




protected void  fromDLQRecord()
{
srcTopicNm      = null;
parsedJSON      = null;
topicEntryTS    = null;
dlqEntryTS      = null;
rsrcType        = null;
rsrcID          = null;

if (origRecord != null)
    {    
    srcTopicNm = origRecord.topic();
    
    
    parseFullPayload( origRecord.value() );
    
    extractTopicEntryTimestamp();
    extractDLQEntryTimestamp();
    extractSubscriptionID();
    extractResourceType();
    extractResourceID();
    }

return;
}




protected  void        parseFullPayload( String  jsonString )
{
if ((jsonString != null) && (jsonString.length() > 0))
    parsedJSON = new JSONObject( jsonString );

return;
}




public  String      elapsedTimeInTopic()
{
String      elapsedTime    = null;
MyInstant   crntTime       = MyInstant.now();
MyInstant   topicEntryTime = topicEntryTimestamp();

if ((topicEntryTime != null) && (crntTime != null))
    {
    Duration dur = Duration.between( topicEntryTime.asInstant(), crntTime.asInstant() );
    
    elapsedTime = String.format( "%dd %d:%02d", dur.toDays(), dur.toHoursPart(), dur.toMinutesPart() );
    }

return elapsedTime;
}






public  ConsumerRecord<String,String>   originalKafkaRecord()
{
return origRecord;
}




public  MyInstant   topicEntryTimestamp()
{
return topicEntryTS;
}



public  MyInstant    dlqEntryTimestamp()
{
return dlqEntryTS;
}


public  String      resourceType()
{
return rsrcType;
}




public  String      resourceID()
{
return rsrcID;
}



public JSONObject  dlqBundle()
{
return parsedJSON;
}



public abstract String         subscriptionID();



protected void  extractTopicEntryTimestamp()
{
topicEntryTS = null;

long    ts = origRecord.timestamp();
if (ts > 0)
    topicEntryTS = new MyInstant( ts );

else
    topicEntryTS = MyInstant.now();

return;
}


protected abstract void        extractDLQEntryTimestamp();
protected abstract void        extractSubscriptionID();
protected abstract void        extractResourceType();
protected abstract void        extractResourceID();
public    abstract String[]    csvColumnHeaders();
public    abstract String[]    csvColumnValues();
}
