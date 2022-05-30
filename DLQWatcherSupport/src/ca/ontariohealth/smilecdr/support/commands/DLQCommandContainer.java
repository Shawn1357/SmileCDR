/**
 * 
 */
package ca.ontariohealth.smilecdr.support.commands;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import ca.ontariohealth.smilecdr.support.commands.json.CommandParamAdapter;

/**
 * @author adminuser
 *
 */
public class DLQCommandContainer
{
private static Logger                       logr                = LoggerFactory.getLogger( DLQCommandContainer.class );

private UUID                                cmdID               = UUID.randomUUID();
private long                                createTS            = System.currentTimeMillis();
private String                              issueChannelName    = null;
private String                              responseChannelName = null;
private DLQCommand                          commandToIssue      = null;
private List<DLQCommandParam>               params              = new ArrayList<>();


public  DLQCommandContainer()
{
logr.debug( "Entering: {} Default Constructor", DLQCommandContainer.class.getSimpleName() );
logr.debug( "Exiting: {} Default Constructor", DLQCommandContainer.class.getSimpleName() );
return;
}



public  DLQCommandContainer( String                             issueChannelNm,
                             String                             responseChannelNm,
                             DLQCommand                         command,
                             List<DLQCommandParam>                 cmdParams )
{
logr.debug( "Entering {} Constructor", DLQCommandContainer.class.getSimpleName() );

setIssueChannelName( issueChannelNm );
setResponseChannelName( responseChannelNm );
setCommandToIssue( command );
setCommandParams( cmdParams );

logr.debug( "Exiting {} Constructor", DLQCommandContainer.class.getSimpleName() );
return;
}




public UUID getCommandUUID()
{
return cmdID;
}




public long getCreateTimestamp()
{
return createTS;
}




public void setIssueChannelName( String channelName )
{
if ((channelName == null) || (channelName.length() == 0))
    throw new IllegalArgumentException( "Channel Name must not be null or zero length." );

issueChannelName = channelName;
return;
}



public final String getIssueChannelName()
{
return issueChannelName;
}



public void setResponseChannelName( String channelName )
{
if ((channelName == null) || (channelName.length() == 0))
    throw new IllegalArgumentException( "Channel Name must not be null ro zero length." );

responseChannelName = channelName;
return;
}



public final String getResponseChannelName()
{
return responseChannelName;
}




public void setCommandToIssue( DLQCommand  cmdToIssue )
{
if (cmdToIssue == null)
    throw new IllegalArgumentException( "Command to Issue must not be null." );

commandToIssue = cmdToIssue;
return;
}




public DLQCommand  getCommandToIssue()
{
return commandToIssue;
}





public void setCommandParams( List<DLQCommandParam>    cmdParams )
{
params.clear();
if (cmdParams != null)
    params.addAll( cmdParams );

return;
}




public List<DLQCommandParam>  getCommandParams()
{
return params;
}




public static void main( String[] args )
{
GsonBuilder builder = new GsonBuilder();

builder.registerTypeAdapter( DLQCommandParam.class, new CommandParamAdapter() );
builder.setPrettyPrinting();


DLQCommandContainer cmd  = new DLQCommandContainer();

cmd.setIssueChannelName( "Issue.Channel.Name" );
cmd.setResponseChannelName( "Response.ChannelName" );
cmd.setCommandToIssue( DLQCommand.HELLO );

/*
List<DLQCommandParam>   parms  = new ArrayList<>();
*/

DLQCommandParam    param1 = new DLQCommandParam( "Param 1", "Value 1" );
DLQCommandParam    param2 = new DLQCommandParam( "Param 2", "Value 2" );

cmd.getCommandParams().add( param1 );
cmd.getCommandParams().add( param2 );

//cmd.setCommandParams( parms );


Gson    xltrToJSON = builder.create();
String  cmdAsJSON = xltrToJSON.toJson( cmd );

System.out.println( "Command Container in JSON form:" );
System.out.println( cmdAsJSON );

Gson                xltrFromJSON = builder.create();
DLQCommandContainer rtrndCmd     = xltrFromJSON.fromJson( cmdAsJSON, DLQCommandContainer.class );

Gson                dummy        = builder.create();
String              forOutput    = dummy.toJson( rtrndCmd );

System.out.println( "Command Container from JSON form:" );
System.out.println( forOutput );
return;
}
}
