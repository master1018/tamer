public class TrustAnchors extends SecmodTest {
    public static void main(String[] args) throws Exception {
        if (initSecmod() == false) {
            return;
        }
        if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            System.out.println("Test currently does not work on Windows, skipping");
            return;
        }
        String configName = BASE + SEP + "nsstrust.cfg";
        Provider p = getSunPKCS11(configName);
        System.out.println(p);
        Security.addProvider(p);
        KeyStore ks = KeyStore.getInstance("PKCS11", p);
        ks.load(null, null);
        Collection<String> aliases = new TreeSet<String>(Collections.list(ks.aliases()));
        System.out.println("entries: " + aliases.size());
        System.out.println(aliases);
        for (String alias : aliases) {
            if (ks.isCertificateEntry(alias) == false) {
                throw new Exception("not trusted: " + alias);
            }
            X509Certificate cert = (X509Certificate)ks.getCertificate(alias);
            if (cert.getSubjectX500Principal().equals(cert.getIssuerX500Principal())) {
            System.out.print(".");
                cert.verify(cert.getPublicKey());
            } else {
                System.out.print("-");
            }
        }
        System.out.println();
        System.out.println("OK");
    }
}
