public class KeyManagerFactory {
    private Provider provider;
    private KeyManagerFactorySpi factorySpi;
    private String algorithm;
    public final static String getDefaultAlgorithm() {
        String type;
        type = AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return Security.getProperty("sun.ssl.keymanager.type");
            }
        });
        if (type == null) {
            type = "SunX509";
        }
        return type;
    }
    protected KeyManagerFactory(KeyManagerFactorySpi factorySpi,
                                Provider provider, String algorithm) {
        this.factorySpi = factorySpi;
        this.provider = provider;
        this.algorithm = algorithm;
    }
    public final String getAlgorithm() {
        return this.algorithm;
    }
    public static final KeyManagerFactory getInstance(String algorithm)
        throws NoSuchAlgorithmException
    {
        try {
            Object[] objs = SSLSecurity.getImpl(algorithm, "KeyManagerFactory",
                                                (String) null);
            return new KeyManagerFactory((KeyManagerFactorySpi)objs[0],
                                    (Provider)objs[1],
                                    algorithm);
        } catch (NoSuchProviderException e) {
            throw new NoSuchAlgorithmException(algorithm + " not found");
        }
    }
    public static final KeyManagerFactory getInstance(String algorithm,
                                                 String provider)
        throws NoSuchAlgorithmException, NoSuchProviderException
    {
        if (provider == null || provider.length() == 0)
            throw new IllegalArgumentException("missing provider");
        Object[] objs = SSLSecurity.getImpl(algorithm, "KeyManagerFactory",
                                            provider);
        return new KeyManagerFactory((KeyManagerFactorySpi)objs[0],
                                        (Provider)objs[1], algorithm);
    }
    public static final KeyManagerFactory getInstance(String algorithm,
                                                 Provider provider)
        throws NoSuchAlgorithmException
    {
        if (provider == null)
            throw new IllegalArgumentException("missing provider");
        Object[] objs = SSLSecurity.getImpl(algorithm, "KeyManagerFactory",
                                            provider);
        return new KeyManagerFactory((KeyManagerFactorySpi)objs[0],
                                        (Provider)objs[1], algorithm);
    }
    public final Provider getProvider() {
        return this.provider;
    }
    public void init(KeyStore ks, char[] password)
        throws KeyStoreException, NoSuchAlgorithmException,
            UnrecoverableKeyException {
        factorySpi.engineInit(ks, password);
    }
    public KeyManager[] getKeyManagers() {
        return factorySpi.engineGetKeyManagers();
    }
}
