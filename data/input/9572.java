public class MyX509CertImpl extends X509Certificate
        implements X509Extension {
    X509Certificate c;
    protected MyX509CertImpl(X509Certificate cert) {
        c = cert;
    }
    public void checkValidity() throws CertificateExpiredException,
            CertificateNotYetValidException {
        c.checkValidity();
    }
    public void checkValidity(Date date) throws CertificateExpiredException,
            CertificateNotYetValidException {
        c.checkValidity(date);
    }
    public int getVersion() {
        return c.getVersion();
    }
    public BigInteger getSerialNumber() {
        return c.getSerialNumber();
    }
    public Principal getIssuerDN() {
        return c.getIssuerDN();
    }
    public X500Principal getIssuerX500Principal() {
        return c.getIssuerX500Principal();
    }
    public Principal getSubjectDN() {
        return c.getSubjectDN();
    }
    public X500Principal getSubjectX500Principal() {
        return c.getSubjectX500Principal();
    }
    public Date getNotBefore() {
        return c.getNotBefore();
    }
    public Date getNotAfter() {
        return c.getNotAfter();
    }
    public byte[] getTBSCertificate()
        throws CertificateEncodingException {
        return c.getTBSCertificate();
    }
    public byte[] getSignature() {
        return c.getSignature();
    }
    public String getSigAlgName() {
        return c.getSigAlgName();
    }
    public String getSigAlgOID() {
        return c.getSigAlgOID();
    }
    public byte[] getSigAlgParams() {
        return c.getSigAlgParams();
    }
    public boolean[] getIssuerUniqueID() {
        return c.getIssuerUniqueID();
    }
    public boolean[] getSubjectUniqueID() {
        return c.getSubjectUniqueID();
    }
    public boolean[] getKeyUsage() {
        return c.getKeyUsage();
    }
    public List<String> getExtendedKeyUsage()
            throws CertificateParsingException {
        return c.getExtendedKeyUsage();
    }
    public int getBasicConstraints() {
        return c.getBasicConstraints();
    }
    public Collection<List<?>> getSubjectAlternativeNames()
        throws CertificateParsingException {
        return c.getSubjectAlternativeNames();
    }
    public Collection<List<?>> getIssuerAlternativeNames()
        throws CertificateParsingException {
        return c.getIssuerAlternativeNames();
    }
    public boolean hasUnsupportedCriticalExtension() {
        return c.hasUnsupportedCriticalExtension();
    }
    public Set<String> getCriticalExtensionOIDs() {
        return c.getCriticalExtensionOIDs();
    }
    public Set<String> getNonCriticalExtensionOIDs() {
        return c.getNonCriticalExtensionOIDs();
    }
    public byte[] getExtensionValue(String oid) {
        return c.getExtensionValue(oid);
    }
    public boolean equals(Object other) {
        return c.equals(other);
    }
    public int hashCode() {
        return c.hashCode();
    }
    public byte[] getEncoded()
            throws CertificateEncodingException {
        return c.getEncoded();
    }
    public void verify(PublicKey key)
            throws CertificateException, NoSuchAlgorithmException,
            InvalidKeyException, NoSuchProviderException,
            SignatureException {
        System.out.println("Trying a verify");
        try {
            c.verify(key);
        } catch (SignatureException e) {
            System.out.println("Rethrowing \"acceptable\" exception");
            throw new InvalidKeyException(
                "Rethrowing as a SignatureException", e);
        }
    }
    public void verify(PublicKey key, String sigProvider)
            throws CertificateException, NoSuchAlgorithmException,
            InvalidKeyException, NoSuchProviderException,
            SignatureException {
        System.out.println("Trying a verify");
        try {
            c.verify(key, sigProvider);
        } catch (SignatureException e) {
            System.out.println("Rethrowing \"acceptable\" exception");
            throw new InvalidKeyException(
                "Rethrowing as a SignatureException", e);
        }
    }
    public String toString() {
        return c.toString();
    }
    public PublicKey getPublicKey() {
        return c.getPublicKey();
    }
}
