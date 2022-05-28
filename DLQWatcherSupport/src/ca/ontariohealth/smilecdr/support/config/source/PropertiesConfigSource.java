/**
 * 
 */
package ca.ontariohealth.smilecdr.support.config.source;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author adminuser
 *
 */
public class PropertiesConfigSource implements ConfigSource<Map<String, ArrayList<String>>> 
{
private static final Logger							     logr 				= LoggerFactory.getLogger(PropertiesConfigSource.class);


private				 Map<String, ArrayList<String>>      cfgProps			= new HashMap<String, ArrayList<String>>();
private static final ArrayList<String>                   emptyList          = new ArrayList<String>();

private static final String							     IS_GRP_PROP_PTRN 	= ".*\\.([0-9]+)$";
	


public PropertiesConfigSource( final String  propsFileNm )
{
logr.debug( "Entering: PropertiesConfigSource(String)" );

if ((propsFileNm != null) && (propsFileNm.length() > 0))
    {
    InputStream iStrm       = null;
    Properties  cfgProps    = null;

    try 
        {   
        // Loading properties file from the path
        iStrm = ClassLoader.getSystemResourceAsStream( propsFileNm ); 
        
        if (iStrm != null)
            {
            cfgProps = new Properties();
            cfgProps.load( iStrm );            
            }
        
        else
            {
            logr.error("Unable to open configuration file: {}", propsFileNm );
            throw new IllegalArgumentException( "Unable to open configuration file: " + propsFileNm );
            }
        } 

    catch (IOException e) 
        {
        cfgProps = null;
        logr.error( "Unable to load configuration from: {}", propsFileNm );
        throw new IllegalArgumentException( "Unable to load from configuration file: " + propsFileNm, e );
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
        
        finally
            {
            iStrm = null;
            }
        }
    
    if (cfgProps != null)
        loadConfig( cfgProps );
    }

else
    throw new IllegalArgumentException( "Properties File Name must not be null or zero-length." );

logr.debug( "Exiting: PropertiesConfigSource(String)" );
return;
}





public	PropertiesConfigSource( Properties  cfgSrc )
{
logr.debug( "Entering Constructor: {}", PropertiesConfigSource.class.getSimpleName() );

if (cfgSrc != null)
    loadConfig( cfgSrc );

else
    throw new IllegalArgumentException( "Properties parameter must not be null" );


logr.debug( "Exiting Constructor: {}", PropertiesConfigSource.class.getSimpleName() );
return;
}




/**
 * @param cfgSrc
 * @throws IllegalArgumentException
 */
private	void	loadConfig( Properties cfgSrc )
{
logr.debug( "Entering: loadConfig" );

boolean loadCfg = cfgSrc != null;

if (loadCfg)
	{
	cfgProps.clear();
	
	Set<String>	       propNms     = cfgSrc.stringPropertyNames();
	SortedSet<String>  sortedProps = new TreeSet<>( propNms );
	
	for (String crntKey : sortedProps)
		{
		if (crntKey != null)
		    {
    		PropEntry crntEntry       = parsePropKey( crntKey );
    		String	  crntVal         = cfgSrc.getProperty( crntKey );
    		
    		ArrayList<String> valList = null;
    		
    		if (cfgProps.containsKey( crntEntry.propKey ))
                {
                valList = cfgProps.get( crntEntry.propKey );
                valList.ensureCapacity( crntEntry.propNdx );
                }
    		
    		else
                {
                valList = new ArrayList<String>( 10 );
                cfgProps.put( crntEntry.propKey, valList );
                }
    		
    		valList.add( crntVal );
       		}
		}
	}

logr.debug( "Exiting: loadConfig" );
return;	
}




private PropEntry	parsePropKey( String propKey )
{
PropEntry	rtrn = new PropEntry();

if (propKey != null)
	{
	rtrn.rawKey = propKey.strip();
	
	/*
	 * The group name is of one of the forms:
	 *     prop.name.123 = some value
	 *     
	 *     or 
	 *     
	 *     prop.name     = some value
	 *     
	 * If we match the pattern, we are guaranteed to be of the first from
	 * meaning the property name ends with a string of digits preceded by a
	 * period ('.').
	 * 
	 */
	
	String grpName = rtrn.rawKey;
	if (grpName.matches( IS_GRP_PROP_PTRN ))
	    {
	    int    lastDotNdx = grpName.lastIndexOf( '.' );
	   
	    if (lastDotNdx > 1)
	        {
	        try
	            {
	            rtrn.propKey = grpName.substring( 0, lastDotNdx ).strip();
	            rtrn.propNdx = Integer.valueOf( grpName.substring( lastDotNdx + 1 ) );
	            }
	        
	        catch (IndexOutOfBoundsException iob)
	            {
	            rtrn.propKey = rtrn.rawKey;
	            }
	        }
	    }
	
	else
	    rtrn.propKey = rtrn.rawKey;
	}

return rtrn;
}



@Override
public Map<String, ArrayList<String>> getAllConfigValues() 
{
Map<String, ArrayList<String>>	rtrn = cfgProps;
return rtrn;
}





@Override
public ArrayList<String> getConfigValues(String configPropNm) 
{
ArrayList<String>   rtrn = emptyList;

if ((configPropNm != null) && (configPropNm.length() > 0) && (cfgProps.containsKey( configPropNm )))
    rtrn = cfgProps.get( configPropNm );


return rtrn;
}



private class   PropEntry
{
public String   rawKey  = null;
public String   propKey = null;
public Integer  propNdx = 1;
}

}
