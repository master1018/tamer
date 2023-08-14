public class MyFailingCertificate extends MyCertificate {
    public MyFailingCertificate(String type, byte[] encoding) {
        super(type, encoding);
    }
    @Override
    public byte[] getEncoded() throws CertificateEncodingException {
        throw new CertificateEncodingException("testing purpose");
    }
}
