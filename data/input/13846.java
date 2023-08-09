public class AddTrustedCert extends SecmodTest {
    public static void main(String[] args) throws Exception {
        if (initSecmod() == false) {
            return;
        }
        InputStream in = new FileInputStream(BASE + SEP + "anchor.cer");
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate)factory.generateCertificate(in);
        in.close();
        String configName = BASE + SEP + "nss.cfg";
        Provider p = getSunPKCS11(configName);
        System.out.println(p);
        Security.addProvider(p);
        KeyStore ks = KeyStore.getInstance("PKCS11", p);
        ks.load(null, password);
        Collection<String> aliases = new TreeSet<String>(Collections.list(ks.aliases()));
        System.out.println("entries: " + aliases.size());
        System.out.println(aliases);
        int size1 = aliases.size();
        String alias = "anchor";
        ks.setCertificateEntry(alias, cert);
        ks.setCertificateEntry(alias, cert);
        aliases = new TreeSet<String>(Collections.list(ks.aliases()));
        System.out.println("entries: " + aliases.size());
        System.out.println(aliases);
        int size2 = aliases.size();
        if ((size2 != size1 + 1) || (aliases.contains(alias) == false)) {
            throw new Exception("Trusted cert not added");
        }
        X509Certificate cert2 = (X509Certificate)ks.getCertificate(alias);
        if (cert.equals(cert2) == false) {
            throw new Exception("KeyStore returned incorrect certificate");
        }
        System.out.println("OK");
    }
}
