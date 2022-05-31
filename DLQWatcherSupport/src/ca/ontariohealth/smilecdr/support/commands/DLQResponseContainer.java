/**
 * 
 */
package ca.ontariohealth.smilecdr.support.commands;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * @author adminuser
 *
 */
public class DLQResponseContainer
{
private UUID                    respID                  = UUID.randomUUID();
private Long                    createTS                = System.currentTimeMillis();
private Long                    processingCompleteTS    = null;
private DLQCommandContainer     srcCommand              = null;
private DLQCommandOutcome       processingOutcome       = null;
private List<String>            reportLines             = new LinkedList<>();

public DLQResponseContainer()
{
return;
}



public DLQResponseContainer( DLQCommandContainer srcCmd )
{
setSourceCommand( srcCmd );
return;
}



public DLQResponseContainer( DLQCommandContainer srcCmd, DLQCommandOutcome outcome )
{
setSourceCommand( srcCmd );
setOutcome( outcome );
return;
}



public UUID getResponseUUID()
{
return respID;
}




public void setSourceCommand( DLQCommandContainer srcCmd )
{
srcCommand = srcCmd;
return;
}



public DLQCommandContainer getSourceCommand()
{
return srcCommand;
}



public void setOutcome( DLQCommandOutcome procOutcome )
{
processingOutcome = procOutcome;
return;
}



public DLQCommandOutcome getOutcome()
{
return processingOutcome;
}



public void addReportLine( String rprtLine )
{
if (rprtLine != null)
    reportLines.add( rprtLine );

return;
}



public void     recordCompleteTimestamp()
{
recordCompleteTimestamp( null );
return;
}




public void     recordCompleteTimestamp( Long timestamp )
{
processingCompleteTS = timestamp != null ? timestamp : System.currentTimeMillis();
return;
}




public Long     getCreatedTimestamp()
{
return createTS;
}




public Long     getCompletedTimestamp()
{
return processingCompleteTS;
}
}
