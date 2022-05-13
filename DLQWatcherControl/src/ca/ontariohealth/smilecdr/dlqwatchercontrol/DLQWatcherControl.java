/**
 * 
 */
package ca.ontariohealth.smilecdr.dlqwatchercontrol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ontariohealth.smilecdr.BaseApplication;


/**
 * @author adminuser
 *
 */
public class DLQWatcherControl extends BaseApplication
{
static final Logger 			logr      = LoggerFactory.getLogger(DLQWatcherControl.class);

public static void main( String[] args )
{
logr.debug( "Entering: main" );

DLQWatcherControl	watcherCtrl = new DLQWatcherControl();
watcherCtrl.launch(args);

logr.debug( "Exiting: main");
return;
}



@Override
protected void launch() 
{
// TODO Auto-generated method stub
return;	
}

}
