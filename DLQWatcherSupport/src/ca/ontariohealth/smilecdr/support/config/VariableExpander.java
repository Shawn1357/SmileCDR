/**
 * 
 */
package ca.ontariohealth.smilecdr.support.config;

import ca.ontariohealth.smilecdr.BaseApplication;
import ca.ontariohealth.smilecdr.support.MyInstant;
import ca.ontariohealth.smilecdr.support.commands.DLQRecordsInterpreter;

/**
 * @author shawn.brant
 *
 */
public class VariableExpander 
{
Configuration	appConfig = null;

public	VariableExpander()
{
appConfig = BaseApplication.config();
return;
}


public VariableExpander( Configuration appCfg )
{
appConfig = (appCfg != null) ? appCfg : BaseApplication.config();
return;		
}




public	String	expand( String srcLine )
{
String	rtrn = srcLine;

if (rtrn == null)
	rtrn = "";

else
	{
	
	}

return rtrn;
}



public	String	expand( String srcLine, DLQRecordsInterpreter rcrds )
{
String rtrn = srcLine;

if (rtrn == null)
	rtrn = "";

else
	{
	}

return rtrn;
}


public	String	expand( String srcLine, StandardExpansionVariable varToExpand )
{
String	rtrn = null;

if (srcLine == null)
	rtrn = "";

else if ((varToExpand != null) && (srcLine.contains( varToExpand.expansionVariable() )))
	rtrn = expand( srcLine, varToExpand, null );

return rtrn;
}



public  String	expand( String						srcLine, 
						StandardExpansionVariable	varToExpand, 
						DLQRecordsInterpreter		rcrds )
{
String	rtrn = srcLine;

if (srcLine == null)
	srcLine = "";

else if ((varToExpand != null) && (rtrn.contains( varToExpand.expansionVariable() )))
	{
	switch (varToExpand)
		{
		case	NOW:
			rtrn = srcLine.replace( varToExpand.expansionVariable(), MyInstant.now().toString( appConfig, ConfigProperty.TIMESTAMP_FORMAT ) );
			break;
			
		case	ENV_NAME_VAR:
			rtrn = srcLine.replace( varToExpand.expansionVariable(), appConfig.getEnvironmentName() != null ? appConfig.getEnvironmentName().envName() : "" );
			break;

		case	INST_NAME_VAR:
			rtrn = srcLine.replace( varToExpand.expansionVariable(), appConfig.getInstanceName()    != null ? appConfig.getInstanceName().instName()   : "" );
			break;

		case	DLQ_TOPIC:
			rtrn = srcLine.replace( varToExpand.expansionVariable(), appConfig.configValue( ConfigProperty.KAFKA_DLQ_TOPIC_NAME, "unknown" ) );
			break;

		case	DLQ_MAX_TIME:
			rtrn = srcLine.replace( varToExpand.expansionVariable(), appConfig.configValue( ConfigProperty.DLQ_PARK_ENTRIES_AFTER_HOURS, "unknown" ));
			break;
			
		case	DLQ_PURGE_TIME:
			rtrn = srcLine.replace( varToExpand.expansionVariable(), appConfig.configValue( ConfigProperty.KAFKA_DLQ_RETENTION_HOURS, "unknown" ) );
			break;

		case	PARKING_LOT_TOPIC:
			rtrn = srcLine.replace( varToExpand.expansionVariable(), appConfig.configValue( ConfigProperty.KAFKA_PARK_TOPIC_NAME, "unknown" ));
			break;

		case	PARKING_LOT_PURGE_TIME:
			rtrn = srcLine.replace( varToExpand.expansionVariable(), appConfig.configValue( ConfigProperty.KAFKA_PARK_RETENTION_HOURS, "unknown" ));
			break;

		case	RECORD_COUNT:
			rtrn = srcLine.replace( varToExpand.expansionVariable(), rcrds != null ? Integer.toString( rcrds.recordCount() ) : "" );
			break;
				
		case	RECORDS_CSV_HEADER:
			rtrn = srcLine.replace( varToExpand.expansionVariable(), rcrds != null ? rcrds.csvHeaders() : "" );
			break;
				
		case	RECORDS_AS_CSV:
			rtrn = srcLine.replace( varToExpand.expansionVariable(), rcrds != null ? rcrds.asCSVReport() : "" );
			break;
				
		default:
			// Unhandled variable to expand.
			// Leave it be - perhaps one of the other expansion routines can handle it.
			rtrn = srcLine;
			break;
		}
	}


return rtrn;
}




public  String  expand( String srcLine, String                    sysEnvVar );
public	String	expand( String srcLine, Configuration             appCfg,      ConfigProperty        cfgProp );
public	String  expand( String srcLine, Configuration             appCfg,      String                propNm );
}