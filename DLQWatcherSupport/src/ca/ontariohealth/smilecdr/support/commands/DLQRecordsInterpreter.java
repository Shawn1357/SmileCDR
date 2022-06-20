/**
 * 
 */
package ca.ontariohealth.smilecdr.support.commands;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ontariohealth.smilecdr.support.MyInstant;
import ca.ontariohealth.smilecdr.support.commands.response.CWMDLQRecordEntry;
import ca.ontariohealth.smilecdr.support.config.ConfigProperty;
import ca.ontariohealth.smilecdr.support.config.Configuration;

/**
 * @author adminuser
 *
 */
public class DLQRecordsInterpreter
{
private static final	Logger				        logr		= LoggerFactory.getLogger(DLQRecordsInterpreter.class);

private	Configuration						        appConfig	= null;
private	ArrayList<ConsumerRecord<String, String>>   dlqRecords	= new ArrayList<>();
private ArrayList<CWMDLQRecordEntry>		        dlqDetails  = new ArrayList<>();

private static final String[]    CSV_HDRS    = { "SubscriptionID",
                                                 "ResourceType",
                                                 "ResourceID",
                                                 "DLQEntryEpochMillis",
                                                 "DLQEntryLocalTimeStamp",
                                                 "DurationOnDLQ" };

private static final String      CSV_FLD_SEP = ", ";


private MyInstant   reportStartTime = MyInstant.now();


public  DLQRecordsInterpreter( Configuration appCfg )
{
logr.debug( "Entering: DLQRecordsInterpreter(ConsumerRecords)" );

if (appCfg == null)
    throw new IllegalArgumentException( "appCfg parameter must not be null." );

appConfig  = appCfg;

return;
}


public	DLQRecordsInterpreter( ConsumerRecords<String, String> kafkaRcrds,
		                       Configuration                   appCfg )
{
logr.debug( "Entering: DLQRecordsInterpreter(ConsumerRecords)" );

if (kafkaRcrds == null)
	throw new IllegalArgumentException( "dlqRcrds parameter must not be null." );

if (appCfg == null)
	throw new IllegalArgumentException( "appCfg parameter must not be null." );

appConfig  = appCfg;

extractDetailsList( dlqDetails, kafkaRcrds );

logr.debug( "Exiting: DLQRecordsInterpreter(ConsumerRecords)" );
return;
}



public  void    addDLQRecords( ConsumerRecords<String,String>   kafkaRcrds )
{
if (kafkaRcrds != null)
    extractDetailsList( dlqDetails, kafkaRcrds );

return;
}



public void     addDLQRecord( ConsumerRecord<String,String> kafkaRcrd )
{
if (kafkaRcrd != null)
    {
    dlqRecords.add( kafkaRcrd );
    extractOneRecordDetails( dlqDetails, kafkaRcrd );
    }
}



public List<CWMDLQRecordEntry>  getInterpretations()
{
return dlqDetails;
}



private void extractDetailsList( List<CWMDLQRecordEntry>         targetList,
                                 ConsumerRecords<String, String> dlqRcrds )
{
logr.debug( "Entering: extractDetailsList" );

if (dlqRcrds != null)
	{
	logr.debug( "About to extract DLQ Details from {} DLQ Kafka Event(s)", dlqRcrds.count() );
	for (ConsumerRecord<String, String> crnt : dlqRcrds)
		{
		if (crnt != null)
		    {
		    dlqRecords.add( crnt );
			extractOneRecordDetails( targetList, crnt );
		    }
		}
	}


logr.debug( "Exiting: extractDetailsList" );
return;
}



private String generateCSVReport( List<CWMDLQRecordEntry> dlqEntries )
{
StringBuffer  rtrn = new StringBuffer();

for (CWMDLQRecordEntry crnt : dlqEntries)
    if (crnt != null)
        rtrn.append( asCSV( crnt ) ).append( System.lineSeparator() );

return rtrn.toString();
}


private String  asCSV( CWMDLQRecordEntry rcrd )
{
StringBuffer        rtrn  = new StringBuffer();

if (rcrd != null)
    {
    int                 fldWid  = CSV_HDRS[0].length();
    String              crntVal = rcrd.subscriptionID();
    DateTimeFormatter   frmtr   = DateTimeFormatter.ofPattern( appConfig.configValue( ConfigProperty.TIMESTAMP_FORMAT ) );
    
    rtrn.append( formatField( fldWid, crntVal ) );
    
    fldWid = CSV_HDRS[1].length();
    crntVal = rcrd.resourceType();
    rtrn.append( CSV_FLD_SEP ).append( formatField( fldWid, crntVal ) );
    
    fldWid = CSV_HDRS[2].length();
    crntVal = rcrd.resourceID();
    rtrn.append( CSV_FLD_SEP ).append( formatField( fldWid, crntVal ) );
    
    MyInstant ts = rcrd.dlqEntryTimestamp();
    if (ts != null)
        crntVal = rcrd.dlqEntryTimestamp().getEpochMillis().toString();
    
    else
        crntVal = "";
    
    fldWid = CSV_HDRS[3].length();
    rtrn.append( CSV_FLD_SEP ).append( formatField( fldWid, crntVal ) );
    
    LocalDateTime lclTS = (ts != null) ? ts.asLocalDateTime() : null;
    if (lclTS != null)
        crntVal = lclTS.format( frmtr );
    
    else
        crntVal = "";
    
    fldWid = CSV_HDRS[4].length();
    rtrn.append( CSV_FLD_SEP ).append( formatField( fldWid, crntVal ) );
    
    fldWid = CSV_HDRS[5].length();
    Duration timeOnDLQ = Duration.between( ts.asInstant(), reportStartTime.asInstant() );
    crntVal = String.format( "%dd %2d:%02d", 
                             timeOnDLQ.toDays(),
                             timeOnDLQ.toHoursPart(),
                             timeOnDLQ.toMinutesPart() );
    
    rtrn.append( CSV_FLD_SEP ).append( formatField( fldWid, crntVal ) );
    }


return rtrn.toString();
}



private String  formatField( int colWidth, String fldVal )
{
String frmtdFld = null;

if (fldVal == null)
    fldVal = "";

if (fldVal.length() >= colWidth)
    frmtdFld = fldVal;

else
    frmtdFld = " ".repeat( colWidth - fldVal.length() ) + fldVal;

return frmtdFld;
}


private void	extractOneRecordDetails( List<CWMDLQRecordEntry>        targetList,
                                         ConsumerRecord<String, String> rcrd )
{
logr.debug( "Entering: extractOneRecordDetails" );

if (rcrd != null)
    {
    CWMDLQRecordEntry  crntRcrd = new CWMDLQRecordEntry( rcrd, appConfig );

    if (crntRcrd != null)
        targetList.add( crntRcrd );
    }


logr.debug( "Exiting: extractOneRecordDetails" );
return;
}


public	int		recordCount()
{
int		rtrn = 0;

if (dlqDetails != null)
	rtrn = dlqDetails.size();

return rtrn;
}



public  final String  csvHeaders()
{
return String.join( CSV_FLD_SEP, CSV_HDRS );
}




public	String	asCSVReport()
{
String  report = null;

if ((recordCount() > 0) && (dlqDetails != null))
    report = generateCSVReport( dlqDetails );

else
    report = "<<No Records>>";
    
return report;
}
}
