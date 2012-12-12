package org.smoc;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.MimeMessage;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.operator.OperatorCreationException;
import org.smoc.cryptograhy.RecipientKeyStore;
import org.smoc.cryptograhy.SenderKeyStore;
import org.smoc.cryptograhy.Smime;
import org.smoc.cryptograhy.SmocSigner;
import org.smoc.exceptions.*;
import org.smoc.mail.IMAPSynchronizer;
import org.smoc.mail.SMTPMailer;
import org.smoc.utils.ConfigReader;
import org.smoc.utils.SmocFileUtils;

/**
 * The Smoc class provides methods which allow to sign and hash a missive XML file, send the signed and hashed missive
 * XML file via SMTP using SMIME encapsulation and synchronize the sent mails list over IMAP.
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class Smoc {

    private boolean hasConfiguration;
    private boolean hasValidConfiguration;
    private IMAPSynchronizer imapSynchronizer;
    private MailcapCommandMap mailcap;
    private HashMap<String, String> smocConfig;
    private SMTPMailer smtpMailer;

    /**
     * Smoc class default constructor
     * 
     * @param configFilename SMOC module configuration file path and name
     * @throws ConfigurationFileNotFoundException
     * @throws InvalidConfigurationException
     * @throws IOException
     */
    public Smoc(String configFilename) throws ConfigurationFileNotFoundException, IOException,
            InvalidConfigurationException {

        // Initialise class attributes
        this.hasConfiguration = false;
        this.hasValidConfiguration = false;
        this.mailcap = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        this.smocConfig = new HashMap<>();

        // Set the default command map
        this.setCommandMap();

        // Add Bouncy Castle security provider
        Security.addProvider(new BouncyCastleProvider());

        try {

            // Configuration file reader instance
            ConfigReader configReader = new ConfigReader(configFilename);

            // Configuration has been found
            this.hasConfiguration = true;

            // Parse the configuration file
            this.smocConfig = configReader.parse();

            // Configuration file is valid
            this.hasValidConfiguration = true;

            // Check if the name of the recipient has been defined in the configuration file
            if (smocConfig.containsKey("recipient.name")) {

                // Mail content generator instance with the name of the recipient
                this.smtpMailer = new SMTPMailer(smocConfig.get("sender.name"), smocConfig.get("sender.address"),
                        smocConfig.get("recipient.name"), smocConfig.get("recipient.address"),
                        smocConfig.get("smtp.host"), smocConfig.get("smtp.username"), smocConfig.get("smtp.password"));

            } else {

                // Mail content generator instance with the name of the recipient
                this.smtpMailer = new SMTPMailer(smocConfig.get("sender.name"), smocConfig.get("sender.address"),
                        smocConfig.get("recipient.address"), smocConfig.get("smtp.host"),
                        smocConfig.get("smtp.username"), smocConfig.get("smtp.password"));
            }

            // Set port of SMTP host if defined
            if (smocConfig.containsKey("smtp.port")) {
                this.smtpMailer.setPort(smocConfig.get("smtp.port"));
            }

            // Instance of IMAP synchronization class
            this.imapSynchronizer = new IMAPSynchronizer(smocConfig.get("imap.host"), smocConfig.get("imap.username"),
                    smocConfig.get("imap.password"), smocConfig.get("imap.protocol"), smocConfig.get("imap.folder"));

        } catch (ConfigurationFileNotFoundException ex) {

            // Configuration file was not found
            this.hasConfiguration = false;

            // Throw the raised exception
            throw ex;

        } catch (InvalidConfigurationException ex) {

            // Configuration file is not valid
            this.hasValidConfiguration = false;

            // Throw the raised exception
            throw ex;
        }
    }

    /**
     * Get the contents of missive XML document, sign and encrypt the missive email and send the email via SMTP and
     * synchronize the sent email with Sent Items folder of the email account via IMAP
     * 
     * @param subject Subject of the missive document email
     * @param filename Path and name of missive XML file
     * @return Whether the missive XML email was successfully sent
     * @throws ConfigurationFileNotFoundException
     * @throws InvalidConfigurationException
     * @throws FileNotFoundException
     * @throws IOException
     * @throws KeyStoreException
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws UnrecoverableKeyException
     * @throws NoSuchCertificateException
     * @throws AddressException
     * @throws MessagingException
     * @throws OperatorCreationException
     * @throws SMIMEException
     * @throws CertificateEncodingException
     * @throws InvalidCMSAlgorithmException
     * @throws CMSException
     * @throws javax.mail.NoSuchProviderException
     * @throws SentItemsFolderNotFoundException
     * @throws UnsupportedEncodingException 
     */
    public boolean sendMissive(String subject, String filename) throws ConfigurationFileNotFoundException,
            InvalidConfigurationException, FileNotFoundException, IOException, KeyStoreException,
            NoSuchProviderException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException,
            NoSuchCertificateException, AddressException, MessagingException, OperatorCreationException,
            SMIMEException, CertificateEncodingException, InvalidCMSAlgorithmException, CMSException,
            javax.mail.NoSuchProviderException, SentItemsFolderNotFoundException, UnsupportedEncodingException {

        // Check if we have valid configuration settings
        if (this.validateConfiguration()) {

            // XML missive file content
            String missiveContent = SmocFileUtils.readFile(filename);

            // Sender key store
            SenderKeyStore senderKeyStore = new SenderKeyStore(smocConfig.get("sender.keystore.file"),
                    smocConfig.get("sender.keystore.alias"), smocConfig.get("sender.keystore.provider"),
                    smocConfig.get("sender.keystore.type"), smocConfig.get("sender.keystore.password"));

            // Recipient key store
            RecipientKeyStore recipientKeyStore = new RecipientKeyStore(smocConfig.get("recipient.key.file"));

            // Get message body
            MimeMessage body = this.smtpMailer.createSmtpMessageBody(subject, missiveContent);

            // Missive email content signer instance
            SmocSigner signer = new SmocSigner(senderKeyStore, smocConfig.get("sign.algorithm"));

            // Sign missive email content
            MimeMessage signedMessage = signer.sign(body, this.smtpMailer.getSession());

            // SMIME message encryptor instance
            Smime smime = new Smime(recipientKeyStore, senderKeyStore);

            // Send encrypted and signed missive XML document to recipient
            this.smtpMailer.send(smime.encryptForRecipient(signedMessage, body, this.smtpMailer.getSession(),
                    this.smocConfig.get("smime.cms.algorithm")));

            // Synchronize Sent Items folder of IMAP account
            this.imapSynchronizer.synchronize(smime.encryptForSender(signedMessage, body, this.smtpMailer.getSession(),
                    this.smocConfig.get("smime.cms.algorithm")));

            // Missive document was successfully sent
            return true;
        }

        return false;
    }

    /**
     * Check if the SMOC configuration file was successfully read and parsed
     * 
     * @return Whether the SMOC configuration file was successfully read and parsed
     * @throws ConfigurationFileNotFoundException
     * @throws InvalidConfigurationException
     */
    private boolean validateConfiguration() throws ConfigurationFileNotFoundException, InvalidConfigurationException {

        // Check if configuration file was not found
        if (!this.hasConfiguration) {
            throw new ConfigurationFileNotFoundException();
        }

        // Check if configuration file is not valid
        if (!this.hasValidConfiguration) {
            throw new InvalidConfigurationException();
        }

        // Check if we have the configuration file and that it is valid
        if (this.hasConfiguration && this.hasValidConfiguration) {
            return true;
        }

        return false;
    }

    /**
     * Set the mail cap command map for SMOC
     */
    private void setCommandMap() {

        // Set mail cap command map
        this.mailcap.addMailcap("application/pkcs7-signature;; "
                + "x-java-content-handler=org.bouncycastle.mail.smime.handlers.pkcs7_signature");
        this.mailcap.addMailcap("application/pkcs7-mime;; "
                + "x-java-content-handler=org.bouncycastle.mail.smime.handlers.pkcs7_mime");
        this.mailcap.addMailcap("application/x-pkcs7-signature;; "
                + "x-java-content-handler=org.bouncycastle.mail.smime.handlers.x_pkcs7_signature");
        this.mailcap.addMailcap("application/x-pkcs7-mime;; "
                + "x-java-content-handler=org.bouncycastle.mail.smime.handlers.x_pkcs7_mime");
        this.mailcap.addMailcap("multipart/signed;; "
                + "x-java-content-handler=org.bouncycastle.mail.smime.handlers.multipart_signed");

        // Set the default command map
        CommandMap.setDefaultCommandMap(this.mailcap);
    }
}
