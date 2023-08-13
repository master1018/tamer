public class CheckCertId {
    private static final String CERT_FILENAME = "interCA.der";
    public static void main(String[] args) throws Exception {
        X509CertImpl cert = loadCert(CERT_FILENAME);
        MessageDigest hash = MessageDigest.getInstance("SHA1");
        hash.update(cert.getSubjectX500Principal().getEncoded());
        byte[] expectedHash = hash.digest();
        CertId certId = new CertId(cert, null);
        byte[] receivedHash = certId.getIssuerNameHash();
        if (! Arrays.equals(expectedHash, receivedHash)) {
            throw new
                Exception("Bad hash value for issuer name in CertId object");
        }
    }
    private static X509CertImpl loadCert(String filename) throws Exception {
        BufferedInputStream bis =
            new BufferedInputStream(
                new FileInputStream(
                    new File(System.getProperty("test.src", "."), filename)));
        return new X509CertImpl(bis);
    }
}
