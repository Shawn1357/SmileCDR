/**
 * 
 */
package ca.ontariohealth.smilecdr.support.commands;

import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * @author adminuser
 *
 */
public class DLQResponseContainer
{
private UUID                    respID                      = UUID.randomUUID();
private Instant                 createTS                    = Instant.now();
private Instant                 processingCompleteTS        = null;
private Instant                 receivedResponseTS          = null;
private DLQCommandContainer     srcCommand                  = null;
private DLQCommandOutcome       processingOutcome           = null;
private List<ReportRecord>      reportLines                 = new LinkedList<>();

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



public void addReportEntry( String rprtLine )
{
if (rprtLine != null)
    reportLines.add( new ReportRecord( rprtLine ) );

return;
}



public void addReportEntry( DLQRecordEntry dlqEntry )
{
if (dlqEntry != null)
    reportLines.add( new ReportRecord( dlqEntry ) );

return;
}



public void     recordCompleteTimestamp()
{
recordCompleteTimestamp( null );
return;
}




public void     recordCompleteTimestamp( Instant timestamp )
{
processingCompleteTS = timestamp != null ? timestamp : Instant.now();
return;
}




public Instant     getCreatedTimestamp()
{
return createTS;
}




public Instant     getCompletedTimestamp()
{
return processingCompleteTS;
}




public  void       setReceivedResponseTimestamp( Instant timestamp )
{
if (timestamp != null)
    receivedResponseTS = timestamp;

else
    receivedResponseTS = Instant.now();

return;
}



public Instant      getReceivedResponseTimestamp()
{
return receivedResponseTS;
}
}
