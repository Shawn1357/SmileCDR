/**
 * 
 */
package ca.ontariohealth.smilecdr;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ontariohealth.smilecdr.support.MaxSizeStack;
import ca.ontariohealth.smilecdr.support.MyInstant;
import ca.ontariohealth.smilecdr.support.MyThread;
import ca.ontariohealth.smilecdr.support.commands.response.CSVTableGenerator;
import ca.ontariohealth.smilecdr.support.config.ConfigProperty;
import ca.ontariohealth.smilecdr.support.config.Configuration;

/**
 * @author shawn.brant
 *
 */
public class MemoryMonitorThread extends	MyThread 
{
private static	Logger							logr					= LoggerFactory.getLogger( MemoryMonitorThread.class );

private			Integer							maxMemSamples 		   	= null;
private			Integer							memPollIntervalMinutes 	= null;
private 		Long							pollIntervalMillis      = null;
private			Long							idleWaitMillis			= null;
private			MyInstant						nextScheduledSnap       = null;
private			MaxSizeStack<MemorySnapshot>	memSnaps 				= null;



protected MemoryMonitorThread( Configuration appCfg )
{
super(appCfg);

logr.debug( "Entering: {} Simple Constructor", MemoryMonitorThread.class.getSimpleName() );

initThreadParms();

logr.debug( "Exiting: {} Simple Constructor", MemoryMonitorThread.class.getSimpleName() );

return;
}




public MemoryMonitorThread( Configuration appCfg, Integer maxMemorySamples, Integer memoryPollIntervalMinutes )
{
super(appCfg);

logr.debug( "Entering: {} Full Constructor", MemoryMonitorThread.class.getSimpleName() );

maxMemSamples          = maxMemorySamples;
memPollIntervalMinutes = memoryPollIntervalMinutes;

initThreadParms();

logr.debug( "Exiting: {} Full Constructor", MemoryMonitorThread.class.getSimpleName() );
return;
}




private void	initThreadParms()
{
idleWaitMillis = appCfg.configLong( ConfigProperty.KAFKA_CONSUMER_POLL_INTERVAL );

if (maxMemSamples == null)
	maxMemSamples = appCfg.configInt( ConfigProperty.MAX_NUM_MEMORY_SAMPLES );

if (memPollIntervalMinutes == null)
	memPollIntervalMinutes = appCfg.configInt( ConfigProperty.MEMORY_SAMPLE_INTERVAL_MINS );

if (memPollIntervalMinutes != null)
	pollIntervalMillis = memPollIntervalMinutes.longValue() * 60L * 1000L;

else
	pollIntervalMillis = 60L * 60L * 1000L;


memSnaps = new MaxSizeStack<MemorySnapshot>( maxMemSamples );

return;
}



@Override
public void run() 
{
super.run();

// Start off with an initial memory snap shot.
// This will calculate when the next snap shot should take place.
takeMemorySnapshot( true );


while (!threadIndicatedToStop())
	{
	MyInstant	crntTS = MyInstant.now();
	
	if ((nextScheduledSnap == null) || (crntTS.compareTo(nextScheduledSnap ) > 0))
		{
		takeMemorySnapshot( true );
		}
	
	// Take a break.
	try
		{
		Thread.sleep( idleWaitMillis );
		} 
	
	catch (InterruptedException e)
		{
		// We don't really care if we have interrupted nap time.
		}
	}

return;
}



public  MemorySnapshot	takeMemorySnapshot()
{
return takeMemorySnapshot( false );
}




private MemorySnapshot	takeMemorySnapshot( boolean isScheduled )
{
MemorySnapshot	snap = memSnaps.push( new MemorySnapshot( isScheduled ) );

// Compute the time stamp the poller should wait until before taking another
nextScheduledSnap = new MyInstant( snap.snapshotTimestamp().getEpochMillis() + pollIntervalMillis );

return snap;
}




public	List<MemorySnapshot>	memorySnapshots()
{
return memSnaps;
}



public class MemorySnapshot implements CSVTableGenerator
{
private              boolean        isScheduledSnap       = true;
private				 MyInstant		snapshotTakenAt 	  = MyInstant.now();
private 			 long			maxMem          	  = Runtime.getRuntime().maxMemory();
private 			 long        	totalMem        	  = Runtime.getRuntime().totalMemory();
private 			 long			freeMem         	  = Runtime.getRuntime().freeMemory();


private static final DecimalFormat	ROUNDED_DECIMALFORMAT;
private static final double			MEGABYTE 			  = ((double) (1024 * 1024));
private static final String 		MIB 				  = "MB";

private static final String[]		CSV_HEADERS           = { "Snapshot Timestamp",
		                                     				  "Max Memory",
		                                     				  "Total Memory",
		                                     				  "Free Memory",
		                                     				  "Percent Used",
		                                     				  "Percent Free"
															};

static 
	{
    DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.ENGLISH);
    
    otherSymbols.setDecimalSeparator('.');
    otherSymbols.setGroupingSeparator(',');
    
    ROUNDED_DECIMALFORMAT = new DecimalFormat("####0.00", otherSymbols);
    ROUNDED_DECIMALFORMAT.setGroupingUsed(false);
	}




public static	String[]	csvColHeaders()
{
return CSV_HEADERS;
}



@Override
public String[] csvColumnHeaders()
{
return csvColHeaders();
}




@Override
public String[] csvColumnValues() 
{
String[]	rtrn = new String[ CSV_HEADERS.length ];

rtrn[ 0 ] = CSVTableGenerator.leftJustify(  CSV_HEADERS[ 0 ].length(), snapshotTakenAt.toString( appConfig(), ConfigProperty.TIMESTAMP_FORMAT ) );
rtrn[ 1 ] = CSVTableGenerator.rightJustify( CSV_HEADERS[ 1 ].length(), maxMemoryToMB() );
rtrn[ 2 ] = CSVTableGenerator.rightJustify( CSV_HEADERS[ 2 ].length(), totalMemoryToMB() );
rtrn[ 3 ] = CSVTableGenerator.rightJustify( CSV_HEADERS[ 3 ].length(), freeMemoryToMB() );
rtrn[ 4 ] = CSVTableGenerator.rightJustify( CSV_HEADERS[ 4 ].length(), String.format( "%s %s",  ROUNDED_DECIMALFORMAT.format( percentUsed() ), "%" ) );
rtrn[ 5 ] = CSVTableGenerator.rightJustify( CSV_HEADERS[ 5 ].length(), String.format( "%s %s",  ROUNDED_DECIMALFORMAT.format( percentFree() ), "%" ) );

return rtrn;
}


public MemorySnapshot()
{
isScheduledSnap = false;
return;
}




private MemorySnapshot( boolean isScheduled )
{
isScheduledSnap = isScheduled;
return;
}



public boolean		isScheduleTimestamp()
{
return isScheduledSnap;
}



public MyInstant	snapshotTimestamp()
{
return snapshotTakenAt;
}



private String	memValToMB( long mem )
{
double memMB = ((double) mem) / MEGABYTE;
return String.format( "%s %s",  ROUNDED_DECIMALFORMAT.format( memMB ), MIB );
}




public long freeMemory()
{
return freeMem;
}




public String	freeMemoryToMB()
{
return memValToMB( freeMem );
}





public long	maxMemory()
{
return	maxMem;
}



public String	maxMemoryToMB()
{
return memValToMB( maxMem );
}





public long totalMemory()
{
return totalMem;
}



public String	totalMemoryToMB()
{
return memValToMB( totalMem );
}




public double percentFree()
{
return (totalMem > 0) ? ((double) freeMem) / ((double) totalMem) * 100.0 : 0.0;
}


public double percentUsed()
{
return (totalMem > 0) ? ((double) (totalMem - freeMem)) / ((double) totalMem) * 100.0 : 100.0;
}
}
}
