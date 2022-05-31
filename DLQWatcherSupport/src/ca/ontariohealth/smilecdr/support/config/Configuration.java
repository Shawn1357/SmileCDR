/**
 * 
 */
package ca.ontariohealth.smilecdr.support.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ontariohealth.smilecdr.support.config.source.ConfigSource;
import ca.ontariohealth.smilecdr.support.config.source.PropertiesConfigSource;

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
public class Configuration implements ConfigMap< PropertiesConfigSource >,
                                      ConfigAppNameAware,
                                      ConfigEnvironmentAware
{
private static final Logger	logr = LoggerFactory.getLogger(Configuration.class);

private ApplicationName                                 appName     = null;
private EnvironmentName				                    envName     = null;
private ConfigSource<Map<String, ArrayList<String>>>    cfgSrc      = null;

private String                                          lastFileNm  = null;
private Properties                                      lastProps   = null;

private HashMap<String, PropertyPermutationInfo>        propLookup  = new HashMap<>();



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




public  Configuration()
{
logr.debug("Entering: Default {} Constructor", Configuration.class.getSimpleName() );
logr.debug("Exiting: Default {} Constructor", Configuration.class.getSimpleName() );
return;
}



public  Configuration( String   propsFileNm )
{
logr.debug( "Entering: {} File Name Constructor.", Configuration.class.getSimpleName() );

loadConfiguration( propsFileNm );

logr.debug( "Exiting: {} File Name Constructor", Configuration.class.getSimpleName() );
return;
}



public  Configuration( Properties   props )
{
logr.debug( "Entering: {} Properties Constructor.", Configuration.class.getSimpleName() );

loadConfiguration( props );

logr.debug( "Exiting: {} Properties Constructor", Configuration.class.getSimpleName() );
return;
}




public void		loadConfiguration( String fileNm )
{
loadConfiguration( appName, envName, fileNm );
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
this.appName    = appName;
this.envName    = envName;

this.lastFileNm = fileNm;
this.lastProps  = null;

this.cfgSrc     = new PropertiesConfigSource( fileNm );
this.propLookup.clear();

return;
}




public  void    loadConfiguration( Properties props )
{
loadConfiguration( appName, envName, props );
return;
}



public  void    loadConfiguration( ApplicationName  appName,
                                   EnvironmentName  envName,
                                   Properties       props )
{
this.appName    = appName;
this.envName    = envName;

this.lastFileNm = null;
this.lastProps  = props;

this.cfgSrc     = new PropertiesConfigSource( props );
this.propLookup.clear();

return;
}


@Override
public void setEnvironmentName(EnvironmentName envNm) 
{
envName = envNm;

if (lastFileNm != null)
    loadConfiguration( lastFileNm );

else if (lastProps != null)
    loadConfiguration( lastProps );

return;
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

if (lastFileNm != null)
    loadConfiguration( lastFileNm );

else if (lastProps != null)
    loadConfiguration( lastProps );

return;
}



@Override
public ApplicationName getApplicationName()
{
return appName; 
}



@Override
public Map<String, ArrayList<String>> configMap() 
{
return cfgSrc.getAllConfigValues();
}



@Override
public boolean hasConfigItem( String configKey ) 
{
boolean		rtrn = hasConfigItem( configKey, 1 );

return rtrn;
}



@Override
public boolean hasConfigItem( String configKey, Integer ndx )
{
boolean     rtrn = false;

if ((configKey != null) && (configKey.length() > 0))
    {
    String  val = configValue( configKey, ndx, null );
    
    rtrn = (val != null);
    }
    
return rtrn;
}





private PropertyPermutationInfo		keyPermutationThatExists( String  cfgKey )
{
PropertyPermutationInfo		rtrn = propLookup.get( cfgKey );

/*
 * If we have not already located the property in the configuration source,
 * search for it and cache that lookup so we don't need to search again.
 * 
 */

if (rtrn == null)
    {
    for (PropertyNamePermutation perm : PropertyNamePermutation.values())
    	{
    	String	cfgKy = perm.propertyName( appName, envName, cfgKey );
    	
    	if (cfgKy != null)
    	    {
    	    ArrayList<String>  vals = cfgSrc.getConfigValues( cfgKy );
    	    
    	    if ((vals != null) && (vals.size() > 0))
    	        {
    	        rtrn = new PropertyPermutationInfo( cfgKey, perm, cfgKy );
    	        propLookup.put( cfgKey,  rtrn );
    	        
    	        break;
    	        }
    	    }
    	}
    }

return rtrn;
}





@Override
public Integer configValueCount( String configKey )
{
ArrayList<String>   vals = configValues( configKey );
Integer             rtrn = vals.size();

return rtrn;
}



@Override
public ArrayList<String>    configValues( String configKey )
{
ArrayList<String>       rtrn     = null;
PropertyPermutationInfo permInfo = keyPermutationThatExists( configKey );

if (permInfo != null)
    rtrn = cfgSrc.getConfigValues( permInfo.fullPropNm );

if (rtrn == null)
    rtrn = new ArrayList<>();
    
return rtrn;
}




@Override
public String configValue( String configKey )
{
String  rtrn = configValue( configKey, 1, null );

return rtrn;
}



@Override
public String configValue( String configKey, String defaultValue ) 
{
String	rtrn = configValue( configKey, 1, defaultValue );

return rtrn;
}



@Override
public String configValue( String configKey, Integer ndx, String defaultValue )
{
String  rtrn    = null;

if ((configKey != null) && (configKey.length() > 0))
    {
    ArrayList<String>   vals = configValues( configKey );
    
    
    if ((ndx == null) || (ndx <= 0)) 
        ndx = 1;
    
    if (vals != null)
        {
        try
            {
            rtrn = vals.get( ndx - 1 );
            }
        
        catch (IndexOutOfBoundsException iob)
            {
            rtrn = null;
            }
        }
    }

if (rtrn == null)
    rtrn = defaultValue;

return rtrn;
}



@Override
public boolean hasConfigItem( ConfigProperty cfgProp ) 
{
boolean rtrn = hasConfigItem( cfgProp, 1 );
return  rtrn;
}







@Override
public boolean hasConfigItem( ConfigProperty cfgProp, Integer ndx )
{
boolean rtrn = false;

if (cfgProp != null)
    {
    // If the config property has a hard coded default, then we know,
    // at the very least, this property has a value.
    
    rtrn = cfgProp.hasHardCodedDefault();
    
    // If it doesn't then look to see if we can find the property in the
    // application configuration or default configuration.
    if (!rtrn)
        rtrn = hasConfigItem( cfgProp.propertyName(), ndx );
    }
 
return rtrn;
}




@Override
public Integer configValueCount( ConfigProperty cfgProp )
{
ArrayList<String> vals = configValues( cfgProp );
Integer           rtrn = vals.size();

return rtrn;
}



@Override
public ArrayList<String> configValues( ConfigProperty cfgProp )
{
ArrayList<String>   rtrn = null;

if (cfgProp != null)
    rtrn = configValues( cfgProp.propertyName() );

if (rtrn == null)
    {
    rtrn = new ArrayList<>();
    
    if (cfgProp.hasHardCodedDefault())
        rtrn.add( cfgProp.hardCodedDefaultValue() );
    }

return rtrn;
}



@Override
public String configValue( ConfigProperty cfgProp ) 
{
String rtrn = configValue( cfgProp, 1, null );

return rtrn;
}





@Override
public String configValue( ConfigProperty cfgProp, Integer ndx )
{
String  rtrn = configValue( cfgProp, ndx, null );

return rtrn;
}



@Override
public String configValue( ConfigProperty cfgProp, String defaultValue )
{
String      rtrn = configValue( cfgProp, 1, defaultValue );

return rtrn;
}



@Override
public String configValue( ConfigProperty cfgProp, Integer ndx, String defaultValue ) 
{
String rtrn = null;

if (cfgProp != null)
	rtrn = configValue( cfgProp.propertyName(), ndx, defaultValue );

else
    rtrn = defaultValue;

if ((rtrn == null) && (cfgProp.hasHardCodedDefault()))
    rtrn = cfgProp.hardCodedDefaultValue();

return rtrn;
}




@Override
public Boolean configBool(ConfigProperty cfgProp) throws IllegalArgumentException 
{
Boolean		rtrn	= null;

if (cfgProp != null)
	{
	String propName = cfgProp.propertyName();
	
	try
		{
		rtrn = configBool( propName );
		}
	
	catch (IllegalArgumentException iae)
		{
		if (cfgProp.hasHardCodedDefault())
			rtrn = BooleanUtils.toBoolean( cfgProp.hardCodedDefaultValue() );
		
		else
			throw iae;
		}
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
	
	try
		{
		rtrn = configInt( propName );
		}
	
	catch (IllegalArgumentException iae)
		{
		if (cfgProp.hasHardCodedDefault())
			rtrn = Integer.valueOf( cfgProp.hardCodedDefaultValue() );
		
		else
			throw iae;
		}
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



private enum    PropertyNamePart
{
APP,
ENV,
KEY
};


private enum    PropertyNamePermutation
{
APP_ENV_KEY( PropertyNamePart.APP, PropertyNamePart.ENV, PropertyNamePart.KEY ),
APP_KEY_ENV( PropertyNamePart.APP, PropertyNamePart.KEY, PropertyNamePart.ENV ),
ENV_APP_KEY( PropertyNamePart.ENV, PropertyNamePart.APP, PropertyNamePart.KEY ),
ENV_KEY_APP( PropertyNamePart.ENV, PropertyNamePart.KEY, PropertyNamePart.APP ),
KEY_APP_ENV( PropertyNamePart.KEY, PropertyNamePart.APP, PropertyNamePart.ENV ),
KEY_ENV_APP( PropertyNamePart.KEY, PropertyNamePart.ENV, PropertyNamePart.APP ),

APP_KEY( PropertyNamePart.APP, PropertyNamePart.KEY ),
ENV_KEY( PropertyNamePart.ENV, PropertyNamePart.KEY ),
KEY_APP( PropertyNamePart.KEY, PropertyNamePart.APP ),
KEY_ENV( PropertyNamePart.KEY, PropertyNamePart.ENV ),

KEY( PropertyNamePart.KEY );


private PropertyNamePart[]    parts = new PropertyNamePart[3];


PropertyNamePermutation( PropertyNamePart part1 )
{
parts[0]  = part1;
parts[1]  = null;
parts[2]  = null;

return;
}




PropertyNamePermutation( PropertyNamePart part1,
                         PropertyNamePart part2 )
{
parts[0]  = part1;
parts[1]  = part2;
parts[2]  = null;

return;
}




PropertyNamePermutation( PropertyNamePart part1,
                         PropertyNamePart part2,
                         PropertyNamePart part3 )
{
parts[0]  = part1;
parts[1]  = part2;
parts[2]  = part3;

return;
}



public String   propertyName( ApplicationName    appNm,
                              EnvironmentName    envNm,
                              String             propKey )
{
String  rtrn = "";

for (int ndx = 0; (ndx < parts.length) && (rtrn != null); ndx++)
    {
    if (parts[ndx] != null)
        {
        switch (parts[ndx])
            {
            case    APP:
                if ((appNm != null) && (appNm.nrmlzdAppName().length() > 0))
                    rtrn = rtrn.concat( rtrn.length() > 0 ? "." : "" )
                               .concat( appNm.nrmlzdAppName() );
                
                else
                    rtrn = null;
                
                break;
                
                
            case    ENV:
                if ((envNm != null) && (envNm.nrmlzdEnvName().length() > 0))
                    rtrn = rtrn.concat( rtrn.length() > 0 ? "." : "" )
                               .concat( envNm.nrmlzdEnvName() );
                
                else
                    rtrn = null;
                
                break;
                
                
            case    KEY:
                if ((propKey != null) && (propKey.length() > 0))
                    rtrn = rtrn.concat( rtrn.length() > 0 ? "." : "" )
                               .concat( propKey.toLowerCase().strip() );
                
                else
                    rtrn = null;
                
                break;
                
                
            default:
                rtrn = null;
                break;
            }
        }
    }

return rtrn;
}
}



private class   PropertyPermutationInfo
{
public  String                      propKey     = null;
public  PropertyNamePermutation     perm        = null;
public  String                      fullPropNm  = null;


public PropertyPermutationInfo( String                      propertyKey,
                                PropertyNamePermutation     permutation,
                                String                      fullPropName )
{
propKey     = propertyKey;
perm        = permutation;
fullPropNm  = fullPropName;

return;
}

@Override
public int hashCode()
{
if (propKey != null)
    return propKey.hashCode();

else
    return super.hashCode();
}




@Override
public boolean equals( Object obj )
{
if (propKey != null)
    return propKey.equals( obj );

else
    return super.equals( obj );
}




@Override
public String toString()
{
if (propKey != null)
    return propKey;

else
    return super.toString();
}
}
}
