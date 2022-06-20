/**
 * 
 */
package ca.ontariohealth.smilecdr.support.commands.json;

import com.google.gson.GsonBuilder;

import ca.ontariohealth.smilecdr.support.MyInstant;
import ca.ontariohealth.smilecdr.support.commands.DLQCommandParam;
import ca.ontariohealth.smilecdr.support.commands.ProcessingMessage;
import ca.ontariohealth.smilecdr.support.commands.response.ReportRecord;

/**
 * @author adminuser
 *
 */
public class JSONApplicationSupport
{
public static void  registerGsonTypeAdpaters( GsonBuilder builder )
{
if (builder == null)
    throw new IllegalArgumentException( "Gson Builder Argument must not be null" );

builder.registerTypeAdapter( DLQCommandParam.class,   new CommandParamAdapter() );
builder.registerTypeAdapter( ReportRecord.class,      new ReportRecordAdapter() );
builder.registerTypeAdapter( MyInstant.class,         new MyInstantAdapter() );
builder.registerTypeAdapter( ProcessingMessage.class, new ProcessingMessageAdapter() );

return;
}
}
