public abstract class CertificateFactorySpi {
    public abstract Certificate engineGenerateCertificate(InputStream inStream)
        throws CertificateException;
    public CertPath engineGenerateCertPath(InputStream inStream)
        throws CertificateException
    {
        throw new UnsupportedOperationException();
    }
    public CertPath engineGenerateCertPath(InputStream inStream,
        String encoding) throws CertificateException
    {
        throw new UnsupportedOperationException();
    }
    public CertPath
        engineGenerateCertPath(List<? extends Certificate> certificates)
        throws CertificateException
    {
        throw new UnsupportedOperationException();
    }
    public Iterator<String> engineGetCertPathEncodings() {
        throw new UnsupportedOperationException();
    }
    public abstract Collection<? extends Certificate>
            engineGenerateCertificates(InputStream inStream)
            throws CertificateException;
    public abstract CRL engineGenerateCRL(InputStream inStream)
        throws CRLException;
    public abstract Collection<? extends CRL> engineGenerateCRLs
            (InputStream inStream) throws CRLException;
}
