/**
 * 
 */
package ca.ontariohealth.smilecdr.support.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author shawn.brant
 *
 */
public class Configuration implements ConfigMap,
                                      ConfigAppNameAware,
                                      ConfigEnvironmentAware
{
/**
 * Identifies the name of the default file to load if no file is given
 * and/or there is a generic file from which to load from.
 */
private static final String			DEFAULT_FILE_NAME 		= "configuration.properties";

private static		 Configuration	defaultConfiguration 	= null;


private ApplicationName             appName                 = null;
private EnvironmentName				envName                 = null;
private Map<String,String>			cfgMap					= null;


public static void					loadDefaultConfiguration()
{
loadDefaultConfiguration( DEFAULT_FILE_NAME );

return;
}


public static void					loadDefaultConfiguration( String defaultFileNm )
{
loadDefaultConfiguration( null, null, defaultFileNm );
return;
}



public static void					loadDefaultConfiguration( ApplicationName	appNm,
		                                                      EnvironmentName   envNm,
		                                                      String            defaultFileNm )
{
defaultConfiguration = new Configuration();
defaultConfiguration.loadConfiguration(null, null, ((defaultFileNm != null) && (defaultFileNm.length() > 0)) ? defaultFileNm : DEFAULT_FILE_NAME );

return;	
}



public void		loadConfiguration( String fileNm )
{
loadConfiguration( null, null, fileNm );
return;
}



/**
 * @param fileNm
 * @param app
 * @return A Configuration Instance referencing the 
 */
public void		loadConfiguration( ApplicationName	appName,
		                           EnvironmentName  envName,
		                           String fileNm )
{
cfgMap  = new HashMap<String,String>();

InputStream	iStrm   = null;
Properties cfgProps = new Properties();

this.appName = appName;
this.envName = envName;

try 
	{
	
    // Loading properties file from the path
	//iStrm = new FileInputStream( fileNm );   
	iStrm = ClassLoader.getSystemResourceAsStream( fileNm );  
	cfgProps.load(iStrm);
	} 

catch (IOException e) 
	{
	e.printStackTrace();
	}

finally 
	{
    try 
    	{
    	if (iStrm != null)
    		{
    		iStrm.close();
    		}
    	} 
    
    catch (IOException e) 
    	{
    	e.printStackTrace();
    	}
	}

// Push all of the properties into the Configuration Map.
for (String crntKey : cfgProps.stringPropertyNames())
	{
	if (crntKey != null)
		{
		String	val = cfgProps.getProperty( crntKey );
		
		cfgMap.put( crntKey, val );
		}
	}

return;
}



@Override
public void setEnvironmentName(EnvironmentName envNm) 
{
envName = envNm;
}



@Override
public EnvironmentName getEnvironmentName() 
{
return envName;
}



@Override
public void setApplicationName(ApplicationName appNm) 
{
appName = appNm;	
}



@Override
public ApplicationName getApplicationName()
{
return appName; 
}



@Override
public Map<String, String> configMap() 
{
return cfgMap;
}



@Override
public boolean hasConfigItem(String configKey) 
{
boolean		rtrn = false;

if ((configKey != null) && (configKey.length() > 0))
	{
	String	appNm 			= appName != null ? appName.nrmlzdAppName() : null;
	String	envNm			= envName != null ? envName.nrmlzdEnvName() : null;
	int		keyPermutation 	= keyPermutationThatExists( appNm, envNm, configKey );
	
	rtrn = (keyPermutation >= 0);
	}
	
// If not found, try the default configuration map
if ((!rtrn) && (defaultConfiguration != null) && (this != defaultConfiguration))
	rtrn = defaultConfiguration.hasConfigItem( configKey );

return rtrn;
}



private int		keyPermutationThatExists( String appNm, String envNm, String cfgKey )
{
int		rtrn = -1;

for (int ndx = 0; (rtrn == -1) && (ndx < 11); ndx++)
	{
	String	cfgKy = genCrntConfigKey( appNm, envNm, cfgKey, ndx );
	
	if ((cfgKy != null) && (cfgMap.containsKey( cfgKy )))
		rtrn = ndx;
	}

return rtrn;
}



private	String	genCrntConfigKey( String appNm, String envNm, String cfgKy, int ndx )
{
String	rtrn = null;

// Generate Keys according to the supplied ndx parameter.
// If a key requires and element and that element is null, then
// the routine will return null.

//   App Env Key
//   App Key Env
//   Env App Key
//   Env Key App
//   Key App Env
//   Key Env App

//   App Key
//   Env Key
//   Key App
//   Key Env

//   Key

switch (ndx)
	{
	case  0:	if ((appNm != null) && (envNm != null)) rtrn = appNm.concat( "." ).concat( envNm ).concat( "." ).concat( cfgKy );	break;
	case  1:    if ((appNm != null) && (envNm != null)) rtrn = appNm.concat( "." ).concat( cfgKy ).concat( "." ).concat( envNm );	break;
	case  2:    if ((appNm != null) && (envNm != null)) rtrn = envNm.concat( "." ).concat( appNm ).concat( "." ).concat( cfgKy );	break;
	case  3:    if ((appNm != null) && (envNm != null)) rtrn = envNm.concat( "." ).concat( cfgKy ).concat( "." ).concat( appNm );	break;
	case  4:    if ((appNm != null) && (envNm != null)) rtrn = cfgKy.concat( "." ).concat( appNm ).concat( "." ).concat( envNm );	break;
	case  5:    if ((appNm != null) && (envNm != null)) rtrn = cfgKy.concat( "." ).concat( envNm ).concat( "." ).concat( appNm );	break;

	case  6:    if (appNm != null)						rtrn = appNm.concat( "." ).concat( cfgKy );									break;
	case  7:    if (envNm != null)						rtrn = envNm.concat( "." ).concat( cfgKy );									break;
	case  8:    if (appNm != null)						rtrn = cfgKy.concat( "." ).concat( appNm );									break;
	case  9:    if (envNm != null)						rtrn = cfgKy.concat( "." ).concat( envNm );									break;
	
	case 10:	rtrn = cfgKy;	break;
	
	default:	rtrn = null;	break;
	}

return rtrn;
}




@Override
public String configValue(String configKey)
{
String	rtrn 	= null;

if ((configKey != null) && (configKey.length() > 0))
	{
	String	envNm 	= envName != null ? envName.nrmlzdEnvName() : null;
	String  appNm	= appName != null ? appName.nrmlzdAppName() : null;
	
	int		keyPerm = keyPermutationThatExists( appNm, envNm, configKey );
	String  cfgKey  = genCrntConfigKey(appNm, envNm, configKey, keyPerm );
	
	if (cfgKey != null)
		rtrn = cfgMap.get( cfgKey );
	}

// If not found, try the default configuration map
if ((rtrn == null) && (defaultConfiguration != null) && (this != defaultConfiguration))
	rtrn = defaultConfiguration.configValue( configKey );

return rtrn;
}


@Override
public String configValue(String configKey, String defaultValue) 
{
String	rtrn = configValue( configKey );

if (rtrn == null)
	rtrn = defaultValue;

return rtrn;
}



}
