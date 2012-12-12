package org.smoc.cryptograhy;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.smoc.exceptions.NoSuchCertificateException;

/**
 * The SenderKeyStore class reads a given key store file secured with a password and gets an instance of the associated
 * private and public key of the email sender.
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class SenderKeyStore {

    private X509Certificate certificate;
    private String keyStoreProvider;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    /**
     * Get the name of the key store provider
     * 
     * @return Name of the key store provider
     */
    public String getKeyStoreProvider() {
        return this.keyStoreProvider;
    }

    /**
     * Get the private key of the mail sender
     * 
     * @return Private key of the mail sender
     */
    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    /**
     * Get the public key of the mail sender
     * 
     * @return Public key of the mail sender
     */
    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    /**
     * Get the certificate
     * @return 
     */
    public X509Certificate getCertificate() {
        return this.certificate;
    }

    /**
     * SenderKeyStore constructor
     * 
     * @param keyStoreFilename Key store path and filename
     * @param keyAlias Key alias
     * @param keyStoreProvider Key store provider name
     * @param keyStoreType Key store provider type
     * @param passphrase Key store pass phrase
     * @throws KeyStoreException
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws UnrecoverableKeyException
     * @throws NoSuchCertificateException
     * @throws IOException 
     */
    public SenderKeyStore (String keyStoreFilename, String keyAlias, String keyStoreProvider, String keyStoreType,
            String passphrase) throws KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException,
            CertificateException, UnrecoverableKeyException, NoSuchCertificateException, IOException {

        // Initialise class attributes
        this.certificate = null;
        this.keyStoreProvider = keyStoreProvider;
        this.privateKey = null;
        this.publicKey = null;

        // Get instance of key store
        KeyStore keystore = KeyStore.getInstance(keyStoreType, keyStoreProvider);

        // Try to load the key store using the given pass phrase
        try (InputStream in = new FileInputStream(keyStoreFilename)) {

            // Load key store
            keystore.load(in, passphrase.toCharArray());

            // Check if the required key alias is found in the key store
            if (keystore.isKeyEntry(keyAlias)) {

                // Certificate
                this.certificate = (X509Certificate)keystore.getCertificate(keyAlias);

                // Get the required public key
                this.publicKey = this.certificate.getPublicKey();

                // Get the required private key
                this.privateKey = (PrivateKey) keystore.getKey(keyAlias, passphrase.toCharArray());

            } else {

                // Throw exception since certificate by the given alias was not found
                throw new NoSuchCertificateException(keyAlias, keyStoreFilename);
            }

        } catch (IOException ex) {

            // Throw raised IOException
            throw ex;
        }
    }
}
