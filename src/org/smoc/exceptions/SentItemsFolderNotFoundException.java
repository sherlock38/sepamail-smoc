package org.smoc.exceptions;

/**
 * The SentItemsFolderNotFoundException class is the exception raised when the Sent Items folder could not be found on
 * the configured IMAP account.
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class SentItemsFolderNotFoundException extends Exception {
    
    /**
     * SentItemsFolderNotFoundException constructor
     * 
     * @param folderName Name of the Sent Items folder
     * @param host Address of the IMAP server
     * @param username User name required to log onto the IMAP server
     */
    public SentItemsFolderNotFoundException(String folderName, String host, String username) {

        // Initialise the parent class
        super("The Sent Items folder named " + folderName + " was not found on " + host + " for " + username + ".");
    }
}
