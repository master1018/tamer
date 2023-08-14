public class ExtKeyUsage {
    public static void main(String[] args) throws Exception {
        File f = new File(System.getProperty("test.src", "."),
            "certextkeyusage");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate)
            cf.generateCertificate(new FileInputStream(f));
        List extKeyUsage = cert.getExtendedKeyUsage();
        if (extKeyUsage.size() != 1)
            throw new Exception("getExtendedKeyUsage()) returned an " +
                "unexpected number of entries: "+extKeyUsage.size());
        try {
            extKeyUsage.clear();
            throw new Exception("List returned by " +
                "getExtendedKeyUsage() is not immutable");
        } catch (UnsupportedOperationException e) {}
        if (!extKeyUsage.contains("1.3.6.1.5.5.7.3.1")) {
            throw new Exception("List returned by "+
                "getExtendedKeyUsage() doesn't contain expected entry");
        }
    }
}
