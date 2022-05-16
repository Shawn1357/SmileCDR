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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.commons.lang3.BooleanUtils;

/**
 * Holds the configuration values for an application and provides a mechanism
 * to retrieve those configuration values.
 * There are two possible levels of configuration values:
 * <li>The application specific configuration value set.</li>
 * <li>A default set of configuration values.</li>
 * <p>
 * Each of these configuration sources must be of a Properties file format.
 * <p>
 * Each of these configuration sources must be locate-able on the class path.
 * <p>
 * If a requested configuration item can not be found in the application
 * configuration set, the same configuration item will be searched in the
 * default configuration set (if it is set).
 * <p>
 * There are two additional parameters which affect how a configuration
 * property is looked up:
 * <li>The Application Name</li>
 * <li>The Environment Name</li>
 * @author shawn.brant
 *
 */
public class Configuration implements ConfigMap,
                                      ConfigAppNameAware,
                                      ConfigEnvironmentAware
{
private static final Logger	logr = LoggerFactory.getLogger(Configuration.class);

/**
 * Identifies the name of the default file to load if no file is given
 * and/or there is a generic file from which to load from.
 */
private static final String			DEFAULT_FILE_NAME 		= "configuration.properties";

private static		 Configuration	defaultConfiguration 	= null;


private ApplicationName             appName                 = null;
private EnvironmentName				envName                 = null;
private Map<String,String>			cfgMap					= null;


/**
 * Name of the class which will serialize Keys from a Kafka Producer.
 * This class Name MUST match the Producer and Consumer instance types for the
 * Key in the generic types:
 * <pre>
 *     org.apache.kafka.clients.producer.Producer
 *     org.apache.kafka.clients.consumer.Consumer
 * </pre>
 */

public static final String			KAFKA_KEY_SERIALIZER_CLASS_NAME 	= "org.apache.kafka.common.serialization.StringSerializer";


/**
 * Name of the class which will serialize Values from a Kafka Producer.
 * This class Name MUST match the Producer and Consumer instance types for the
 * Value in the generic types:
 * <pre>
 *     org.apache.kafka.clients.producer.Producer
 *     org.apache.kafka.clients.consumer.Consumer
 * </pre>
 */

public static final String			KAFKA_VALUE_SERIALIZER_CLASS_NAME	= "org.apache.kafka.common.serialization.StringSerializer";


/**
 * Name of the class which will de-serialize Keys in a Kafka Consumer.
 * This class Name MUST match the Producer and Consumer instance types for the
 * Key in the generic types:
 * <pre>
 *     org.apache.kafka.clients.producer.Producer
 *     org.apache.kafka.clients.consumer.Consumer
 * </pre>
 */

public static final String			KAFKA_KEY_DESERIALIZER_CLASS_NAME	= "org.apache.kafka.common.serialization.StringDeserializer";


/**
 * Name of the class which will de-serialize Values in a Kafka Consumer.
 * This class Name MUST match the Producer and Consumer instance types for the
 * Value in the generic types:
 * <pre>
 *     org.apache.kafka.clients.producer.Producer
 *     org.apache.kafka.clients.consumer.Consumer
 * </pre>
 */

public static final String			KAFKA_VALUE_DESERIALIZER_CLASS_NAME	= "org.apache.kafka.common.serialization.StringDeserializer";

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
	
	if (iStrm != null)
		cfgProps.load(iStrm);
	
	else
		logr.error("Unable to open configuration file: {}", fileNm );
	} 

catch (IOException e) 
	{
	logr.error( "Unable to load configuration from: {}", fileNm );
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



@Override
public boolean hasConfigItem( ConfigProperty cfgProp ) 
{
boolean rtrn = false;

if (cfgProp != null)
	rtrn = hasConfigItem( cfgProp.propertyName() );
	
return rtrn;
}



@Override
public String configValue( ConfigProperty cfgProp ) 
{
String rtrn = null;

if (cfgProp != null)
	rtrn = configValue( cfgProp.propertyName() );

return rtrn;
}



@Override
public String configValue( ConfigProperty cfgProp, String defaultValue ) 
{
String rtrn = null;

if (cfgProp != null)
	rtrn = configValue( cfgProp.propertyName(), defaultValue );

return rtrn;
}


@Override
public Boolean configBool(ConfigProperty cfgProp) throws IllegalArgumentException 
{
Boolean		rtrn	= null;

if (cfgProp != null)
	{
	String propName = cfgProp.propertyName();
	
	rtrn = configBool( propName );
	}

else
	throw new IllegalArgumentException( "Requested Configuration Property must not be null" );

return rtrn;
}




@Override
public Boolean configBool(ConfigProperty cfgProp, Boolean defaultValue) 
{
String      propNm = (cfgProp != null) ? cfgProp.propertyName() : null;
Boolean		rtrn   = configBool( propNm, defaultValue );

return rtrn;
}


@Override
public Boolean configBool(String cfgKey) throws IllegalArgumentException 
{
Boolean		rtrn = null;

if ((cfgKey != null) && (cfgKey.length() > 0))
	{
	rtrn = configBool( cfgKey, null );

	if (rtrn == null)
		throw new IllegalArgumentException( "Unable to find or unable to convert value for property '" +
											cfgKey +
											"' to a Boolean." );
	}

else
	throw new IllegalArgumentException( "Requested Configuration Property Name must not be null or zero-length." );


return rtrn;
}




@Override
public Boolean configBool(String cfgKey, Boolean defaultValue) 
{
Boolean		rtrn   = null;
String		cfgVal = configValue( cfgKey );

if (cfgVal != null)
	{
	rtrn = BooleanUtils.toBooleanObject( cfgVal );
	
	if (rtrn == null)
		rtrn = defaultValue;
	}

return rtrn;
}


@Override
public Integer configInt(ConfigProperty cfgProp) throws IllegalArgumentException 
{
Integer		rtrn	= null;

if (cfgProp != null)
	{
	String propName = cfgProp.propertyName();
	
	rtrn = configInt( propName );
	}

else
	throw new IllegalArgumentException( "Requested Configuration Property must not be null" );

return rtrn;
}


@Override
public Integer configInt(ConfigProperty cfgProp, Integer defaultValue) 
{
String      propNm = (cfgProp != null) ? cfgProp.propertyName() : null;
Integer		rtrn   = configInt( propNm, defaultValue );

return rtrn;
}


@Override
public Integer configInt(String cfgKey) throws IllegalArgumentException 
{
Integer		rtrn = null;

if ((cfgKey != null) && (cfgKey.length() > 0))
	{
	rtrn = configInt( cfgKey, null );

	if (rtrn == null)
		throw new IllegalArgumentException( "Unable to find or unable to convert value for property '" +
											cfgKey +
											"' to an Integer." );
	}

else
	throw new IllegalArgumentException( "Requested Configuration Property Name must not be null or zero-length." );


return rtrn;
}


@Override
public Integer configInt(String cfgKey, Integer defaultValue) 
{
Integer		rtrn   = null;
String		cfgVal = configValue( cfgKey );

if (cfgVal != null)
	{
	rtrn = Integer.valueOf( cfgVal );
	
	if (rtrn == null)
		rtrn = defaultValue;
	}

return rtrn;
}


}
