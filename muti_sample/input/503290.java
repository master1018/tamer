public class MyFailingCertPath extends MyCertPath {
    public MyFailingCertPath(byte[] encoding) {
        super(encoding);
    }
    @Override
    public byte[] getEncoded() throws CertificateEncodingException {
        throw new CertificateEncodingException("testing purpose");
    }
}
