/**
 * 
 */
package ca.ontariohealth.smilecdr.support.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author adminuser
 *
 */
public class FileBasedCredentials implements BasicAuthCredentials
{
private String      usernm = null;
private String      passwd = null;


public FileBasedCredentials( String fileNm )
{
loadCredentials( fileNm );
return;
}



public void loadCredentials( String fileNm )
{
if ((fileNm != null) && (fileNm.length() > 0))
    {
    InputStream   is = getClass().getClassLoader().getResourceAsStream( fileNm );
    loadCredentials( is );
    try
        {
        is.close();
        }
    catch (IOException e)
        {
        // TODO Auto-generated catch block
        e.printStackTrace();
        }
    }

return;
}




public void loadCredentials( InputStream inputStrm )
{
if (inputStrm != null)
    {
    BufferedReader  rdr = new BufferedReader( new InputStreamReader( inputStrm ) );
    loadCredentials( rdr );
    try
        {
        rdr.close();
        }
    catch (IOException e)
        {
        // TODO Auto-generated catch block
        e.printStackTrace();
        }
    }

return;
}




@Override
public void loadCredentials( BufferedReader rdr )
{
// All lines are key/value pairs with the first '=' as the separator.
try
    {
    if ((rdr != null) && (rdr.ready()))
        {
        while (rdr.ready())
            {
            String      nextLine = null;
            
            try
                {
                nextLine = rdr.readLine().strip();
                }
            
            catch (IOException e)
                {
                nextLine = null;
                e.printStackTrace();
                break;
                }
            
            if ((nextLine != null) && (nextLine.length() > 0) && (!nextLine.startsWith( "#" )))
                {
                String[]    parts = nextLine.split( "=", 2 );
                
                if (parts.length == 2)
                    {
                    if ("username".equalsIgnoreCase( parts[0] ) && (parts[1] != null))
                        usernm = parts[1].strip();
                    
                    else if ("password".equalsIgnoreCase( parts[0] ) && (parts[1] != null))
                        passwd = parts[1].strip();
                    }
                }
            }
        }
    }

catch (IOException e)
    {
    e.printStackTrace();
    }

return;
}



@Override
public String username()
{
return usernm;
}



@Override
public String password()
{
return passwd;
}

}
