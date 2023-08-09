public class X509V1CertImpl extends X509Certificate implements Serializable {
    static final long serialVersionUID = -2048442350420423405L;
    private java.security.cert.X509Certificate wrappedCert;
    synchronized private static java.security.cert.CertificateFactory
    getFactory()
    throws java.security.cert.CertificateException
    {
        return java.security.cert.CertificateFactory.getInstance("X.509");
    }
    public X509V1CertImpl() { }
    public X509V1CertImpl(byte[] certData)
    throws CertificateException {
        try {
            ByteArrayInputStream bs;
            bs = new ByteArrayInputStream(certData);
            wrappedCert = (java.security.cert.X509Certificate)
                getFactory().generateCertificate(bs);
        } catch (java.security.cert.CertificateException e) {
            throw new CertificateException(e.getMessage());
        }
    }
    public X509V1CertImpl(InputStream in)
    throws CertificateException {
        try {
            wrappedCert = (java.security.cert.X509Certificate)
                getFactory().generateCertificate(in);
        } catch (java.security.cert.CertificateException e) {
            throw new CertificateException(e.getMessage());
        }
    }
    public byte[] getEncoded() throws CertificateEncodingException {
        try {
            return wrappedCert.getEncoded();
        } catch (java.security.cert.CertificateEncodingException e) {
            throw new CertificateEncodingException(e.getMessage());
        }
    }
    public void verify(PublicKey key)
        throws CertificateException, NoSuchAlgorithmException,
        InvalidKeyException, NoSuchProviderException,
        SignatureException
    {
        try {
            wrappedCert.verify(key);
        } catch (java.security.cert.CertificateException e) {
            throw new CertificateException(e.getMessage());
        }
    }
    public void verify(PublicKey key, String sigProvider)
        throws CertificateException, NoSuchAlgorithmException,
        InvalidKeyException, NoSuchProviderException,
        SignatureException
    {
        try {
            wrappedCert.verify(key, sigProvider);
        } catch (java.security.cert.CertificateException e) {
            throw new CertificateException(e.getMessage());
        }
    }
    public void checkValidity() throws
      CertificateExpiredException, CertificateNotYetValidException {
        checkValidity(new Date());
    }
    public void checkValidity(Date date) throws
      CertificateExpiredException, CertificateNotYetValidException {
        try {
            wrappedCert.checkValidity(date);
        } catch (java.security.cert.CertificateNotYetValidException e) {
            throw new CertificateNotYetValidException(e.getMessage());
        } catch (java.security.cert.CertificateExpiredException e) {
            throw new CertificateExpiredException(e.getMessage());
        }
    }
    public String toString() {
        return wrappedCert.toString();
    }
    public PublicKey getPublicKey() {
        PublicKey key = wrappedCert.getPublicKey();
        return key;
    }
    public int getVersion() {
        return wrappedCert.getVersion() - 1;
    }
    public BigInteger getSerialNumber() {
        return wrappedCert.getSerialNumber();
    }
    public Principal getSubjectDN() {
        return wrappedCert.getSubjectDN();
    }
    public Principal getIssuerDN() {
        return wrappedCert.getIssuerDN();
    }
    public Date getNotBefore() {
        return wrappedCert.getNotBefore();
    }
    public Date getNotAfter() {
        return wrappedCert.getNotAfter();
    }
    public String getSigAlgName() {
        return wrappedCert.getSigAlgName();
    }
    public String getSigAlgOID() {
        return wrappedCert.getSigAlgOID();
    }
    public byte[] getSigAlgParams() {
        return wrappedCert.getSigAlgParams();
    }
    private synchronized void writeObject(ObjectOutputStream stream)
        throws IOException {
        try {
            stream.write(getEncoded());
        } catch (CertificateEncodingException e) {
            throw new IOException("getEncoded failed: " + e.getMessage());
        }
    }
    private synchronized void readObject(ObjectInputStream stream)
        throws IOException {
        try {
            wrappedCert = (java.security.cert.X509Certificate)
                getFactory().generateCertificate(stream);
        } catch (java.security.cert.CertificateException e) {
            throw new IOException("generateCertificate failed: " + e.getMessage());
        }
    }
    public java.security.cert.X509Certificate getX509Certificate() {
        return wrappedCert;
    }
}
