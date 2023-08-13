public class ClientJSSEServerJSSE extends SecmodTest {
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
        CipherTest.main(new JSSEFactory(), ks, args);
    }
    private static class JSSEFactory extends CipherTest.PeerFactory {
        String getName() {
            return "Client JSSE - Server JSSE";
        }
        CipherTest.Client newClient(CipherTest cipherTest) throws Exception {
            return new JSSEClient(cipherTest);
        }
        CipherTest.Server newServer(CipherTest cipherTest) throws Exception {
            return new JSSEServer(cipherTest);
        }
    }
}
