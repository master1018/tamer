public abstract class CertificateFactorySpi {
    public CertificateFactorySpi() {
    }
    public abstract Certificate engineGenerateCertificate(InputStream inStream)
            throws CertificateException;
    public abstract Collection<? extends Certificate> 
        engineGenerateCertificates(InputStream inStream) throws CertificateException;
    public abstract CRL engineGenerateCRL(InputStream inStream)
            throws CRLException;
    public abstract Collection<? extends CRL> 
        engineGenerateCRLs(InputStream inStream) throws CRLException;
    public CertPath engineGenerateCertPath(InputStream inStream)
            throws CertificateException {
        throw new UnsupportedOperationException(
                Messages.getString("security.70")); 
    }
    public CertPath engineGenerateCertPath(InputStream inStream, String encoding)
            throws CertificateException {
        throw new UnsupportedOperationException(
                Messages.getString("security.71")); 
    }
    public CertPath engineGenerateCertPath(List<? extends Certificate>  certificates) 
            throws CertificateException {
        throw new UnsupportedOperationException(
                Messages.getString("security.72")); 
    }
    public Iterator<String> engineGetCertPathEncodings() {
        throw new UnsupportedOperationException(
                Messages.getString("security.73")); 
    }
}
