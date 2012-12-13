package org.smoc.mail;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * SmocAuthenticator class creates an SMTP authenticator using the given username and password pair.
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class SmocAuthenticator extends Authenticator {

    private PasswordAuthentication authentication;

    /**
     * SmocAuthenticator constructor
     * 
     * @param username Username on SMTP host
     * @param password Password for corresponding username on SMTP host
     */
    public SmocAuthenticator(String username, String password) {
        authentication = new PasswordAuthentication(username, password);
    }

    /**
     * Get password authentication object instance
     * 
     * @return Password authentication object instance
     */
    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return authentication;
    }
}
