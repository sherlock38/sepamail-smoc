package org.smoc.cryptograhy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import org.bouncycastle.jce.provider.X509CertificateObject;
import org.bouncycastle.openssl.PEMReader;
import org.smoc.exceptions.PublicKeyFileNotFoundException;

/**
 * The RecipientKeyStore class reads a given public key file and gets an instance of the associated public key of the
 * email recipient.
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class RecipientKeyStore {

    private PublicKey publicKey;
    private X509CertificateObject certificateObject;

    /**
     * Get the public key of the email recipient
     * 
     * @return Public key of the email recipient
     */
    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    /**
     * Get the public key of the recipient as an X509 certificate
     * 
     * @return Public key of the recipient as an X509 certificate
     */
    public X509Certificate getCertificate() {
        return this.certificateObject;
    }

    /**
     * RecipientKeyStore class constructor
     * 
     * @param keyFilename Public key path and filename
     * @throws PublicKeyFileNotFoundException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public RecipientKeyStore(String keyFilename) throws PublicKeyFileNotFoundException, FileNotFoundException,
            IOException {

        // Initialise class attributes
        this.publicKey = null;

        // Public key file
        File publicKeyFile = new File(keyFilename);

        // Check if public key file exists
        if (publicKeyFile.exists()) {

            // Public key file reader
            FileReader fr = new FileReader(publicKeyFile);

            // PEMReader instance to read content of public key file
            PEMReader pr = new PEMReader(fr);

            // Get X509 certificate
            this.certificateObject = (X509CertificateObject) pr.readObject();

            // Public key
            this.publicKey = this.certificateObject.getPublicKey();

        } else {

            // We throw an exception since the public key file could not be found
            throw new PublicKeyFileNotFoundException(keyFilename);
        }
    }
}
