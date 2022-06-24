/**
 * 
 */
package ca.ontariohealth.smilecdr.support.commands;

import java.util.ArrayList;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ontariohealth.smilecdr.support.config.ConfigProperty;
import ca.ontariohealth.smilecdr.support.config.Configuration;
import ca.ontariohealth.smilecdr.support.kafka.KafkaTopicRecordParser;

/**
 * @author adminuser
 *
 */
public class DLQRecordsInterpreter
{
private static final	Logger				            logr			= LoggerFactory.getLogger(DLQRecordsInterpreter.class);

private	Configuration						            appConfig		= null;
private	ArrayList<ConsumerRecord<String, String>>       dlqRecords		= new ArrayList<>();
private ArrayList<KafkaTopicRecordParser>               dlqDetails  	= new ArrayList<>();
private	String											parserClassNm	= null;

private static final String      CSV_FLD_SEP = ", ";
private              String[]    csv_hdrs    = null;


// private MyInstant   reportStartTime = MyInstant.now();


public  DLQRecordsInterpreter( Configuration appCfg )
{
logr.debug( "Entering: DLQRecordsInterpreter(ConsumerRecords)" );

if (appCfg == null)
    throw new IllegalArgumentException( "appCfg parameter must not be null." );

appConfig  = appCfg;
parserClassNm = appConfig.configValue( ConfigProperty.DLQ_PARSER_FQCN_CLASS );

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
    extractOneRecordDetails( dlqDetails, kafkaRcrd, parserClassNm );
    }
}



public List<KafkaTopicRecordParser>  getInterpretations()
{
return dlqDetails;
}



private void extractDetailsList( List<KafkaTopicRecordParser>    targetList,
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
			extractOneRecordDetails( targetList, crnt, parserClassNm );
		    }
		}
	}


logr.debug( "Exiting: extractDetailsList" );
return;
}



private String generateCSVReport( List<KafkaTopicRecordParser> dlqEntries )
{
StringBuffer  rtrn = new StringBuffer();

for (KafkaTopicRecordParser crnt : dlqEntries)
    if (crnt != null)
        rtrn.append( asCSV( crnt ) ).append( System.lineSeparator() );

return rtrn.toString();
}



private String  asCSV( KafkaTopicRecordParser rcrd )
{
StringBuffer        rtrn    = new StringBuffer();

if (rcrd != null)
    {
    String[]            csvHdrs = rcrd.csvColumnHeaders();
    String[]            csvVals = rcrd.csvColumnValues();
    
    for (int crntNdx = 0; (crntNdx < csvHdrs.length) && (crntNdx < csvVals.length); crntNdx++)
        {
        int     fldWid  = csvHdrs[crntNdx].length();
        String  crntVal = csvVals[crntNdx];
        
        if (crntNdx > 0)
            rtrn.append( CSV_FLD_SEP );
        
        rtrn.append( formatField( fldWid, crntVal ) );
        }
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


private void	extractOneRecordDetails( List<KafkaTopicRecordParser>   targetList,
                                         ConsumerRecord<String, String> rcrd,
                                         String							parserClassName )
{
logr.debug( "Entering: extractOneRecordDetails" );

if (rcrd != null)
    {
    KafkaTopicRecordParser	crntRcrd = KafkaTopicRecordParser.fromKafkaRecord( rcrd, appConfig, parserClassName );

    if (crntRcrd != null)
        {
        if (csv_hdrs == null)
            csv_hdrs = crntRcrd.csvColumnHeaders();
        
        targetList.add( crntRcrd );
        }
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
String  rtrn = "";

if (csv_hdrs != null)
    rtrn = String.join( CSV_FLD_SEP, csv_hdrs );

return rtrn;
}




public	String	asCSVReport()
{
String  report = null;

if ((recordCount() > 0) && (dlqDetails != null))
    report = generateCSVReport( dlqDetails );

else
    report = "--No Records--";
    
return report;
}
}
