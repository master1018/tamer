public class CheckNullEntity {
    static boolean separateServerThread = true;
    static String pathToStores = "../../../../../../../etc";
    static String keyStoreFile = "keystore";
    static String trustStoreFile = "truststore";
    static String passwd = "passphrase";
    private void initialize() throws Exception {
        String trustFilename =
            System.getProperty("test.src", "./") + "/" + pathToStores +
                "/" + trustStoreFile;
        char[] passphrase = "passphrase".toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(trustFilename), passphrase);
        for (Enumeration e = ks.aliases() ; e.hasMoreElements() ;) {
            String alias = (String)e.nextElement();
            if (ks.isCertificateEntry(alias)) {
                certChain[0] = (X509Certificate)ks.getCertificate(alias);
                break;
            }
        }
        TrustManagerFactory tmf =
            TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);
        trustManager = (X509TrustManager)(tmf.getTrustManagers())[0];
    }
    public static void main(String[] args) throws Exception {
        new CheckNullEntity();
    }
    X509Certificate[] certChain = {null, null};
    X509TrustManager trustManager = null;
    CheckNullEntity() throws Exception {
        String authType = "RSA";
        int failed = 0x3F; 
        int extFailed = 0x3F; 
        initialize();
        try {
            try {
                trustManager.checkClientTrusted(certChain, (String)null);
            } catch (IllegalArgumentException iae) {
                failed >>= 1;
            }
            try {
                trustManager.checkServerTrusted(certChain, (String)null);
            } catch (IllegalArgumentException iae) {
                failed >>= 1;
            }
            try {
                trustManager.checkClientTrusted(certChain, "");
            } catch (IllegalArgumentException iae) {
                failed >>= 1;
            }
            try {
                trustManager.checkServerTrusted(certChain, "");
            } catch (IllegalArgumentException iae) {
                failed >>= 1;
            }
            try {
                trustManager.checkClientTrusted(null, authType);
            } catch (IllegalArgumentException iae) {
                failed >>= 1;
            }
            try {
                trustManager.checkServerTrusted(null, authType);
            } catch (IllegalArgumentException iae) {
                failed >>= 1;
            }
            if (trustManager instanceof X509ExtendedTrustManager) {
                try {
                    ((X509ExtendedTrustManager)trustManager).checkClientTrusted(
                        certChain, (String)null, "localhost", null);
                } catch (IllegalArgumentException iae) {
                    extFailed >>= 1;
                }
                try {
                    ((X509ExtendedTrustManager)trustManager).checkServerTrusted(
                        certChain, (String)null, "localhost", null);
                } catch (IllegalArgumentException iae) {
                    extFailed >>= 1;
                }
                try {
                    ((X509ExtendedTrustManager)trustManager).checkClientTrusted(
                        certChain, "", "localhost", null);
                } catch (IllegalArgumentException iae) {
                    extFailed >>= 1;
                }
                try {
                    ((X509ExtendedTrustManager)trustManager).checkServerTrusted(
                        certChain, "", "localhost", null);
                } catch (IllegalArgumentException iae) {
                    extFailed >>= 1;
                }
                try {
                    ((X509ExtendedTrustManager)trustManager).checkClientTrusted(
                        null, authType, "localhost", null);
                } catch (IllegalArgumentException iae) {
                    extFailed >>= 1;
                }
                try {
                    ((X509ExtendedTrustManager)trustManager).checkServerTrusted(
                        null, authType, "localhost", null);
                } catch (IllegalArgumentException iae) {
                    extFailed >>= 1;
                }
            } else {
                extFailed = 0;
            }
        } catch (NullPointerException npe) {
            failed = 1;
        } catch (Exception e) {
            System.out.println("Got another exception e" + e);
        }
        if (failed != 0 || extFailed != 0) {
            throw new Exception("Should throw IllegalArgumentException");
        }
    }
}
