/**
 * 
 */
package ca.ontariohealth.smilecdr.support.commands;

/**
 * @author adminuser
 *
 */
public enum DLQCommandOutcome
{
SUCCESS(   0 ),
WARNINGS(  1 ),
ERROR(     2 ),
FATAL(     3 );

private int pri;
private DLQCommandOutcome( int priority ) { pri = priority;  return; }
public  int asPriority() { return pri; };
}
