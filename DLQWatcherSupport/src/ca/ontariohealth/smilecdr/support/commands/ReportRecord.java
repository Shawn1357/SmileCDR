/**
 * 
 */
package ca.ontariohealth.smilecdr.support.commands;

/**
 * @author adminuser
 *
 */
public class ReportRecord
{
private ReportRecordType    rcrdType        = null;
private String              rcrdString      = null;
private DLQRecordEntry      rcrdDLQEntry    = null;



public ReportRecord( String rcdStr )
{
setRcrdString( rcdStr );
return;
}



public ReportRecord( DLQRecordEntry rcdDLQEntry )
{
setRcrdDLQEntry( rcdDLQEntry );
return;
}



public ReportRecordType getRcrdType()
{
return rcrdType;
}



private void setRcrdType( ReportRecordType rcrdType )
{
this.rcrdType = rcrdType;
return;
}




public String getRcrdString()
{
return rcrdString;
}



private void setRcrdString( String rcrdString )
{
setRcrdType( ReportRecordType.STRING );
this.rcrdString   = rcrdString;
this.rcrdDLQEntry = null;
return;
}




public DLQRecordEntry getRcrdDLQEntry()
{
return rcrdDLQEntry;
}




public void setRcrdDLQEntry( DLQRecordEntry rcrdDLQEntry )
{
setRcrdType( ReportRecordType.DLQ_ENTRY_SPEC );
this.rcrdString   = null;
this.rcrdDLQEntry = rcrdDLQEntry;
return;
}
}
