/**
 * 
 */
package ca.ontariohealth.smilecdr.dlqwatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ontariohealth.smilecdr.support.MyInstant;
import ca.ontariohealth.smilecdr.support.commands.DLQCommandOutcome;
import ca.ontariohealth.smilecdr.support.commands.DLQResponseContainer;
import ca.ontariohealth.smilecdr.support.commands.ProcessingMessage;
import ca.ontariohealth.smilecdr.support.commands.ProcessingMessageCode;
import ca.ontariohealth.smilecdr.support.config.ConfigProperty;
import ca.ontariohealth.smilecdr.support.config.Configuration;

/**
 * @author adminuser
 *
 */
public abstract class MyThread extends Thread
{
private static final Logger logr                        = LoggerFactory.getLogger( MyThread.class );

protected Configuration     appCfg                      = null;
private   boolean           continueRunning             = true;
private   Long              maxRunTime                  = null;
private   MyInstant         startTime                   = null;
private   MyInstant         endTime                     = null;



@SuppressWarnings("deprecation")
public static   DLQCommandOutcome        stopThread( DLQResponseContainer resp,
                                                     Configuration       appConfig,
                                                     MyThread             threadToStop, 
                                                     String               threadName )
{
DLQCommandOutcome   rslt = DLQCommandOutcome.SUCCESS;

if (threadToStop != null)
    {
    if (threadToStop.isAlive())
        {
        Long    maxWait = threadToStop.appConfig().configLong( ConfigProperty.STOP_THREAD_MAX_WAIT_MILLIS );
        
        /*
         * Set the flag for the thread to stop.
         * 
         */
        
        logr.debug( "{} Thread is running. Setting flag for it to stop.", threadName );         
        threadToStop.indicateToStopThread(); 
        
        
        /*
         * Wait a while to give the thread a chance to stop...
         * 
         */
        
        if (threadToStop.isAlive())
            logr.debug( "{} Thread is Alive, we need to wait up to {} milliseconds for it to end.", 
                        threadName,
                        maxWait );
        
        boolean     keepWaiting = true;
        MyInstant   killEnd     = new MyInstant( MyInstant.now().getEpochMillis() + maxWait );

        while (threadToStop.isAlive() && keepWaiting)
            {
            try
                {
                logr.debug( "Waiting..." );
                Thread.sleep( 250 );
                } 
            catch (InterruptedException e)
                {
                e.printStackTrace();
                }
            
            keepWaiting = (killEnd.compareTo( MyInstant.now() ) > 0);
            }
        
        /*
         * If the target thread is still running even after the maximum wait
         * time, assume it is hung up for some reason and kill it without
         * prejudice.
         * 
         */

        if (threadToStop.isAlive())
            {
            // Thread is still alive after timeout. Kill it.
            ProcessingMessage procMsg = new ProcessingMessage( ProcessingMessageCode.DLQW_0006, 
                                                               appConfig, 
                                                               threadName,
                                                               maxWait.toString() );
            logr.debug( procMsg.getMsgDesc() );
            threadToStop.stop();
            
            if (resp != null)
                resp.addProcessingMessage( procMsg );
            
            if (rslt.asPriority() < DLQCommandOutcome.WARNINGS.asPriority())
                rslt = DLQCommandOutcome.WARNINGS;
            }
        
        threadToStop = null;
        }
    
    else
        {
        ProcessingMessage procMsg = new ProcessingMessage( ProcessingMessageCode.DLQW_0004, appConfig, threadName );
        logr.debug( procMsg.getMsgDesc() );
        
        if (resp != null)
            resp.addProcessingMessage( procMsg );
        }
    }

else
    {
    logr.debug( "Thread to be stopped is null. Nothing to do." );
    }


return rslt;
}





protected   MyThread( Configuration appCfg )
{
super();
if (appCfg == null)
    throw new IllegalArgumentException( "Application Configuration parameter must not be null" );

this.appCfg = appCfg;

return;
}




@Override
public void run()
{
startTime = MyInstant.now();

Long cfgMaxTime = appCfg.configLong( ConfigProperty.QUIT_AFTER_MILLIS, null );
if (cfgMaxTime != null)
    setMaxRunTime( cfgMaxTime );


return;
}




public MyInstant    getThreadStartTime()
{
return startTime;
}




public void indicateToStopThread() 
{
logr.debug( "Flagging thread to stop running." );
this.continueRunning = false;
}




public boolean  threadIndicatedToStop()
{
return !continueRunning;
}



protected Configuration appConfig()
{
return appCfg;
}



public Long getMaxRunTime()
{
return maxRunTime;
}


public void setMaxRunTime(Long maxRunTime)
{
this.maxRunTime = maxRunTime;
if ((startTime != null) && (maxRunTime != null) && (maxRunTime > 0L))
    endTime = new MyInstant( startTime.getEpochMillis() + maxRunTime );

else if ((maxRunTime == null) || (maxRunTime <= 0L))
    endTime = null;
 
return;
}



public MyInstant    timeForThreadEnd()
{
return endTime;
}


public boolean  scheduledEndTimeElapsed()
{
boolean elapsed = false;

if (endTime != null)
    elapsed = MyInstant.now().compareTo( endTime ) >= 0;

return elapsed;
}
}
