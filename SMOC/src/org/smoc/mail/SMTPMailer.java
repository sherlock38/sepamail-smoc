package org.smoc.mail;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * The SMTPMailer class generates the missive email that will be sent to the recipient and sends a given MIME message
 * via SMTP.
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class SMTPMailer {

    private SmocAuthenticator authenticator;
    private String host;
    private String port;
    private Properties properties;
    private String recipientAddress;
    private String recipientName;
    private String senderAddress;
    private String senderName;
    private Session session;

    /**
     * Get SMTP mail properties
     * 
     * @return SMTP mail properties
     */
    public Properties getProperties() {
        return this.properties;
    }

    /**
     * Get session for SMTP mail
     * 
     * @return Session for SMTP mail
     */
    public Session getSession() {
        return this.session;
    }

    /**
     * Set the port for send SMTP a missive document email
     * 
     * @param port Port number for SMTP
     */
    public void setPort(String port) {

        // Set port value
        this.port = port;

        // Update mail session properties
        this.setMailerProperties();
    }

    /**
     * SMTPMailer class constructor
     * 
     * @param senderName Name of the missive document sender
     * @param senderAddress Email address of the missive document sender
     * @param recipientAddress Email address of the missive document recipient
     * @param host Address of the SMTP server
     * @param username User name required to log onto the SMTP server
     * @param password Password required to log onto the SMTP server
     */
    public SMTPMailer(String senderName, String senderAddress, String recipientAddress, String host, String username,
            String password) {

        // Initialise class attributes
        this.authenticator = new SmocAuthenticator(username, password);
        this.port = "25";
        this.properties = System.getProperties();
        this.recipientAddress = recipientAddress;
        this.recipientName = null;
        this.senderAddress = senderAddress;
        this.senderName = senderName;
        this.host = host;

        // SMTP mail session properties
        this.setMailerProperties();

        // Get a session for the MIME message
        this.session = Session.getInstance(this.properties, this.authenticator);
    }

    /**
     * SMTPMailer class constructor
     * 
     * @param senderName Name of the missive document sender
     * @param senderAddress Email address of the missive document sender
     * @param recipientName Name of the missive document recipient
     * @param recipientAddress Email address of the missive document recipient
     * @param host Address of the SMTP server
     * @param username User name required to log onto the SMTP server
     * @param password Password required to log onto the SMTP server
     */
    public SMTPMailer(String senderName, String senderAddress, String recipientName, String recipientAddress,
            String host, String username, String password) {

        // Initialise class attributes
        this.authenticator = new SmocAuthenticator(username, password);
        this.port = "25";
        this.properties = System.getProperties();
        this.recipientAddress = recipientAddress;
        this.recipientName = recipientName;
        this.senderAddress = senderAddress;
        this.senderName = senderName;
        this.host = host;

        // SMTP mail session properties
        this.setMailerProperties();

        // Get a session for the MIME message
        this.session = Session.getInstance(this.properties, this.authenticator);
    }

    /**
     * Create the SMTP MIME message body using the specified content and subject
     * 
     * @param subject Subject of the missive document email
     * @param content SMIME content of the missive document email
     * @return MIME message body with the given subject and SMIME content
     * @throws AddressException
     * @throws MessagingException 
     * @throws UnsupportedEncodingException
     */
    public MimeMessage createSmtpMessageBody(String subject, String content) throws AddressException,
            MessagingException, UnsupportedEncodingException {

        // Email sender address
        Address sender = new InternetAddress("\"" + this.senderName + "\"< " + this.senderAddress + " >");

        // Email recipient address
        Address recipient;
        if (null == this.recipientName) {
            recipient = new InternetAddress(this.recipientAddress);
        } else {
            recipient = new InternetAddress("\"" + this.recipientName + "\"< " + this.recipientAddress + " >");
        }

        // MIME message body
        MimeMessage body = new MimeMessage(this.session);

        // Set MIME message sender and recipient
        body.setFrom(sender);
        body.setRecipient(Message.RecipientType.TO, recipient);

        // Set the subject of the MIME message
        body.setSubject(subject);

        // Set the content of the MIME message
        body.setContent(content, "text/plain; charset=utf-8");

        // Save MIME message body
        body.saveChanges();

        return body;
    }

    /**
     * Send MIME message to missive XML document recipient
     * 
     * @param encryptedSignedContent Signed and encrypted message
     * @throws MessagingException
     */
    public void send(MimeMessage encryptedSignedContent) throws MessagingException {

        // Send the missive document
        Transport.send(encryptedSignedContent);
    }

    /**
     * Set the properties of the mail session
     */
    private void setMailerProperties() {

        // Properties of SMTP mail session
        this.properties.setProperty("mail.smtp.submitter",
                this.authenticator.getPasswordAuthentication().getUserName());
        this.properties.setProperty("mail.smtp.auth", "true");
        this.properties.setProperty("mail.smtp.host", this.host);
        this.properties.setProperty("mail.smtp.port", this.port);
    }
}
