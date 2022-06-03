/**
 * 
 */
package ca.ontariohealth.smilecdr.support.config;

import java.io.BufferedReader;

/**
 * @author adminuser
 *
 */
public interface BasicAuthCredentials
{
public void     loadCredentials( BufferedReader rdr );
public String   username();
public String   password();
}
