/**
 * 
 */
package ca.ontariohealth.smilecdr.support;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * @author adminuser
 *
 */
public class MyInstant  implements Comparable<MyInstant>
{
private Long    millis  = null;


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



public LocalDateTime   asLocalDateTime()
{
LocalDateTime   rtrn = LocalDateTime.ofInstant( this.asInstant(), ZoneId.systemDefault() );

return rtrn;
}




@Override
public int compareTo( MyInstant o )
{
return millis.compareTo( o.millis );
}
}
