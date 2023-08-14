public abstract class CertStoreSpi {
    public CertStoreSpi(CertStoreParameters params)
            throws InvalidAlgorithmParameterException {
    }
    public abstract Collection<? extends Certificate> engineGetCertificates(CertSelector selector)
            throws CertStoreException;
    public abstract Collection<? extends CRL> engineGetCRLs(CRLSelector selector)
            throws CertStoreException;
}
