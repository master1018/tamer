public class TrustManagerTest extends SecmodTest {
    public static void main(String[] args) throws Exception {
        if (initSecmod() == false) {
            return;
        }
        if ("sparc".equals(System.getProperty("os.arch")) == false) {
            System.out.println("Test currently works only on solaris-sparc, skipping");
            return;
        }
        String configName = BASE + SEP + "fips.cfg";
        Provider p = getSunPKCS11(configName);
        System.out.println(p);
        Security.addProvider(p);
        Security.removeProvider("SunJSSE");
        Provider jsse = new com.sun.net.ssl.internal.ssl.Provider(p);
        Security.addProvider(jsse);
        System.out.println(jsse.getInfo());
        KeyStore ks = KeyStore.getInstance("PKCS11", p);
        ks.load(null, "test12".toCharArray());
        X509Certificate server = loadCertificate("certs/server.cer");
        X509Certificate ca = loadCertificate("certs/ca.cer");
        X509Certificate anchor = loadCertificate("certs/anchor.cer");
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(null, null);
        trustStore.setCertificateEntry("anchor", anchor);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
        tmf.init(trustStore);
        X509TrustManager tm = (X509TrustManager)tmf.getTrustManagers()[0];
        X509Certificate[] chain = {server, ca, anchor};
        tm.checkServerTrusted(chain, "RSA");
        System.out.println("OK");
    }
    private static X509Certificate loadCertificate(String name) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream in = new FileInputStream(BASE + SEP + name);
        X509Certificate cert = (X509Certificate)cf.generateCertificate(in);
        in.close();
        return cert;
    }
}
