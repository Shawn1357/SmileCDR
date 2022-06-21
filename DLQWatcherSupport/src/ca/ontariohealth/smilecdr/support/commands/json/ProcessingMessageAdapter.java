/**
 * 
 */
package ca.ontariohealth.smilecdr.support.commands.json;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import ca.ontariohealth.smilecdr.support.commands.ProcessingMessage;
import ca.ontariohealth.smilecdr.support.commands.ProcessingMessageCode;
import ca.ontariohealth.smilecdr.support.commands.ProcessingMessageSeverity;

/**
 * @author adminuser
 *
 */
public class ProcessingMessageAdapter extends TypeAdapter<ProcessingMessage>
{
private static  Logger          logr                    = LoggerFactory.getLogger( ProcessingMessageAdapter.class );

private static  String          FIELD_MSG_CODE          = "code";
private static  String          FIELD_MSG_SEVERITY      = "severity";
private static  String          FIELD_DESCR             = "desc";


@Override
public void write(JsonWriter writer, ProcessingMessage value) throws IOException
{
if (value != null)
    {
    writer.beginObject();

    writer.name( FIELD_MSG_SEVERITY );
    writer.value( value.getMsgCode().getSeverity().toString() );
    
    writer.name( FIELD_MSG_CODE );
    writer.value( value.getMsgCode().getCode() );
    
    writer.name( FIELD_DESCR );
    writer.value( value.getMsgDesc() );

    writer.endObject();
    }

else
    writer.nullValue();

return;
}



@Override
public ProcessingMessage read( JsonReader reader ) throws IOException
{
ProcessingMessage   rtrn    = null;

String              sevStr  = null;
String              msgCd   = null;
String              descr   = null;

reader.beginObject();
while (reader.hasNext())
    {
    JsonToken   token       = reader.peek();
    String      fieldName   = null;
    
    
    if (token.equals( JsonToken.NAME ))
        fieldName = reader.nextName();
    
    else
        fieldName = null;
    
    
    if (FIELD_MSG_CODE.equalsIgnoreCase( fieldName ))
        {
        token = reader.peek();        
        msgCd = reader.nextString();
        }
    
    else if (FIELD_MSG_SEVERITY.equalsIgnoreCase( fieldName ))
        {
        token  = reader.peek();
        sevStr = reader.nextString();
        }
    
    else if (FIELD_DESCR.equalsIgnoreCase( fieldName ))
        {
        token = reader.peek();
        descr = reader.nextString();
        }
    }

reader.endObject();

if (msgCd != null)
    {
    ProcessingMessageCode       msgCode = ProcessingMessageCode.fromString( msgCd );
    ProcessingMessageSeverity   msgSev  = ProcessingMessageSeverity.valueOf( sevStr );
    
    rtrn = new ProcessingMessage( false, msgCode );
    rtrn.getMsgCode().setSeverity( msgSev );
    rtrn.setMsgDesc( descr );
    }

return rtrn;
}
}
