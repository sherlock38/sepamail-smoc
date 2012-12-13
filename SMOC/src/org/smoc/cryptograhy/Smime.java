package org.smoc.cryptograhy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.cert.CertificateEncodingException;
import java.util.Enumeration;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import org.bouncycastle.cms.CMSAlgorithm;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.jcajce.JceCMSContentEncryptorBuilder;
import org.bouncycastle.cms.jcajce.JceKeyTransRecipientInfoGenerator;
import org.bouncycastle.mail.smime.SMIMEEnvelopedGenerator;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.util.Strings;
import org.smoc.exceptions.InvalidCMSAlgorithmException;

/**
 * The Smime class creates the SMIME envelope that will be mailed to the recipient of the missive document and
 * synchronized with the Sent Items folder of the email sender.
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class Smime {

    private SenderKeyStore senderKeyStore;
    private RecipientKeyStore recipientKeyStore;

    /**
     * Smime class constructor
     * 
     * @param recipientKeyStore  Email recipient key store
     * @param senderKeyStore Email sender key store
     */
    public Smime(RecipientKeyStore recipientKeyStore, SenderKeyStore senderKeyStore) {

        // Initialise class attributes
        this.recipientKeyStore = recipientKeyStore;
        this.senderKeyStore = senderKeyStore;
    }

    /**
     * Generate the encrypted message that will be sent to the missive email recipient
     * 
     * @param signedMessage Signed message
     * @param originalMessage Original message
     * @param session SMTP session
     * @param cmsAlgorithm CMS encryption algorithm
     * @return Encrypted message that will be sent to the missive email recipient
     * @throws CertificateEncodingException
     * @throws InvalidCMSAlgorithmException
     * @throws SMIMEException
     * @throws CMSException
     * @throws IOException
     * @throws MessagingException 
     */
    public MimeMessage encryptForRecipient(MimeMessage signedMessage, MimeMessage originalMessage, Session session,
            String cmsAlgorithm) throws CertificateEncodingException, InvalidCMSAlgorithmException, SMIMEException,
            CMSException, IOException, MessagingException {

        // Create the MIME encryptor
        SMIMEEnvelopedGenerator encryptor = new SMIMEEnvelopedGenerator();

        // Add recipient info generator to the encryptor
        encryptor.addRecipientInfoGenerator(
                new JceKeyTransRecipientInfoGenerator(this.recipientKeyStore.getCertificate())
                .setProvider(this.senderKeyStore.getKeyStoreProvider()));

        // Get the content encryptor based on the given CMS algorithm name
        JceCMSContentEncryptorBuilder algorithm = this.getCmsContentEncryptor(cmsAlgorithm);

        // Encrypt the signed message
        MimeBodyPart encryptedPart = encryptor.generate(signedMessage,
                algorithm.setProvider(this.senderKeyStore.getKeyStoreProvider()).build());

        // Add encrypted part to byte array
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        encryptedPart.writeTo(out);

        // Create MIME message for encrypted and signed content
        MimeMessage encryptedMessage = new MimeMessage(session, new ByteArrayInputStream(out.toByteArray()));

        // Get all original headers in the original message
        Enumeration headers = originalMessage.getAllHeaderLines();

        // Set all original MIME headers in the encrypted message
        while (headers.hasMoreElements()) {

            // Current header
            String headerLine = (String) headers.nextElement();

            // Check that we are not overriding any content-* headers from the original message
            if (!Strings.toLowerCase(headerLine).startsWith("content-")) {
                encryptedMessage.addHeaderLine(headerLine);
            }
        }

        // Save encrypted message
        encryptedMessage.saveChanges();

        return encryptedMessage;
    }

    /**
     * Generate the encrypted message that will be stored in the Sent items folder of the email sender
     * 
     * @param signedMessage Signed message
     * @param originalMessage Original message
     * @param session IMAP session
     * @param cmsAlgorithm CMS encryption algorithm
     * @return Encrypted message that will be sent to the missive email recipient
     * @throws CertificateEncodingException
     * @throws InvalidCMSAlgorithmException
     * @throws SMIMEException
     * @throws CMSException
     * @throws IOException
     * @throws MessagingException 
     */
    public MimeMessage encryptForSender(MimeMessage signedMessage, MimeMessage originalMessage, Session session,
            String cmsAlgorithm) throws CertificateEncodingException, InvalidCMSAlgorithmException, SMIMEException,
            CMSException, IOException, MessagingException {

        // Create the MIME encryptor
        SMIMEEnvelopedGenerator encryptor = new SMIMEEnvelopedGenerator();

        // Add recipient info generator to the encryptor
        encryptor.addRecipientInfoGenerator(
                new JceKeyTransRecipientInfoGenerator(this.senderKeyStore.getCertificate())
                .setProvider(this.senderKeyStore.getKeyStoreProvider()));

        // Get the content encryptor based on the given CMS algorithm name
        JceCMSContentEncryptorBuilder algorithm = this.getCmsContentEncryptor(cmsAlgorithm);

        // Encrypt the signed message
        MimeBodyPart encryptedPart = encryptor.generate(signedMessage,
                algorithm.setProvider(this.senderKeyStore.getKeyStoreProvider()).build());

        // Add encrypted part to byte array
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        encryptedPart.writeTo(out);

        // Create MIME message for encrypted and signed content
        MimeMessage encryptedMessage = new MimeMessage(session, new ByteArrayInputStream(out.toByteArray()));

        // Get all original headers in the original message
        Enumeration headers = originalMessage.getAllHeaderLines();

        // Set all original MIME headers in the encrypted message
        while (headers.hasMoreElements()) {

            // Current header
            String headerLine = (String) headers.nextElement();

            // Check that we are not overriding any content-* headers from the original message
            if (!Strings.toLowerCase(headerLine).startsWith("content-")) {
                encryptedMessage.addHeaderLine(headerLine);
            }
        }

        // Save encrypted message
        encryptedMessage.saveChanges();

        return encryptedMessage;
    }

    /**
     * Get an instance of the SMIME content encryptor from a string which defines the name of the encryptor
     * 
     * @param cmsAlgorithm CMS algorithm name
     * @return SMIME content encryptor instance
     * @throws InvalidCMSAlgorithmException 
     */
    private JceCMSContentEncryptorBuilder getCmsContentEncryptor(String cmsAlgorithm) throws 
            InvalidCMSAlgorithmException {

        // Content encryptor
        JceCMSContentEncryptorBuilder jcceb;

        // Initialise encryptor builder based on CMS algorithm string
        switch (cmsAlgorithm) {

            case "AES128_CBC":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES128_CBC);
                break;

            case "AES128_WRAP":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES128_WRAP);
                break;

            case "AES192_CBC":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES192_CBC);
                break;

            case "AES192_WRAP":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES192_WRAP);
                break;

            case "AES256_CBC":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES256_CBC);
                break;

            case "AES256_WRAP":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.AES256_WRAP);
                break;

            case "CAMELLIA128_CBC":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.CAMELLIA128_CBC);
                break;

            case "CAMELLIA128_WRAP":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.CAMELLIA128_WRAP);
                break;

            case "CAMELLIA192_CBC":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.CAMELLIA192_CBC);
                break;

            case "CAMELLIA192_WRAP":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.CAMELLIA192_WRAP);
                break;

            case "CAMELLIA256_CBC":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.CAMELLIA256_CBC);
                break;

            case "CAMELLIA256_WRAP":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.CAMELLIA256_WRAP);
                break;

            case "CAST5_CBC":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.CAST5_CBC);
                break;

            case "DES_EDE3_CBC":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.DES_EDE3_CBC);
                break;

            case "DES_EDE3_WRAP":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.DES_EDE3_WRAP);
                break;

            case "ECDH_SHA1KDF":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.ECDH_SHA1KDF);
                break;

            case "ECMQV_SHA1KDF":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.ECMQV_SHA1KDF);
                break;

            case "GOST3411":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.GOST3411);
                break;

            case "IDEA_CBC":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.IDEA_CBC);
                break;

            case "MD5":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.MD5);
                break;

            case "RC2_CBC":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.RC2_CBC);
                break;

            case "RIPEMD128":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.RIPEMD128);
                break;

            case "RIPEMD160":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.RIPEMD160);
                break;

            case "RIPEMD256":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.RIPEMD256);
                break;

            case "SEED_CBC":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.SEED_CBC);
                break;

            case "SEED_WRAP":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.SEED_WRAP);
                break;

            case "SHA1":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.SHA1);
                break;

            case "SHA224":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.SHA224);
                break;

            case "SHA256":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.SHA256);
                break;

            case "SHA384":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.SHA384);
                break;

            case "SHA512":
                jcceb = new JceCMSContentEncryptorBuilder(CMSAlgorithm.SHA512);
                break;

            default:
                throw new InvalidCMSAlgorithmException(cmsAlgorithm);
        }
        
        return jcceb;
    }
}
