package org.smoc.mail;

import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.MimeMessage;
import org.smoc.exceptions.SentItemsFolderNotFoundException;

/**
 * The IMAPSynchronizer class synchronizes the Sent Items content of the pre-defined IMAP account with the missive
 * document sent to the recipient via SMTP.
 *
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 0.1
 */
public class IMAPSynchronizer {

    private String folder;
    private String host;
    private String password;
    private Properties properties;
    private String protocol;
    private String username;

    /**
     * IMAPSynchronizer class constructor
     * 
     * @param host Address of the IMAP server
     * @param username User name required to log onto the IMAP server
     * @param password Password required to log onto the IMAP server
     * @param protocol Protocol required to connect to the IMAP server (imap or imaps)
     * @param folder Name of the "Sent items" folder
     */
    public IMAPSynchronizer(String host, String username, String password, String protocol, String folder) {

        // Initialise the class attributes of the class
        this.folder = folder;
        this.host = host;
        this.password = password;
        this.properties = System.getProperties();
        this.protocol = protocol;
        this.username = username;

        // Set the properties for the IMAP connection session
        this.properties.setProperty("mail.store.protocol", protocol);
    }

    /**
     * Synchronize the sent missive XML message with the "Sent items" folder of the IMAP account
     * 
     * @param message Message sent via SMTP
     * @throws NoSuchProviderException
     * @throws MessagingException
     * @throws SentItemsFolderNotFoundException
     */
    public void synchronize(MimeMessage message) throws NoSuchProviderException,
            MessagingException, SentItemsFolderNotFoundException {

        // Session for accessing IMAP store
        Session session = Session.getInstance(this.properties, null);

        // IMAP store
        Store store = session.getStore(this.protocol);

        // Connect to the IMAP server
        store.connect(this.host, this.username, this.password);
        
        // Get the Sent Items folder
        Folder sentItemsFolder = store.getFolder(this.folder);

        // Check if the configured Sent Items folder exists
        if (sentItemsFolder.exists()) {

            // Open the Sent Items folder for reading and writing
            sentItemsFolder.open(Folder.READ_WRITE);

            // Messages array
            Message[] messages = new Message[1];

            // Add message sent to the message array
            messages[0] = message;

            // Append the message to the Sent Items folder
            sentItemsFolder.appendMessages(messages);

            // Close the folder
            sentItemsFolder.close(true);

        } else {

            // We throw an exception to indicate that the folder does not exist
            throw new SentItemsFolderNotFoundException(this.folder, this.host, this.username);
        }
    }
}
