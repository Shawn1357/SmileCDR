/**
 * 
 */
package ca.ontariohealth.smilecdr.support.email;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ca.ontariohealth.smilecdr.support.config.ConfigProperty;
import ca.ontariohealth.smilecdr.support.config.Configuration;

/**
 * @author adminuser
 *
 */
public class EMailTemplateDetails
{
private static final Logger                 logr                = LoggerFactory.getLogger( EMailTemplateDetails.class );

private              Configuration          appConfig           = null;
private              ConfigProperty         templateProp        = null;
private              String                 templateName        = null;

private              Address                fromEMailAddr       = null;
private              String                 toEMailAddrsStr     = null;
private              Address[]              toEMailAddrs        = null;
private              String                 ccEMailAddrsStr     = null;
private              Address[]              ccEMailAddrs        = null;
private              String                 bccEMailAddrsStr    = null;
private              Address[]              bccEMailAddrs       = null;
private              String                 emailSubject        = null;
private              Boolean                inclComments        = null;
private              String                 templateFileNm      = null;




public enum EMailAddressType
{
TO,
CC,
BCC
};



public EMailTemplateDetails( Configuration appCfg, ConfigProperty emailTemplateProp ) throws AddressException
{
if ((appCfg == null) || (emailTemplateProp == null))
    throw new IllegalArgumentException( EMailTemplateDetails.class.getSimpleName() + " constructor parameters must not be null" );

logr.debug( "Entering: {} Constructor", EMailTemplateDetails.class.getSimpleName() );

appConfig    = appCfg;
templateProp = emailTemplateProp;

loadTemplateDetails();

logr.debug( "Exiting: {} Constructor", EMailTemplateDetails.class.getSimpleName() );
return;
}



public Address  fromEMailAddress()
{
return fromEMailAddr;
}



public Address[]   toEMailAddresses()
{
return toEMailAddrs;
}



public Address[] ccEMailAddresses()
{
return ccEMailAddrs;
}



public Address[] bccEMailAddresses()
{
return bccEMailAddrs;
}




public  Address[]    emailAddresses( EMailAddressType addrType )
{
Address[]   rtrn = null;

if (addrType != null)
    {
    switch (addrType)
        {
        case TO:    rtrn = toEMailAddresses();      break;
        case CC:    rtrn = ccEMailAddresses();      break;
        case BCC:   rtrn = bccEMailAddresses();     break;
        default:    rtrn = null;                    break;
        }
    }

else
    rtrn = null;


return rtrn;
}




public String   subjectLine()
{
return emailSubject;
}



public  boolean inclBodyCommentLines()
{
return inclComments.booleanValue();
}



public  String  bodyTemplateFileName()
{
return templateFileNm;
}



private void    loadTemplateDetails() throws AddressException
{
templateName = appConfig.configValue( templateProp );
if ((templateName != null) && (templateName.length() > 0))
    {
    fromEMailAddr    = new InternetAddress( emailPropertyValue( ConfigProperty.EMAIL_FROM_ADDR ) );
    
    toEMailAddrsStr  = emailPropertyValue( ConfigProperty.EMAIL_TO_ADDRS );
    toEMailAddrs     = InternetAddress.parse( toEMailAddrsStr );
    
    ccEMailAddrsStr  = emailPropertyValue( ConfigProperty.EMAIL_CC_ADDRS );
    ccEMailAddrs     = InternetAddress.parse( ccEMailAddrsStr );
    
    bccEMailAddrsStr = emailPropertyValue( ConfigProperty.EMAIL_BCC_ADDRS );
    bccEMailAddrs    = InternetAddress.parse( bccEMailAddrsStr );
    
    emailSubject   = emailPropertyValue( ConfigProperty.EMAIL_SUBJECT );
    inclComments   = Boolean.valueOf( emailPropertyValue( ConfigProperty.EMAIL_INCL_HASHTAG_LINES ) );
    templateFileNm = emailPropertyValue( ConfigProperty.EMAIL_BODY_FILE_NM );    
    }


return;
}


private String  emailPropertyValue( ConfigProperty  cfgProp )
{
String  rtrn = null;

if (cfgProp != null)
    {
    String  propertyName = cfgProp.propertyName().concat( "." ).concat( templateName );
    
    rtrn = appConfig.configValue( propertyName ); 
    }


return rtrn;
}
}
