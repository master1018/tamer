public class CertStore {
    private static final String SERVICE = "CertStore"; 
    private static Engine engine = new Engine(SERVICE);
    private static final String PROPERTYNAME = "certstore.type"; 
    private static final String DEFAULTPROPERTY = "LDAP"; 
    private final Provider provider;
    private final CertStoreSpi spiImpl;
    private final String type;
    private final CertStoreParameters certStoreParams;
    protected CertStore(CertStoreSpi storeSpi, Provider provider, String type,
            CertStoreParameters params) {
        this.provider = provider;
        this.type = type;
        this.spiImpl = storeSpi;
        this.certStoreParams = params;
    }
    public static CertStore getInstance(String type, CertStoreParameters params)
            throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        if (type == null) {
            throw new NullPointerException(Messages.getString("security.07")); 
        }
        try {
            synchronized (engine) {
                engine.getInstance(type, params);
                return new CertStore((CertStoreSpi) engine.spi, engine.provider,
                        type, params);
            }
        } catch (NoSuchAlgorithmException e) {
            Throwable th = e.getCause();
            if (th == null) {
                throw e;
            } else {
                throw new InvalidAlgorithmParameterException(e.getMessage(), th);
            }
        }
    }
    public static CertStore getInstance(String type,
            CertStoreParameters params, String provider)
            throws InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException {
        if ((provider == null) || (provider.length() == 0)) {
            throw new IllegalArgumentException(Messages.getString("security.02")); 
        }
        Provider impProvider = Security.getProvider(provider);
        if (impProvider == null) {
            throw new NoSuchProviderException(provider);
        }
        return getInstance(type, params, impProvider);
    }
    public static CertStore getInstance(String type,
            CertStoreParameters params, Provider provider)
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        if (provider == null) {
            throw new IllegalArgumentException(Messages.getString("security.04")); 
        }
        if (type == null) {
            throw new NullPointerException(Messages.getString("security.07")); 
        }
        try {
            synchronized (engine) {
                engine.getInstance(type, provider, params);
                return new CertStore((CertStoreSpi) engine.spi, provider, type,
                        params);
            }
        } catch (NoSuchAlgorithmException e) {
            Throwable th = e.getCause();
            if (th == null) {
                throw e;
            } else {
                throw new InvalidAlgorithmParameterException(e.getMessage(), th);
            }
        }
    }
    public final String getType() {
        return type;
    }
    public final Provider getProvider() {
        return provider;
    }
    public final CertStoreParameters getCertStoreParameters() {
        if (certStoreParams == null) {
            return null;
        } else {
            return (CertStoreParameters) certStoreParams.clone();
        }
    }
    public final Collection<? extends Certificate> getCertificates(CertSelector selector)
            throws CertStoreException {
        return spiImpl.engineGetCertificates(selector);
    }
    public final Collection<? extends CRL> getCRLs(CRLSelector selector)
            throws CertStoreException {
        return spiImpl.engineGetCRLs(selector);
    }
    public static final String getDefaultType() {
        String defaultType = AccessController
                .doPrivileged(new java.security.PrivilegedAction<String>() {
                    public String run() {
                        return Security.getProperty(PROPERTYNAME);
                    }
                });
        return (defaultType == null ? DEFAULTPROPERTY : defaultType);
    }
}
