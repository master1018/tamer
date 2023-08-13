public class KMTMGetNothing {
    KMTMGetNothing() throws Exception {
        char[] passphrase = "none".toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, passphrase);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);
        X509KeyManager km = (X509KeyManager) kmf.getKeyManagers()[0];
        if (km.getCertificateChain(null) != null) {
            throw new Exception("km.getCertificateChain(null) != null");
        }
        if (km.getCertificateChain("fubar") != null) {
            throw new Exception("km.getCertificateChain(\"fubar\") != null");
        }
        if (km.getPrivateKey(null) != null) {
            throw new Exception("km.getPrivateKey(null) != null");
        }
        if (km.getPrivateKey("fubar") != null) {
            throw new Exception("km.getPrivateKey(\"fubar\") != null");
        }
        System.out.println("KM TESTS PASSED");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);
        X509TrustManager tm = (X509TrustManager) tmf.getTrustManagers()[0];
        if ((tm.getAcceptedIssuers() == null) ||
                (tm.getAcceptedIssuers().length != 0)) {
            throw new Exception("tm.getAcceptedIssuers() != null");
        }
        System.out.println("TM TESTS PASSED");
        System.out.println("ALL TESTS PASSED");
    }
    public static void main(String[] args) throws Exception {
        new KMTMGetNothing();
    }
}
