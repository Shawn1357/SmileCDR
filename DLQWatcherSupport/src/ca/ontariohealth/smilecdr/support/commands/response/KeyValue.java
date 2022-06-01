/**
 * 
 */
package ca.ontariohealth.smilecdr.support.commands.response;

/**
 * @author adminuser
 *
 */
public class KeyValue
{
private String  key     = null;
private String  value   = null;


public  KeyValue( String keyNm, String val )
{
setKey( keyNm );
setValue( val );

return;
}


public String getKey()
{
return key;
}


public void setKey( String keyNm )
{
this.key = keyNm;
}


public String getValue()
{
return value;
}


public void setValue( String val )
{
this.value = val;
return;
}

}
