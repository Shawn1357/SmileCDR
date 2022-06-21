/**
 * 
 */
package ca.ontariohealth.smilecdr.support.commands;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.google.gson.Gson;

import ca.ontariohealth.smilecdr.support.MyInstant;
import ca.ontariohealth.smilecdr.support.commands.response.CWMDLQRecordEntry;
import ca.ontariohealth.smilecdr.support.commands.response.KeyValue;
import ca.ontariohealth.smilecdr.support.commands.response.ReportRecord;

/**
 * @author adminuser
 *
 */
public class DLQResponseContainer
{
private UUID                    respID                      = UUID.randomUUID();
private MyInstant               createTS                    = MyInstant.now();
private MyInstant               processingCompleteTS        = null;
private MyInstant               receivedResponseTS          = null;
private DLQCommandContainer     srcCommand                  = null;
private DLQCommandOutcome       processingOutcome           = null;
private int                     processingMessageCount      = 0;
private List<ProcessingMessage> processingMessages          = new LinkedList<>();
private int                     reportLineCount             = 0;
private List<ReportRecord>      reportLines                 = new LinkedList<>();




public static DLQResponseContainer   fromJSON( String jsonStr )
{
DLQResponseContainer rtrn = null;

if ((jsonStr != null) && (jsonStr.length() > 0))
    {
    Gson    gson = new Gson();
    
    rtrn = gson.fromJson( jsonStr, DLQResponseContainer.class );
    }

return rtrn;
}






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



public void addProcessingMessage( ProcessingMessage newMsg )
{
if (newMsg != null)
    {
    processingMessages.add( newMsg );
    processingMessageCount++;
    }

return;
}



public int  getProcessingMessageCount()
{
return processingMessageCount;
}



public void addReportEntry( String rprtLine )
{
if (rprtLine != null)
    {
    reportLines.add( new ReportRecord( rprtLine ) );
    reportLineCount++;
    }

return;
}



public void addReportEntry( KeyValue keyVal )
{
if (keyVal != null)
    {
    reportLines.add( new ReportRecord( keyVal ) );
    reportLineCount++;
    }

return;
}



public void addReportEntry( CWMDLQRecordEntry dlqEntry )
{
if (dlqEntry != null)
    {
    reportLines.add( new ReportRecord( dlqEntry ) );
    reportLineCount++;
    }

return;
}



public int  getReportEntryCount()
{
return reportLineCount;
}




public void     recordCompleteTimestamp()
{
recordCompleteTimestamp( null );
return;
}




public void     recordCompleteTimestamp( MyInstant timestamp )
{
processingCompleteTS = timestamp != null ? timestamp : MyInstant.now();
return;
}




public MyInstant     getCreatedTimestamp()
{
return createTS;
}




public MyInstant     getCompletedTimestamp()
{
return processingCompleteTS;
}




public  void       setReceivedResponseTimestamp( MyInstant timestamp )
{
if (timestamp != null)
    receivedResponseTS = timestamp;

else
    receivedResponseTS = MyInstant.now();

return;
}



public MyInstant      getReceivedResponseTimestamp()
{
return receivedResponseTS;
}
}
