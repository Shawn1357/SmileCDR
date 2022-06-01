/**
 * 
 */
package ca.ontariohealth.smilecdr.support.commands.json;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * @author adminuser
 *
 */
public class InstantAdapter extends TypeAdapter<Instant>
{
private static  String  FIELD_EPOCH_MILLIS  = "epochMillis";
private static  String  FIELD_ISO8601_STR   = "iso8601Format";
private static  String  FIELD_LOCAL_TS      = "localDateTime";

private static  ZoneId  systemZone          = ZoneId.systemDefault();
private static  DateTimeFormatter   lclFmt  = DateTimeFormatter.ofPattern( "yyyy-MM-dd HH:mm:ss" );

@Override
public void write (JsonWriter writer, Instant value) throws IOException
{
if (value != null)
    {
    Long            epochMillis   = Long.valueOf( value.toEpochMilli() );
    String          iso8601Fmt    = value.toString();
    LocalDateTime   localDateTime = LocalDateTime.ofInstant( value, systemZone );
    
    writer.beginObject();
    
    writer.name( FIELD_EPOCH_MILLIS );
    writer.value( epochMillis );
    writer.name( FIELD_LOCAL_TS );
    writer.value( lclFmt.format( localDateTime ) );
    writer.name( FIELD_ISO8601_STR );
    writer.value( iso8601Fmt );
    
    writer.endObject();
    }

else
    writer.nullValue();

return;
}



@Override
public Instant read( JsonReader reader ) throws IOException
{
Instant    rtrn = null;

reader.beginObject();
while (reader.hasNext())
    {
    JsonToken   token       = reader.peek();
    String      fieldName   = null;
    
    
    if (token.equals( JsonToken.NAME ))
        fieldName = reader.nextName();
    
    else
        fieldName = null;
    
    // Only need the Epoch Millis.
    // Everything else is a convenience and can be ignored.
    if (FIELD_EPOCH_MILLIS.equals( fieldName ))
        {
        token   = reader.peek();
        Long millis = reader.nextLong();
        
        rtrn = Instant.ofEpochMilli( millis );
        }
    }

reader.endObject();

return rtrn;
}
}
