package com.aternity.tupleBuilder.utils;

import com.aternity.agentSimulator.simulator.Configuration;
import com.aternity.agentSimulator.simulator.util.StringUtils;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by seagull on 26/04/2015.
 */
public class SslHelper {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SslHelper.class);

    public SslHelper() {
    }

    public static void trustSelfSignedSSL() {
        try {
            final SSLContext ctx = SSLContext.getInstance("TLSv1.2");
            final X509TrustManager tm = new X509TrustManager() {

                public void checkClientTrusted(final X509Certificate[] xcs, final String string) throws CertificateException {
                    // do nothing
                }

                public void checkServerTrusted(final X509Certificate[] xcs, final String string) throws CertificateException {
                    // do nothing
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

//            load client keystore
            KeyManagerFactory kmFactory = KeyManagerFactory.getInstance("SunX509");

            // If key store parameters were supplied in connection file, they will be used
            // If not, we check if they was supplied as VM parameters
            String keyStorePath = (!StringUtils.isEmpty(Configuration.sslKeyStore) ? Configuration.sslKeyStore : System.getProperty("javax.net.ssl.keyStore"));
            String keyStorePass = (!StringUtils.isEmpty(Configuration.sslKeyStorePwd) ? Configuration.sslKeyStorePwd : System.getProperty("javax.net.ssl.keyStorePassword"));

            if(StringUtils.isEmpty(keyStorePass) || StringUtils.isEmpty(keyStorePath)){
                //logger.warn("keyStore parameters were not supplied, client keyStore will not be loaded");
                ctx.init(null, new TrustManager[]{tm}, null);
            } else {

                logger.info("Loading client keyStore: keyStorePath={}, keyStorePass={}", keyStorePath, keyStorePass);
                final KeyStore keyStore = loadKeyStore(keyStorePath, keyStorePass, "JKS");
                if(keyStore != null) {
                    kmFactory.init(keyStore, keyStorePass.toCharArray());
                    ctx.init(kmFactory.getKeyManagers(), new TrustManager[]{tm}, null);
                } else {
                    ctx.init(null, new TrustManager[]{tm}, null);
                }
            }

            SSLContext.setDefault(ctx);
            HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());

            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static KeyStore loadKeyStore(String keyStorePath, String keyStorePass, String keyStoreType) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {

        // check if key store path exists
        if(!(new File(keyStorePath)).exists()){
            logger.error("loadKeyStore: key store cannot be loaded since key store path does not exist [{}]", keyStorePath);
            return null;
        }


        KeyStore keyStore = KeyStore.getInstance(keyStoreType);
        FileInputStream stream = new FileInputStream(keyStorePath);
        keyStore.load(stream, keyStorePass.toCharArray());
        stream.close();
        return keyStore;
    }


    // EasySSLProtocolSocketFactory can be used to create SSL connections that allow the target server to authenticate with a self-signed certificate.
    // http://svn.apache.org/viewvc/httpcomponents/oac.hc3x/trunk/src/contrib/org/apache/commons/httpclient/contrib/ssl/EasySSLProtocolSocketFactory.java?view=markup


}
