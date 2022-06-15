/**
 * 
 */
package ca.ontariohealth.smilecdr.dlqwatcher;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

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
private static final	Logger				logr		= LoggerFactory.getLogger(DLQRecordsInterpreter.class);

private	Configuration						appConfig	= null;
private	ConsumerRecords<String, String>		dlqRecords	= null;
private ArrayList<CWMDLQRecordEntry>		dlqDetails  = new ArrayList<>();
private String								dlqAsCSV    = null;

private static final String[]               CSV_HDRS    = { "SubscriptionID",
                                                            "ResourceType",
                                                            "ResourceID",
                                                            "DLQEntryEpochMillis",
                                                            "DLQEntryLocalTimeStamp" };

private static final String                 CSV_COL_HDR = String.join( ",", CSV_HDRS );




public	DLQRecordsInterpreter( ConsumerRecords<String, String> dlqRcrds,
		                       Configuration                   appCfg )
{
logr.debug( "Entering: DLQRecordsInterpreter" );

if (dlqRcrds == null)
	throw new IllegalArgumentException( "dlqRcrds parameter must not be null." );

if (appCfg == null)
	throw new IllegalArgumentException( "appCfg parameter must not be null." );

appConfig  = appCfg;
dlqRecords = dlqRcrds;

extractDetailsList();

logr.debug( "Exiting: DLQRecordsInterpreter" );
return;
}


private void extractDetailsList()
{
logr.debug( "Entering: extractDetailsList" );

if (dlqRecords != null)
	{
	logr.debug( "About to extract DLQ Details from {} DLQ Kafka Event(s)", dlqRecords.count() );
	for (ConsumerRecord<String, String> crnt : dlqRecords)
		{
		if (crnt != null)
			extractOneRecordDetails( crnt );
		}
	
	/*
	 * Generate the CSV Summary for the received DLQ Entries:
	 * 
	 */
	
	logr.debug( "Convert {} DLQ Detail Record(s) into a CSV Report.", dlqDetails.size() );
	dlqAsCSV = "";
	for (CWMDLQRecordEntry crntInst : dlqDetails)
		if (crntInst != null)
			dlqAsCSV = dlqAsCSV.concat( "    " ).concat( asCSV( crntInst ) ).concat( System.lineSeparator() );
	}


logr.debug( "Exiting: extractDetailsList" );
return;
}



private String  asCSV( CWMDLQRecordEntry rcrd )
{
StringBuffer        rtrn  = new StringBuffer();

if (rcrd != null)
    {
    String              crntVal = rcrd.subscriptionID();
    DateTimeFormatter   frmtr   = DateTimeFormatter.ofPattern( appConfig.configValue( ConfigProperty.TIMESTAMP_FORMAT ) );
    
    rtrn.append( crntVal != null ? crntVal : "" );
    
    crntVal = rcrd.resourceType();
    rtrn.append( "," ).append( crntVal != null ? crntVal : "" );
    
    crntVal = rcrd.resourceID();
    rtrn.append( "," ).append( crntVal != null ? crntVal : "" );
    
    MyInstant ts = rcrd.dlqEntryTimestamp();
    if (ts != null)
        {
        crntVal = rcrd.dlqEntryTimestamp().getEpochMillis().toString();
        rtrn.append( "," ).append( crntVal != null ? crntVal : "" );
        }
    
    else
        rtrn.append( "," );
    
    LocalDateTime lclTS = (ts != null) ? ts.asLocalDateTime() : null;
    if (lclTS != null)
        {
        crntVal = lclTS.format( frmtr );
        rtrn.append( "," ).append( crntVal != null ? crntVal : "" );
        }
    
    else
        rtrn.append( "," );
    }


return rtrn.toString();
}



private void	extractOneRecordDetails( ConsumerRecord<String, String> rcrd )
{
logr.debug( "Entering: extractOneRecordDetails" );

if (rcrd != null)
    {
    CWMDLQRecordEntry  crntRcrd = new CWMDLQRecordEntry( rcrd, appConfig );

    if (crntRcrd != null)
        dlqDetails.add( crntRcrd );
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
return CSV_COL_HDR;
}




public	String	asCSVReport()
{
return dlqAsCSV != null ? dlqAsCSV : "<<No Records>>";
}
}
