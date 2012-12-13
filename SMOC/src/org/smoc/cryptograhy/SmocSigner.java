package org.smoc.cryptograhy;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.IssuerAndSerialNumber;
import org.bouncycastle.asn1.smime.SMIMECapabilitiesAttribute;
import org.bouncycastle.asn1.smime.SMIMECapability;
import org.bouncycastle.asn1.smime.SMIMECapabilityVector;
import org.bouncycastle.asn1.smime.SMIMEEncryptionKeyPreferenceAttribute;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.mail.smime.SMIMEException;
import org.bouncycastle.mail.smime.SMIMESignedGenerator;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.util.Store;

/**
 * The SmocSigner class signs the MIME message of an email with the X509 private key of the email sender.
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class SmocSigner {

    private SenderKeyStore senderKeyStore;
    private SMIMESignedGenerator signer;

    /**
     * SmocSigner class constructor
     * 
     * @param senderKeyStore Mail sender key store instance
     * @param signAlgorithm Message signature algorithm
     * @throws OperatorCreationException
     * @throws CertificateEncodingException
     */
    public SmocSigner(SenderKeyStore senderKeyStore, String signAlgorithm) throws OperatorCreationException,
            CertificateEncodingException {

        // Initialise class attributes
        this.senderKeyStore = senderKeyStore;

        // SMIME signer capabilities
        SMIMECapabilityVector capabilities = new SMIMECapabilityVector();

        // Add capabilities to the signer
        capabilities.addCapability(SMIMECapability.aES128_CBC);
        capabilities.addCapability(SMIMECapability.aES192_CBC);
        capabilities.addCapability(SMIMECapability.aES256_CBC);
        capabilities.addCapability(SMIMECapability.dES_CBC);
        capabilities.addCapability(SMIMECapability.dES_EDE3_CBC);
        capabilities.addCapability(SMIMECapability.rC2_CBC, 128);

        // Certificate issuer
        X500Name x500name = new X500Name(this.senderKeyStore.getCertificate().getIssuerDN().getName());

        // Certificate issuer and serial number
        IssuerAndSerialNumber issuerAndSerialNumber = new IssuerAndSerialNumber(x500name,
                this.senderKeyStore.getCertificate().getSerialNumber());

        // Encodable vector attributes
        ASN1EncodableVector attributes = new ASN1EncodableVector();

        // Set the SMIME encryption key preference attribute
        attributes.add(new SMIMEEncryptionKeyPreferenceAttribute(issuerAndSerialNumber));

        // Add SMIME capabilities attributes
        attributes.add(new SMIMECapabilitiesAttribute(capabilities));
        
        // Create the SMIME signed content generator
        this.signer = new SMIMESignedGenerator();

        // SmocSigner info generator
        JcaSimpleSignerInfoGeneratorBuilder signerInfoGenerator = new JcaSimpleSignerInfoGeneratorBuilder();

        // Set the attributes of the signer info generator
        signerInfoGenerator.setProvider(this.senderKeyStore.getKeyStoreProvider());
        signerInfoGenerator.setSignedAttributeGenerator(new AttributeTable(attributes));

        // Set the signer info generator
        this.signer.addSignerInfoGenerator(signerInfoGenerator.build(signAlgorithm,
                this.senderKeyStore.getPrivateKey(), this.senderKeyStore.getCertificate()));

        // Add list of certificates to the generator
        List certificatesList = new ArrayList();
        certificatesList.add(this.senderKeyStore.getCertificate());
        Store certificateStore = new JcaCertStore(certificatesList);
        this.signer.addCertificates(certificateStore);
    }

    /**
     * Sign the MIME message content of an email for the given SMTP session
     * 
     * @param body MIME message content of an email that needs to be signed
     * @param session SMTP session
     * @return Signed the MIME message content of an email
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws SMIMEException
     * @throws MessagingException 
     */
    public MimeMessage sign(MimeMessage body, Session session) throws NoSuchAlgorithmException, NoSuchProviderException,
            SMIMEException, MessagingException {

        // Sign the given MIME message
        MimeMultipart mm = this.signer.generate(body, this.senderKeyStore.getKeyStoreProvider());
        MimeMessage signedMessage = new MimeMessage(session);

        // Get all original MIME headers
        Enumeration headers = body.getAllHeaderLines();
        
        // Set all original MIME headers in the signed message
        while (headers.hasMoreElements()) {
            signedMessage.addHeaderLine((String) headers.nextElement());
        }

        // Set the content of the signed message
        signedMessage.setContent(mm);

        // Save changes made to the signed message body
        signedMessage.saveChanges();

        return signedMessage;
    }
}
