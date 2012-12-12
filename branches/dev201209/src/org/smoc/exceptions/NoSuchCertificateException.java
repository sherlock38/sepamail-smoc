package org.smoc.exceptions;

/**
 * The InvalidConfigurationException class is the exception raised when required certificate was not found in the given
 * certificate store.
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class NoSuchCertificateException extends Exception {

    /**
     * NoSuchCertificateException class constructor
     * 
     * @param keyAlias Key alias
     * @param keyStoreFilename Key store path and filename 
     */
    public NoSuchCertificateException(String keyAlias, String keyStoreFilename) {

        // Initialise parent class
        super("The certificate with the alias " + keyAlias + " could not be found in the key store at "
                + keyStoreFilename + ".");
    }
}
