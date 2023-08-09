public class MyCertStoreSpi extends CertStoreSpi {
    public MyCertStoreSpi(CertStoreParameters params)
            throws InvalidAlgorithmParameterException {
        super(params);        
        if (!(params instanceof MyCertStoreParameters)) {
            throw new InvalidAlgorithmParameterException("Invalid params");
        }
    }
    public Collection<Certificate> engineGetCertificates(CertSelector selector)
            throws CertStoreException {
        if (selector == null) {
            throw new CertStoreException("Parameter is null");
        }
        return null;
    }
    public Collection<CRL> engineGetCRLs(CRLSelector selector)
            throws CertStoreException {
        if (selector == null) {
            throw new CertStoreException("Parameter is null");
        }
        return null;
    }
}
