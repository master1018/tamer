public abstract class X509Certificate extends Certificate {
    private static Constructor constructor;
    static {
        try {
            String classname = (String) AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                        return Security.getProperty("cert.provider.x509v1"); 
                    }
                }
            );
            Class cl = Class.forName(classname);
            constructor =
                cl.getConstructor(new Class[] {InputStream.class});
        } catch (Throwable e) {
        }
    }
    public X509Certificate() {
        super();
    }
    public static final X509Certificate getInstance(InputStream inStream)
                                             throws CertificateException {
        if (inStream == null) {
            throw new CertificateException(Messages.getString("security.87")); 
        }
        if (constructor != null) {
            try {
                return (X509Certificate) 
                    constructor.newInstance(new Object[] {inStream});
            } catch (Throwable e) {
                throw new CertificateException(e.getMessage());
            }
        }
        final java.security.cert.X509Certificate cert;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509"); 
            cert = (java.security.cert.X509Certificate)
                                            cf.generateCertificate(inStream);
        } catch (java.security.cert.CertificateException e) {
            throw new CertificateException(e.getMessage());
        }
        return new X509Certificate() {
            public byte[] getEncoded() throws CertificateEncodingException {
                try {
                    return cert.getEncoded();
                } catch (java.security.cert.CertificateEncodingException e) {
                    throw new CertificateEncodingException(e.getMessage());
                }
            }
            public void verify(PublicKey key) throws CertificateException,
                                NoSuchAlgorithmException, InvalidKeyException,
                                NoSuchProviderException, SignatureException {
                try {
                    cert.verify(key);
                } catch (java.security.cert.CertificateException e) {
                    throw new CertificateException(e.getMessage());
                }
            }
            public void verify(PublicKey key, String sigProvider)
                            throws CertificateException,
                                NoSuchAlgorithmException, InvalidKeyException,
                                NoSuchProviderException, SignatureException {
                try {
                    cert.verify(key, sigProvider);
                } catch (java.security.cert.CertificateException e) {
                    throw new CertificateException(e.getMessage());
                }
            }
            public String toString() {
                return cert.toString();
            }
            public PublicKey getPublicKey() {
                return cert.getPublicKey();
            }
            public void checkValidity() throws CertificateExpiredException,
                                   CertificateNotYetValidException {
                try {
                    cert.checkValidity();
                } catch (java.security.cert.CertificateNotYetValidException e) {
                    throw new CertificateNotYetValidException(e.getMessage());
                } catch (java.security.cert.CertificateExpiredException e) {
                    throw new CertificateExpiredException(e.getMessage());
                }
            }
            public void checkValidity(Date date) 
                            throws CertificateExpiredException,
                                   CertificateNotYetValidException {
                try {
                    cert.checkValidity(date);
                } catch (java.security.cert.CertificateNotYetValidException e) {
                    throw new CertificateNotYetValidException(e.getMessage());
                } catch (java.security.cert.CertificateExpiredException e) {
                    throw new CertificateExpiredException(e.getMessage());
                }
            }
            public int getVersion() {
                return 2;
            }
            public BigInteger getSerialNumber() {
                return cert.getSerialNumber();
            }
            public Principal getIssuerDN() {
                return cert.getIssuerDN();
            }
            public Principal getSubjectDN() {
                return cert.getSubjectDN();
            }
            public Date getNotBefore() {
                return cert.getNotBefore();
            }
            public Date getNotAfter() {
                return cert.getNotAfter();
            }
            public String getSigAlgName() {
                return cert.getSigAlgName();
            }
            public String getSigAlgOID() {
                return cert.getSigAlgOID();
            }
            public byte[] getSigAlgParams() {
                return cert.getSigAlgParams();
            }
        };
    }
    public static final X509Certificate getInstance(byte[] certData)
                                             throws CertificateException {
        if (certData == null) {
            throw new CertificateException(Messages.getString("security.88")); 
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(certData);
        return getInstance(bais);
    }
    public abstract void checkValidity()
            throws CertificateExpiredException, CertificateNotYetValidException;
    public abstract void checkValidity(Date date)
            throws CertificateExpiredException, CertificateNotYetValidException;
    public abstract int getVersion();
    public abstract BigInteger getSerialNumber();
    public abstract Principal getIssuerDN();
    public abstract Principal getSubjectDN();
    public abstract Date getNotBefore();
    public abstract Date getNotAfter();
    public abstract String getSigAlgName();
    public abstract String getSigAlgOID();
    public abstract byte[] getSigAlgParams();
}
