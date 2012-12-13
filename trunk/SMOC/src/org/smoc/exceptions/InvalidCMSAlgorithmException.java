package org.smoc.exceptions;

/**
 * The InvalidCMSAlgorithmException class is the exception raised when the CMS algorithm specified is not valid.
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class InvalidCMSAlgorithmException extends Exception {

    /**
     * InvalidCMSAlgorithmException default constructor
     */
    public InvalidCMSAlgorithmException() {

        // Initialise the parent class
        super("The CMS algorithm is not valid.");
    }
    
    /**
     * InvalidCMSAlgorithmException constructor
     * 
     * @param cmsName Specified CMS algorithm name
     */
    public InvalidCMSAlgorithmException(String cmsName) {

        // Initialise the parent class
        super("The CMS algorithm " + cmsName + " is not valid.");
    }
}
