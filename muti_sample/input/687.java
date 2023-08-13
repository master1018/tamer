public class DetectInvalidEncoding {
    public static void main(String[] args) throws Exception {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        File f = new File
            (System.getProperty("test.src", "."), "invalidcert.pem");
        InputStream inStream = new FileInputStream(f);
        try {
            X509Certificate cert =
                (X509Certificate) cf.generateCertificate(inStream);
        } catch (CertificateParsingException ce) {
            return;
        }
        throw new Exception("CertificateFactory.generateCertificate() did not "
            + "throw CertificateParsingException on invalid X.509 cert data");
    }
}
