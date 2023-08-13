public class BadX509CertData {
    private static final String data = "\211\0\225\3\5\0\70\154\157\231";
    public static void main(String[] args) throws Exception {
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        InputStream is = new ByteArrayInputStream(data.getBytes("ISO8859_1"));
        try {
            Certificate cert = factory.generateCertificate(is);
        } catch (CertificateException ce) {
            return;
        }
        throw new Exception("CertificateFactory.generateCertificate() did "
            + "not throw CertificateParsingException on bad X.509 cert data");
    }
}
