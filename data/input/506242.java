public class X509CertImpl extends X509Certificate {
    private static final long serialVersionUID = 2972248729446736154L;
    private final Certificate certificate;
    private final TBSCertificate tbsCert;
    private final Extensions extensions;
    private long notBefore = -1;
    private long notAfter;
    private BigInteger serialNumber;
    private X500Principal issuer;
    private X500Principal subject;
    private byte[] tbsCertificate;
    private byte[] signature;
    private String sigAlgName;
    private String sigAlgOID;
    private byte[] sigAlgParams;
    private boolean nullSigAlgParams;
    private PublicKey publicKey;
    private volatile byte[] encoding;
    public X509CertImpl(InputStream in) throws CertificateException {
        try {
            this.certificate = (Certificate) Certificate.ASN1.decode(in);
            this.tbsCert = certificate.getTbsCertificate();
            this.extensions = tbsCert.getExtensions();
        } catch (IOException e) {
            throw new CertificateException(e);
        }
    }
    public X509CertImpl(Certificate certificate) {
        this.certificate = certificate;
        this.tbsCert = certificate.getTbsCertificate();
        this.extensions = tbsCert.getExtensions();
    }
    public X509CertImpl(byte[] encoding) throws IOException {
        this((Certificate) Certificate.ASN1.decode(encoding));
    }
    public void checkValidity() throws CertificateExpiredException,
                                       CertificateNotYetValidException {
        if (notBefore == -1) {
            notBefore = tbsCert.getValidity().getNotBefore().getTime();
            notAfter = tbsCert.getValidity().getNotAfter().getTime();
        }
        long time = System.currentTimeMillis();
        if (time < notBefore) {
            throw new CertificateNotYetValidException();
        }
        if (time > notAfter) {
            throw new CertificateExpiredException();
        }
    }
    public void checkValidity(Date date)
                                throws CertificateExpiredException,
                                       CertificateNotYetValidException {
        if (notBefore == -1) {
            notBefore = tbsCert.getValidity().getNotBefore().getTime();
            notAfter = tbsCert.getValidity().getNotAfter().getTime();
        }
        long time = date.getTime();
        if (time < notBefore) {
            throw new CertificateNotYetValidException("current time: " + date
                + ", validation time: " + new Date(notBefore));
        }
        if (time > notAfter) {
            throw new CertificateExpiredException("current time: " + date
                + ", expiration time: " + new Date(notAfter));
        }
    }
    public int getVersion() {
        return tbsCert.getVersion() + 1;
    }
    public BigInteger getSerialNumber() {
        if (serialNumber == null) {
            serialNumber = tbsCert.getSerialNumber();
        }
        return serialNumber;
    }
    public Principal getIssuerDN() {
        if (issuer == null) {
            issuer = tbsCert.getIssuer().getX500Principal();
        }
        return issuer;
    }
    public X500Principal getIssuerX500Principal() {
        if (issuer == null) {
            issuer = tbsCert.getIssuer().getX500Principal();
        }
        return issuer;
    }
    public Principal getSubjectDN() {
        if (subject == null) {
            subject = tbsCert.getSubject().getX500Principal();
        }
        return subject;
    }
    public X500Principal getSubjectX500Principal() {
        if (subject == null) {
            subject = tbsCert.getSubject().getX500Principal();
        }
        return subject;
    }
    public Date getNotBefore() {
        if (notBefore == -1) {
            notBefore = tbsCert.getValidity().getNotBefore().getTime();
            notAfter = tbsCert.getValidity().getNotAfter().getTime();
        }
        return new Date(notBefore);
    }
    public Date getNotAfter() {
        if (notBefore == -1) {
            notBefore = tbsCert.getValidity().getNotBefore().getTime();
            notAfter = tbsCert.getValidity().getNotAfter().getTime();
        }
        return new Date(notAfter);
    }
    public byte[] getTBSCertificate()
                        throws CertificateEncodingException {
        if (tbsCertificate == null) {
            tbsCertificate = tbsCert.getEncoded();
        }
        byte[] result = new byte[tbsCertificate.length];
        System.arraycopy(tbsCertificate, 0, result, 0, tbsCertificate.length);
        return result;
    }
    public byte[] getSignature() {
        if (signature == null) {
            signature = certificate.getSignatureValue();
        }
        byte[] result = new byte[signature.length];
        System.arraycopy(signature, 0, result, 0, signature.length);
        return result;
    }
    public String getSigAlgName() {
        if (sigAlgOID == null) {
            sigAlgOID = tbsCert.getSignature().getAlgorithm();
            sigAlgName = AlgNameMapper.map2AlgName(sigAlgOID);
            if (sigAlgName == null) {
                sigAlgName = sigAlgOID;
            }
        }
        return sigAlgName;
    }
    public String getSigAlgOID() {
        if (sigAlgOID == null) {
            sigAlgOID = tbsCert.getSignature().getAlgorithm();
            sigAlgName = AlgNameMapper.map2AlgName(sigAlgOID);
            if (sigAlgName == null) {
                sigAlgName = sigAlgOID;
            }
        }
        return sigAlgOID;
    }
    public byte[] getSigAlgParams() {
        if (nullSigAlgParams) {
            return null;
        }
        if (sigAlgParams == null) {
            sigAlgParams = tbsCert.getSignature().getParameters();
            if (sigAlgParams == null) {
                nullSigAlgParams = true;
                return null;
            }
        }
        return sigAlgParams;
    }
    public boolean[] getIssuerUniqueID() {
        return tbsCert.getIssuerUniqueID();
    }
    public boolean[] getSubjectUniqueID() {
        return tbsCert.getSubjectUniqueID();
    }
    public boolean[] getKeyUsage() {
        if (extensions == null) {
            return null;
        }
        return extensions.valueOfKeyUsage();
    }
    public List getExtendedKeyUsage()
                                throws CertificateParsingException {
        if (extensions == null) {
            return null;
        }
        try {
            return extensions.valueOfExtendedKeyUsage();
        } catch (IOException e) {
            throw new CertificateParsingException(e);
        }
    }
    public int getBasicConstraints() {
        if (extensions == null) {
            return Integer.MAX_VALUE;
        }
        return extensions.valueOfBasicConstrains();
    }
    public Collection getSubjectAlternativeNames()
                                throws CertificateParsingException {
        if (extensions == null) {
            return null;
        }
        try {
            return extensions.valueOfSubjectAlternativeName();
        } catch (IOException e) {
            throw new CertificateParsingException(e);
        }
    }
    public Collection getIssuerAlternativeNames()
                                throws CertificateParsingException {
        if (extensions == null) {
            return null;
        }
        try {
            return extensions.valueOfIssuerAlternativeName();
        } catch (IOException e) {
            throw new CertificateParsingException(e);
        }
    }
    public byte[] getEncoded() throws CertificateEncodingException {
        if (encoding == null) {
            encoding = certificate.getEncoded();
        }
        byte[] result = new byte[encoding.length];
        System.arraycopy(encoding, 0, result, 0, encoding.length);
        return result;
    }
    public PublicKey getPublicKey() {
        if (publicKey == null) {
            publicKey = tbsCert.getSubjectPublicKeyInfo().getPublicKey();
        }
        return publicKey;
    }
    public String toString() {
        return certificate.toString();
    }
    public void verify(PublicKey key)
                         throws CertificateException, NoSuchAlgorithmException,
                                InvalidKeyException, NoSuchProviderException,
                                SignatureException {
        if (getSigAlgName().endsWith("withRSA")) {
            fastVerify(key);
            return;
        }
        Signature signature = Signature.getInstance(getSigAlgName());
        signature.initVerify(key);
        if (tbsCertificate == null) {
            tbsCertificate = tbsCert.getEncoded();
        }
        signature.update(tbsCertificate, 0, tbsCertificate.length);
        if (!signature.verify(certificate.getSignatureValue())) {
            throw new SignatureException(Messages.getString("security.15C")); 
        }
    }
    public void verify(PublicKey key, String sigProvider)
                         throws CertificateException, NoSuchAlgorithmException,
                                InvalidKeyException, NoSuchProviderException,
                                SignatureException {
        if (getSigAlgName().endsWith("withRSA")) {
            fastVerify(key);
            return;
        }
        Signature signature =
            Signature.getInstance(getSigAlgName(), sigProvider);
        signature.initVerify(key);
        if (tbsCertificate == null) {
            tbsCertificate = tbsCert.getEncoded();
        }
        signature.update(tbsCertificate, 0, tbsCertificate.length);
        if (!signature.verify(certificate.getSignatureValue())) {
            throw new SignatureException(Messages.getString("security.15C")); 
        }
    }
    private void fastVerify(PublicKey key) throws SignatureException,
            InvalidKeyException, NoSuchAlgorithmException {
        if (!(key instanceof RSAPublicKey)) {
            throw new InvalidKeyException(Messages.getString("security.15C1"));
        }
        RSAPublicKey rsaKey = (RSAPublicKey) key;
        String algorithm = getSigAlgName();
        if ("MD2withRSA".equalsIgnoreCase(algorithm) ||
                "MD2withRSAEncryption".equalsIgnoreCase(algorithm) ||
                "1.2.840.113549.1.1.2".equalsIgnoreCase(algorithm) ||
                "MD2/RSA".equalsIgnoreCase(algorithm)) {
            throw new NoSuchAlgorithmException(algorithm);
        }
        int i = algorithm.indexOf("with");
        algorithm = algorithm.substring(i + 4) + "-" + algorithm.substring(0, i);
        if (tbsCertificate == null) {
            tbsCertificate = tbsCert.getEncoded();
        }
        byte[] sig = certificate.getSignatureValue();
        if (!OpenSSLSocketImpl.verifySignature(tbsCertificate, sig, algorithm, rsaKey)) {
            throw new SignatureException(Messages.getString("security.15C")); 
        }
    }
    public Set getNonCriticalExtensionOIDs() {
        if (extensions == null) {
            return null;
        }
        return extensions.getNonCriticalExtensions();
    }
    public Set getCriticalExtensionOIDs() {
        if (extensions == null) {
            return null;
        }
        return extensions.getCriticalExtensions();
    }
    public byte[] getExtensionValue(String oid) {
        if (extensions == null) {
            return null;
        }
        Extension ext = extensions.getExtensionByOID(oid);
        return (ext == null) ? null : ext.getRawExtnValue();
    }
    public boolean hasUnsupportedCriticalExtension() {
        if (extensions == null) {
            return false;
        }
        return extensions.hasUnsupportedCritical();
    }
}
