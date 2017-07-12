package com.rosterloh.andriot.cloud;

import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyInfo;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.security.auth.x500.X500Principal;

import timber.log.Timber;

/**
 * This class wraps the storage and access of the authentication key used for Cloud IoT.  One of
 * the driving reasons for this is to leverage the secure key storage on Android Things.
 *
 * This class currently only support RS256 Authentication.  TODO:  Add EC256 support.
 */
public class MqttAuthentication {

    private static final String DEFAULT_KEYSTORE = "AndroidKeyStore";
    private static final String DEFAULT_ALIAS = "Cloud IoT Authentication";
    private final String mKeystoreName;
    private final String mKeyAlias;

    private Certificate mCertificate;
    private PrivateKey mPrivateKey;

    /**
     * Create a new Cloud IoT Authentication wrapper using the default keystore and alias.
     */
    public MqttAuthentication() {
        this(DEFAULT_KEYSTORE, DEFAULT_ALIAS);
    }

    /**
     * Create a new Cloud IoT Authentication wrapper using the specified keystore and alias (instead
     * of the defaults)
     *
     * @param keystoreName The keystore to load
     * @param keyAlias the alias in the keystore for Cloud IoT Authentication
     */
    public MqttAuthentication(String keystoreName, String keyAlias) {
        mKeystoreName = keystoreName;
        mKeyAlias = keyAlias;
    }

    public void initialize() {
        try {
            KeyStore ks = KeyStore.getInstance(mKeystoreName);
            ks.load(null);

            mCertificate = ks.getCertificate(mKeyAlias);
            if (mCertificate == null) {
                // generate key
                Timber.w("No IoT Auth Certificate found, generating new cert");
                generateAuthenticationKey();
                mCertificate = ks.getCertificate(mKeyAlias);
            }

            Timber.i("loaded certificate: " + mKeyAlias);

            if (mCertificate instanceof X509Certificate) {
                X509Certificate x509Certificate = (X509Certificate) mCertificate;
                Timber.d("Subject: " + x509Certificate.getSubjectX500Principal().toString());
                Timber.d("Issuer: " + x509Certificate.getIssuerX500Principal().toString());
                Timber.d("Signature: " + x509Certificate.getSignature().toString());
            }

            Key key = ks.getKey(mKeyAlias, null);
            mPrivateKey = (PrivateKey) key;
            boolean keyIsInSecureHardware = false;
            try {
                KeyFactory factory = KeyFactory.getInstance(mPrivateKey.getAlgorithm(), mKeystoreName);
                KeyInfo keyInfo = factory.getKeySpec(mPrivateKey, KeyInfo.class);
                keyIsInSecureHardware = keyInfo.isInsideSecureHardware();
                Timber.d("able to confirm if key is secured or not");
            } catch (GeneralSecurityException e) {
                // ignored
            }
            Timber.i("Key is in secure hardware? " + keyIsInSecureHardware);


        } catch (GeneralSecurityException | IOException e) {
            Timber.e("Failed to open keystore", e);
        }

    }

    /**
     * Generate a new RSA key pair entry in the Android Keystore by
     * using the KeyPairGenerator API. This creates both a KeyPair
     * and a self-signed certificate, both with the same alias
     */
    private void generateAuthenticationKey() throws GeneralSecurityException {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA, mKeystoreName);
        kpg.initialize(new KeyGenParameterSpec.Builder(
                mKeyAlias,
                KeyProperties.PURPOSE_SIGN)
                .setKeySize(2048)
                .setCertificateSubject(new X500Principal("CN=unused"))
                .setDigests(KeyProperties.DIGEST_SHA256)
                .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                .build());

        kpg.generateKeyPair();
    }

    /**
     * Exports the authentication certificate to a file.
     *
     * @param destination the file to write the certificate to (PEM encoded)
     */
    public void exportPublicKey(File destination) throws IOException, GeneralSecurityException {
        FileOutputStream os = new FileOutputStream(destination);
        os.write(getCertificatePEM().getBytes());
        os.flush();
        os.close();
    }

    public Certificate getCertificate() {
        return mCertificate;
    }

    /**
     * Returns the PEM-format encoded certificate
     */
    public String getCertificatePEM() throws GeneralSecurityException {
        StringBuilder sb = new StringBuilder();
        sb.append("-----BEGIN CERTIFICATE-----\n");
        sb.append(Base64.encodeToString(mCertificate.getEncoded(), Base64.DEFAULT));
        sb.append("-----END CERTIFICATE-----\n");
        return sb.toString();
    }

    public PrivateKey getPrivateKey() {
        return mPrivateKey;
    }
/*
    public char[] createJwt(String projectId)
            throws IOException, InvalidKeySpecException, NoSuchAlgorithmException {
        LocalDateTime now = LocalDateTime.now();

        // Create a JWT to authenticate this device. The device will be disconnected after the token
        // expires, and will have to reconnect with a new token. The audience field should always
        // be set to the GCP project id.
        JwtBuilder jwtBuilder =
                Jwts.builder()
                        .setIssuedAt(Date.from(now.toInstant(ZoneOffset.UTC)))
                        .setExpiration(now.plusMinutes(60).toDate())
                        .setAudience(projectId);

        return jwtBuilder.signWith(SignatureAlgorithm.RS256, privateKey).compact().toCharArray();
    }*/
}
