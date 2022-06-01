/**
 * 
 */
package ca.ontariohealth.smilecdr.support.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ca.ontariohealth.smilecdr.support.MyInstant;
import ca.ontariohealth.smilecdr.support.commands.json.CommandParamAdapter;

/**
 * @author adminuser
 *
 */
public class DLQCommandContainer
{
private static Logger                       logr                = LoggerFactory.getLogger( DLQCommandContainer.class );

private UUID                                cmdID               = UUID.randomUUID();
private MyInstant                           createTS            = MyInstant.now();
private MyInstant                           processingStartedTs = null;
private String                              issueChannelName    = null;
private String                              responseChannelName = null;
private DLQCommand                          commandToIssue      = null;
private List<DLQCommandParam>               params              = new ArrayList<>();



public static DLQCommandContainer   fromJSON( String jsonStr )
{
DLQCommandContainer rtrn = null;

if ((jsonStr != null) && (jsonStr.length() > 0))
    {
    Gson    gson = new Gson();
    
    rtrn = gson.fromJson( jsonStr, DLQCommandContainer.class );
    }

return rtrn;
}





public  DLQCommandContainer()
{
logr.debug( "Entering: {} Default Constructor", DLQCommandContainer.class.getSimpleName() );
logr.debug( "Exiting: {} Default Constructor", DLQCommandContainer.class.getSimpleName() );
return;
}



public  DLQCommandContainer( String                             issueChannelNm,
                             String                             responseChannelNm,
                             DLQCommand                         command,
                             List<DLQCommandParam>              cmdParams )
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



public MyInstant getCreateTimestamp()
{
return createTS;
}




public void recordProcessingStartTimestamp()
{
recordProcessingStartTimestamp( null );
return;
}



public void recordProcessingStartTimestamp( MyInstant timestamp )
{
processingStartedTs = (timestamp != null) ? timestamp : MyInstant.now();
return;
}



public MyInstant getProcessingStartTimestamp()
{
return processingStartedTs;
}



public String   toJSON( GsonBuilder  bldr )
{
Gson    xltrToJSON   = bldr.create();
String  jsonCmd      = xltrToJSON.toJson( this );

return jsonCmd;
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

DLQCommandParam    param1 = new DLQCommandParam( "Param 1", "Value 1" );
DLQCommandParam    param2 = new DLQCommandParam( "Param 2", "Value 2" );

cmd.getCommandParams().add( param1 );
cmd.getCommandParams().add( param2 );


Gson    xltrToJSON = builder.create();
String  cmdAsJSON  = xltrToJSON.toJson( cmd );

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
