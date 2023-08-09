public abstract class X509CRL extends CRL implements X509Extension {
    protected X509CRL() {
        super("X.509"); 
    }
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof X509CRL)) {
            return false;
        }
        X509CRL obj = (X509CRL) other;
        try {
            return Arrays.equals(getEncoded(), obj.getEncoded());
        } catch (CRLException e) {
            return false;
        }
    }
    public int hashCode() {
        try {
            int res = 0;
            byte[] array = getEncoded();
            for (int i=0; i<array.length; i++) {
                res += array[i] & 0xFF;
            }
            return res;
        } catch (CRLException e) {
            return 0;
        }
    }
    public abstract byte[] getEncoded() throws CRLException;
    public abstract void verify(PublicKey key)
                     throws CRLException, NoSuchAlgorithmException,
                            InvalidKeyException, NoSuchProviderException,
                            SignatureException;
    public abstract void verify(PublicKey key, String sigProvider)
                     throws CRLException, NoSuchAlgorithmException,
                            InvalidKeyException, NoSuchProviderException,
                            SignatureException;
    public abstract int getVersion();
    public abstract Principal getIssuerDN();
    public X500Principal getIssuerX500Principal() {
        try {
            CertificateFactory factory = CertificateFactory
                    .getInstance("X.509"); 
            X509CRL crl = (X509CRL) factory
                    .generateCRL(new ByteArrayInputStream(getEncoded()));
            return crl.getIssuerX500Principal();
        } catch (Exception e) {
            throw new RuntimeException(Messages.getString("security.59"), e); 
        }
    }
    public abstract Date getThisUpdate();
    public abstract Date getNextUpdate();
    public abstract X509CRLEntry getRevokedCertificate(BigInteger serialNumber);
    public X509CRLEntry getRevokedCertificate(X509Certificate certificate) {
        if (certificate == null) {
            throw new NullPointerException();
        }
        return getRevokedCertificate(certificate.getSerialNumber());
    }
    public abstract Set<? extends X509CRLEntry> getRevokedCertificates();
    public abstract byte[] getTBSCertList() throws CRLException;
    public abstract byte[] getSignature();
    public abstract String getSigAlgName();
    public abstract String getSigAlgOID();
    public abstract byte[] getSigAlgParams();
}
