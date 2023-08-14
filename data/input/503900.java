public class CertificateFactory {
    private static final String SERVICE = "CertificateFactory"; 
    private static Engine engine = new Engine(SERVICE);
    private final Provider provider;
    private final CertificateFactorySpi spiImpl;
    private final String type;
    protected CertificateFactory(CertificateFactorySpi certFacSpi,
            Provider provider, String type) {
        this.provider = provider;
        this.type = type;
        this.spiImpl = certFacSpi;
    }
    public static final CertificateFactory getInstance(String type)
            throws CertificateException {
        if (type == null) {
            throw new NullPointerException(Messages.getString("security.07")); 
        }
        try {
            synchronized (engine) {
                engine.getInstance(type, null);
                return new CertificateFactory((CertificateFactorySpi) engine.spi,
                        engine.provider, type);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new CertificateException(e);
        }
    }
    public static final CertificateFactory getInstance(String type,
            String provider) throws CertificateException,
            NoSuchProviderException {
        if ((provider == null) || (provider.length() == 0)) {
            throw new IllegalArgumentException(Messages.getString("security.02")); 
        }
        Provider impProvider = Security.getProvider(provider);
        if (impProvider == null) {
            throw new NoSuchProviderException(provider);
        }
        return getInstance(type, impProvider);
    }
    public static final CertificateFactory getInstance(String type,
            Provider provider) throws CertificateException {
        if (provider == null) {
            throw new IllegalArgumentException(Messages.getString("security.04")); 
        }
        if (type == null) {
            throw new NullPointerException(Messages.getString("security.07")); 
        }
        try {
            synchronized (engine) {
                engine.getInstance(type, provider, null);
                return new CertificateFactory((CertificateFactorySpi) engine.spi,
                        provider, type);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new CertificateException(e.getMessage());
        }
    }
    public final Provider getProvider() {
        return provider;
    }
    public final String getType() {
        return type;
    }
    public final Certificate generateCertificate(InputStream inStream)
            throws CertificateException {
        return spiImpl.engineGenerateCertificate(inStream);
    }
    public final Iterator<String> getCertPathEncodings() {
        return spiImpl.engineGetCertPathEncodings();
    }
    public final CertPath generateCertPath(InputStream inStream)
            throws CertificateException {
        Iterator<String> it = getCertPathEncodings();
        if (!it.hasNext()) {
            throw new CertificateException(Messages.getString("security.74")); 
        }
        return spiImpl.engineGenerateCertPath(inStream, it.next());
    }
    public final CertPath generateCertPath(InputStream inStream, String encoding)
            throws CertificateException {
        return spiImpl.engineGenerateCertPath(inStream, encoding);
    }
    public final CertPath generateCertPath(List<? extends Certificate> certificates)
            throws CertificateException {
        return spiImpl.engineGenerateCertPath(certificates);
    }
    public final Collection<? extends Certificate> generateCertificates(InputStream inStream)
            throws CertificateException {
        return spiImpl.engineGenerateCertificates(inStream);
    }
    public final CRL generateCRL(InputStream inStream) throws CRLException {
        return spiImpl.engineGenerateCRL(inStream);
    }
    public final Collection<? extends CRL> generateCRLs(InputStream inStream)
            throws CRLException {
        return spiImpl.engineGenerateCRLs(inStream);
    }
}
