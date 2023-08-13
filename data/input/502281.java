final class DefaultSSLContext {
    private static SSLContext defaultSSLContext;
     static synchronized SSLContext getContext() {
        if (defaultSSLContext == null) {
            defaultSSLContext = AccessController
                    .doPrivileged(new PrivilegedAction<SSLContext>() {
                        public SSLContext run() {
                            return findDefault();
                        }
                    });
        }
        return defaultSSLContext;
    }
    private static SSLContext findDefault() {
        for (Provider provider : Services.getProvidersList()) {
            final Provider.Service service = Engine.door.getService(provider, "SSLContext");
            if (service != null) {
                try {
                    SSLContext con = new SSLContext((SSLContextSpi) service.newInstance(null),
                            service.getProvider(), service.getAlgorithm());
                    KeyManager[] keyManagers = null;
                    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
                    String keystore = System.getProperty("javax.net.ssl.keyStore");
                    String keystorepwd = System.getProperty("javax.net.ssl.keyStorePassword");
                    char[] pwd = null;
                    if (keystorepwd != null) {
                        pwd = keystorepwd.toCharArray();
                    }
                    if (keystore != null) {
                        FileInputStream fis = new FileInputStream(keystore);
                        try {
                            ks.load(fis, pwd);
                        } finally {
                            fis.close();
                        }
                        KeyManagerFactory kmf;
                        String kmfAlg = Security.getProperty("ssl.KeyManagerFactory.algorithm");
                        if (kmfAlg == null) {
                            kmfAlg = "SunX509";
                        }
                        kmf = KeyManagerFactory.getInstance(kmfAlg);
                        kmf.init(ks, pwd);
                        keyManagers = kmf.getKeyManagers();
                    }
                    TrustManager[] trustManagers = null;
                    keystore = System.getProperty("javax.net.ssl.trustStore");
                    keystorepwd = System.getProperty("javax.net.ssl.trustStorePassword");
                    pwd = null;
                    if (keystorepwd != null) {
                        pwd = keystorepwd.toCharArray();
                    }
                    if (keystore != null) {
                        FileInputStream fis = new FileInputStream(keystore);
                        try {
                            ks.load(fis, pwd);
                        } finally {
                            fis.close();
                        }
                        TrustManagerFactory tmf;
                        String tmfAlg = Security.getProperty("ssl.TrustManagerFactory.algorithm");
                        if (tmfAlg == null) {
                            tmfAlg = "PKIX";
                        }
                        tmf = TrustManagerFactory.getInstance(tmfAlg);
                        tmf.init(ks);
                        trustManagers = tmf.getTrustManagers();
                    }
                    con.init(keyManagers, trustManagers, null);
                    return con;
                } catch (Exception e) {
                }
            }
        }
        return null;
    }
}
