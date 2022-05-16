/**
 * 
 */
package ca.ontariohealth.smilecdr.dlqwatcher;

import java.util.ArrayList;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
private ArrayList<DLQInstanceDetails>		dlqDetails  = new ArrayList<>();
private String								dlqAsCSV    = null;


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
	for (DLQInstanceDetails crntInst : dlqDetails)
		if (crntInst != null)
			dlqAsCSV = dlqAsCSV.concat( "    " ).concat( crntInst.asCSV() ).concat( System.lineSeparator() );
	}


logr.debug( "Exiting: extractDetailsList" );
return;
}



private void	extractOneRecordDetails( ConsumerRecord<String, String> rcrd )
{
logr.debug( "Entering: extractOneRecordDetails" );

String	rcrdJSONString = rcrd.value();

if ((rcrdJSONString != null) && (rcrdJSONString.length() > 0))
	{
	int canonIndex = rcrdJSONString.indexOf( "canonicalSubscription" );
	int slashIndex = rcrdJSONString.indexOf( '/', canonIndex );
	int commaIndex = rcrdJSONString.indexOf( ',', canonIndex );
	
	String subscrID = rcrdJSONString.substring( slashIndex + 1, commaIndex - 1 );
	
	int resourceTypeIndex = rcrdJSONString.indexOf( "resourceType" );
	int colonIndex        = rcrdJSONString.indexOf( ':', resourceTypeIndex );
	commaIndex            = rcrdJSONString.indexOf( ',', colonIndex );
	
	String resourceType = rcrdJSONString.substring( colonIndex + 3, commaIndex - 2 );
	
	colonIndex = rcrdJSONString.indexOf( ':', commaIndex );
	commaIndex = rcrdJSONString.indexOf( ',', colonIndex );
	
	String resourceID = rcrdJSONString.substring( colonIndex + 3, commaIndex - 2 );
	
	logr.debug( "Subscription ID: {}", subscrID );
	logr.debug( "Resource Type:   {}", resourceType );
	logr.debug( "Resource ID:     {}", resourceID );
	
	DLQInstanceDetails	dtls = new DLQInstanceDetails( subscrID, resourceType, resourceID );
	dlqDetails.add( dtls );
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



public	String	asCSVReport()
{
return dlqAsCSV != null ? dlqAsCSV : "<<No Records>>";
}
}
