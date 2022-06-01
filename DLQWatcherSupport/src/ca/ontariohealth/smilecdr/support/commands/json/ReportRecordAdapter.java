/**
 * 
 */
package ca.ontariohealth.smilecdr.support.commands.json;

import java.io.IOException;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import ca.ontariohealth.smilecdr.support.commands.response.DLQRecordEntry;
import ca.ontariohealth.smilecdr.support.commands.response.KeyValue;
import ca.ontariohealth.smilecdr.support.commands.response.ReportRecord;
import ca.ontariohealth.smilecdr.support.commands.response.ReportRecordType;

/**
 * @author adminuser
 *
 */
public class ReportRecordAdapter extends TypeAdapter<ReportRecord>
{
private static  Logger          logr                    = LoggerFactory.getLogger( ReportRecordAdapter.class );

private static  String          FIELD_STRING_LINE       = "text";
private static  String          FIELD_KEY               = "key";
private static  String          FIELD_VALUE             = "value";
private static  String          FIELD_ENTRY_TS          = "entryTimestamp";
private static  String          FIELD_SUBSCRIPTION_ID   = "subscriptionID";
private static  String          FIELD_RESOURCE_TYPE     = "resourceType";
private static  String          FIELD_RESOURCE_ID       = "resourceID";

private static  InstantAdapter  instantAdapter          = new InstantAdapter();


@Override
public void write(JsonWriter writer, ReportRecord value) throws IOException
{
if (value != null)
    {
    writer.beginObject();
    
    ReportRecordType    rcrdTyp = value.getRcrdType();
    
    switch (rcrdTyp)
        {
        case    STRING:
            String  rprtLine = value.getRcrdString();
            
            writer.name(  FIELD_STRING_LINE );
            writer.value( rprtLine );
            break;
            
            
        case    KEY_VALUE:
            KeyValue    keyVal = value.getRcrdKeyValue();
            
            writer.name( FIELD_KEY );
            writer.value( keyVal.getKey() );
            
            writer.name( FIELD_VALUE );
            writer.value( keyVal.getValue() );
            break;
            
            
        case    DLQ_ENTRY_SPEC:
            DLQRecordEntry  dlqEntry    = value.getRcrdDLQEntry();
            Instant         dlqTS       = dlqEntry.getEntryTimestamp();
            
            writer.name( FIELD_ENTRY_TS );
            instantAdapter.write( writer, dlqTS );
            
            writer.name( FIELD_SUBSCRIPTION_ID );
            writer.value( dlqEntry.getSubscriptionID() );
            
            writer.name( FIELD_RESOURCE_TYPE );
            writer.value( dlqEntry.getResourceType() );
            
            writer.name( FIELD_RESOURCE_ID );
            writer.value( dlqEntry.getResourceID() );
            
            break;
        
            
        default:
            // Unexpected Report Record Type
            logr.error( "Unexpected Report Record Type: {}", rcrdTyp.toString() );
            //throw new IllegalArgumentException( "Unable to convert record of type: " + rcrdTyp.toString() + " into JSON format." );
        }
    
    writer.endObject();
    }

else
    writer.nullValue();

return;
}



@Override
public ReportRecord read( JsonReader reader ) throws IOException
{
ReportRecord    rtrn = null;

ReportRecordType    rprtRcrdType        = null;
String              rprtTextLine        = null;
Instant             rprtDLQEntryTS      = null;
String              rprtDLQSubID        = null;
String              rprtDLQRsrcType     = null;
String              rprtDLQRsrcID       = null;
String              rprtKey             = null;
String              rprtValue           = null;

reader.beginObject();
while (reader.hasNext())
    {
    JsonToken   token       = reader.peek();
    String      fieldName   = null;
    
    
    if (token.equals( JsonToken.NAME ))
        fieldName = reader.nextName();
    
    else
        fieldName = null;
    
    
    if (FIELD_STRING_LINE.equals( fieldName ))
        {
        token = reader.peek();
        
        rprtTextLine = reader.nextString();
        rprtRcrdType = ReportRecordType.STRING;
        }
    
    else if (FIELD_KEY.equals( fieldName ))
        {
        token = reader.peek();
        
        rprtKey = reader.nextString();
        rprtRcrdType = ReportRecordType.KEY_VALUE;
        }
    
    else if (FIELD_VALUE.equals( fieldName ))
        {
        token = reader.peek();
        
        rprtValue = reader.nextString();
        rprtRcrdType = ReportRecordType.KEY_VALUE;
        }
    
    else if (FIELD_ENTRY_TS.equals( fieldName ))
        {
        token = reader.peek();
        
        rprtDLQEntryTS = instantAdapter.read( reader );
        rprtRcrdType   = ReportRecordType.DLQ_ENTRY_SPEC;
        }
    
    else if (FIELD_SUBSCRIPTION_ID.equals( fieldName ))
        {
        token = reader.peek();
        
        rprtDLQSubID = reader.nextString();
        rprtRcrdType = ReportRecordType.DLQ_ENTRY_SPEC;
        }
    
    else if (FIELD_RESOURCE_TYPE.equals( fieldName ))
        {
        token = reader.peek();
        
        rprtDLQRsrcType = reader.nextString();
        rprtRcrdType    = ReportRecordType.DLQ_ENTRY_SPEC;
        }
    
    else if (FIELD_RESOURCE_ID.equals( fieldName ))
        {
        token = reader.peek();
        
        rprtDLQRsrcID   = reader.nextString();
        rprtRcrdType    = ReportRecordType.DLQ_ENTRY_SPEC;
        }
    }

reader.endObject();

if (rprtRcrdType != null)
    {
    switch (rprtRcrdType)
        {
        case    STRING:
            rtrn = new ReportRecord( rprtTextLine );
            break;
            
        case    KEY_VALUE:
            KeyValue keyVal = new KeyValue( rprtKey, rprtValue );
            rtrn = new ReportRecord( keyVal );
            break;
            
        case    DLQ_ENTRY_SPEC:
            DLQRecordEntry  dlqEntry = new DLQRecordEntry( rprtDLQEntryTS, rprtDLQSubID, rprtDLQRsrcType, rprtDLQRsrcID );
            rtrn = new ReportRecord( dlqEntry );
            break;
            
        default:
            logr.error( "Unexpected Report Record Line: {}", rprtRcrdType.toString() );
            break;
        }
    }

return rtrn;
}
}
