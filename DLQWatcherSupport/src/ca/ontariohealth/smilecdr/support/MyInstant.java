/**
 * 
 */
package ca.ontariohealth.smilecdr.support;

import java.time.Instant;

/**
 * @author adminuser
 *
 */
public class MyInstant
{
private Long    millis = null;


public static   MyInstant   now()
{
MyInstant rtrn = new MyInstant();

return rtrn;
}



public MyInstant()
{
setEpochMillis( null );
return;
}



public MyInstant( Long epochMillis )
{
setEpochMillis( epochMillis );
return;
}



public void  setEpochMillis( Long epochMillis )
{
if (epochMillis == null)
    millis = System.currentTimeMillis();

else
    millis = epochMillis;

return;
}




public Long  getEpochMillis()
{
return millis;
}



public Instant  asInstant()
{
Instant rtrn = Instant.ofEpochMilli( millis );
return rtrn;
}
}
