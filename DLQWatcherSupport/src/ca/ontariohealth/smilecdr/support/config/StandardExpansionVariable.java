/**
 * 
 */
package ca.ontariohealth.smilecdr.support.config;

/**
 * @author shawn.brant
 *
 */
public enum StandardExpansionVariable 
{
  NOW(                    "Now",                    "The current timestamp" )
, ENV_NAME_VAR(           "EnvironmentName",        "The environment name as supplied into the application" )
, INST_NAME_VAR(          "InstanceName",           "The application instance name as supplied into the application" )
, DLQ_TOPIC(              "DLQTopicName",           "The name of the Dead Letter Queue Kafka topic" )
, DLQ_MAX_TIME(           "AllowedTimeOnDLQ",       "The maximum allowed time in the Dead Letter Queue Kafka Topic before being Parked" )
, DLQ_PURGE_TIME(         "DLQPurgeTime",           "The time on the Dead Letter Queue before being allowed to be purged from it" )
, PARKING_LOT_TOPIC(      "ParkingLotTopicName",    "The name of the Kafka Topic to moving expiring DLQ entries to" )
, PARKING_LOT_PURGE_TIME( "ParkingLotPurgeTime",    "The maximum amount of time an entry is allowed to exist on the Parking Lot" )
, RECORD_COUNT(           "RecordCount",            "The number of records being reported as the main report." )
, RECORDS_CSV_HEADER(     "RecordsCSVHeader",       "The CSV Headers for the main report" )
, RECORDS_AS_CSV(         "RecordsAsCSV",           "The main report records in a CSV formatted string" )
;	

private static final String EXPANSION_VAR_LEADER  = "{{";
private static final String EXPANSION_VAR_TRAILER = "}}";

private final String baseVarName;
private final String expansionVarString;
private final String descr;



private StandardExpansionVariable( String baseVar, String desc )
{
baseVarName 	   = baseVar.strip();
expansionVarString = EXPANSION_VAR_LEADER + baseVarName + EXPANSION_VAR_TRAILER;
descr              = desc;

return;
}


public final String description()
{
return descr;
}



public final String baseVarString()
{
return baseVarName;
}



public final String expansionVariable()
{
return expansionVarString; 
}
}
