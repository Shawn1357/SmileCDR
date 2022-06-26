/**
 * 
 */
package ca.ontariohealth.smilecdr.support;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import ca.ontariohealth.smilecdr.support.config.ConfigProperty;
import ca.ontariohealth.smilecdr.support.config.Configuration;

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



public String		toString( String dtTmFormatPattern )
{
String	rtrn = null;

if ((dtTmFormatPattern != null) && (dtTmFormatPattern.length() > 0))
	{
	DateTimeFormatter frmtr = DateTimeFormatter.ofPattern( dtTmFormatPattern )
			                                   .withZone( ZoneId.systemDefault() );
	
	rtrn = frmtr.format( asInstant() );
	}

else
	rtrn = String.valueOf( getEpochMillis() );

return rtrn;
}



public String       toString( Configuration appConfig, ConfigProperty tsFmtProperty )
{
String  tsFmtPattern = appConfig.configValue( tsFmtProperty );
return toString( tsFmtPattern );
}




public String       toString( Configuration appConfig, String customTSFmtProperty )
{
String  tsFmtPattern = appConfig.configValue( customTSFmtProperty );
return toString( tsFmtPattern );
}





@Override
public int compareTo( MyInstant o )
{
return millis.compareTo( o.millis );
}
}
