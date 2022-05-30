/**
 * 
 */
package ca.ontariohealth.smilecdr.support.commands.json;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import ca.ontariohealth.smilecdr.support.commands.DLQCommandParam;

/**
 * @author adminuser
 *
 */
public class CommandParamAdapter extends TypeAdapter<DLQCommandParam>
{
private static  String  PARAM_FIELD_NAME    = "name";
private static  String  PARAM_FIELD_VALUE   = "value";

@Override
public void write (JsonWriter writer, DLQCommandParam value) throws IOException
{
if (value != null)
    {
    String  paramNm  = value.paramName();
    String  paramVal = value.paramValue();
    
    if (paramNm != null)
        paramNm = paramNm.strip();
    
    else
        paramNm = "";
    
    if (paramVal != null)
        paramVal = paramVal.strip();
    
    else
        paramVal = "";
    
    
    writer.beginObject();
    
    writer.name( PARAM_FIELD_NAME );
    writer.value( paramNm );
    writer.name( PARAM_FIELD_VALUE );
    writer.value( paramVal );
    
    writer.endObject();
    }

else
    writer.nullValue();

return;
}



@Override
public DLQCommandParam read( JsonReader reader ) throws IOException
{
DLQCommandParam    newParam = null;
String          paramNm  = null;
String          paramVal = null;

reader.beginObject();
while (reader.hasNext())
    {
    JsonToken   token       = reader.peek();
    String      fieldName   = null;
    
    
    if (token.equals( JsonToken.NAME ))
        fieldName = reader.nextName();
    
    if (PARAM_FIELD_NAME.equals( fieldName ))
        {
        token   = reader.peek();
        paramNm = reader.nextString();
        }
      
    else if (PARAM_FIELD_VALUE.equals( fieldName ))
        {
        token = reader.peek();
        paramVal = reader.nextString();
        }
    
    newParam = new DLQCommandParam( paramNm, paramVal );
    }

reader.endObject();

return newParam;
}
}
