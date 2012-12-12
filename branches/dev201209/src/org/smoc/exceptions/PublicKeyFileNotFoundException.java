package org.smoc.exceptions;

import java.io.FileNotFoundException;

/**
 * The PublicKeyFileNotFoundException class is the exception raised when the public key file for the email recipient
 * could not be found.
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class PublicKeyFileNotFoundException extends FileNotFoundException {

    /**
     * PublicKeyFileNotFoundException default constructor
     */
    public PublicKeyFileNotFoundException() {

        // Initialise the parent class
        super("The public key file of the email recipient could not be found.");
    }

    /**
     * PublicKeyFileNotFoundException constructor
     * 
     * @param filename Public key filename
     */
    public PublicKeyFileNotFoundException(String filename) {

        // Initialise the parent class
        super("The public key file " + filename + ", of the email recipient, could not be found.");
    }
}
