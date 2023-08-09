public class MyCertificateFactory extends CertificateFactorySpi {
    CertificateFactory cf;
    public MyCertificateFactory() {
        try {
            cf = CertificateFactory.getInstance("X.509", "SUN");
        } catch (Exception e) {
            throw new RuntimeException(
                "Couldn't create the Sun X.509 CertificateFactory");
        }
    }
    public Certificate engineGenerateCertificate(InputStream inStream)
        throws CertificateException {
        Certificate cert = cf.generateCertificate(inStream);
        if (!(cert instanceof X509Certificate)) {
            throw new RuntimeException("Not an X509Certificate");
        }
        return new MyX509CertImpl((X509Certificate)cert);
    }
    public CertPath engineGenerateCertPath(InputStream inStream)
        throws CertificateException {
        return cf.generateCertPath(inStream);
    }
    public CertPath engineGenerateCertPath(InputStream inStream,
        String encoding)
        throws CertificateException {
        return cf.generateCertPath(inStream, encoding);
    }
    public CertPath
        engineGenerateCertPath(List<? extends Certificate> certificates)
        throws CertificateException {
        return cf.generateCertPath(certificates);
    }
    public Iterator<String> engineGetCertPathEncodings() {
        return cf.getCertPathEncodings();
    }
    public Collection<? extends Certificate>
            engineGenerateCertificates(InputStream inStream)
            throws CertificateException {
        return cf.generateCertificates(inStream);
    }
    public CRL engineGenerateCRL(InputStream inStream)
        throws CRLException {
        return cf.generateCRL(inStream);
    }
    public Collection<? extends CRL> engineGenerateCRLs
            (InputStream inStream) throws CRLException {
        return cf.generateCRLs(inStream);
    }
}
