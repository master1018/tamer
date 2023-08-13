public class KeyAgreement {
    private static final Debug debug =
                        Debug.getInstance("jca", "KeyAgreement");
    private Provider provider;
    private KeyAgreementSpi spi;
    private final String algorithm;
    private Service firstService;
    private Iterator serviceIterator;
    private final Object lock;
    protected KeyAgreement(KeyAgreementSpi keyAgreeSpi, Provider provider,
                           String algorithm) {
        this.spi = keyAgreeSpi;
        this.provider = provider;
        this.algorithm = algorithm;
        lock = null;
    }
    private KeyAgreement(Service s, Iterator t, String algorithm) {
        firstService = s;
        serviceIterator = t;
        this.algorithm = algorithm;
        lock = new Object();
    }
    public final String getAlgorithm() {
        return this.algorithm;
    }
    public static final KeyAgreement getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        List services = GetInstance.getServices("KeyAgreement", algorithm);
        Iterator t = services.iterator();
        while (t.hasNext()) {
            Service s = (Service)t.next();
            if (JceSecurity.canUseProvider(s.getProvider()) == false) {
                continue;
            }
            return new KeyAgreement(s, t, algorithm);
        }
        throw new NoSuchAlgorithmException
                                ("Algorithm " + algorithm + " not available");
    }
    public static final KeyAgreement getInstance(String algorithm,
            String provider) throws NoSuchAlgorithmException,
            NoSuchProviderException {
        Instance instance = JceSecurity.getInstance
                ("KeyAgreement", KeyAgreementSpi.class, algorithm, provider);
        return new KeyAgreement((KeyAgreementSpi)instance.impl,
                instance.provider, algorithm);
    }
    public static final KeyAgreement getInstance(String algorithm,
            Provider provider) throws NoSuchAlgorithmException {
        Instance instance = JceSecurity.getInstance
                ("KeyAgreement", KeyAgreementSpi.class, algorithm, provider);
        return new KeyAgreement((KeyAgreementSpi)instance.impl,
                instance.provider, algorithm);
    }
    private static int warnCount = 10;
    void chooseFirstProvider() {
        if (spi != null) {
            return;
        }
        synchronized (lock) {
            if (spi != null) {
                return;
            }
            if (debug != null) {
                int w = --warnCount;
                if (w >= 0) {
                    debug.println("KeyAgreement.init() not first method "
                        + "called, disabling delayed provider selection");
                    if (w == 0) {
                        debug.println("Further warnings of this type will "
                            + "be suppressed");
                    }
                    new Exception("Call trace").printStackTrace();
                }
            }
            Exception lastException = null;
            while ((firstService != null) || serviceIterator.hasNext()) {
                Service s;
                if (firstService != null) {
                    s = firstService;
                    firstService = null;
                } else {
                    s = (Service)serviceIterator.next();
                }
                if (JceSecurity.canUseProvider(s.getProvider()) == false) {
                    continue;
                }
                try {
                    Object obj = s.newInstance(null);
                    if (obj instanceof KeyAgreementSpi == false) {
                        continue;
                    }
                    spi = (KeyAgreementSpi)obj;
                    provider = s.getProvider();
                    firstService = null;
                    serviceIterator = null;
                    return;
                } catch (Exception e) {
                    lastException = e;
                }
            }
            ProviderException e = new ProviderException
                    ("Could not construct KeyAgreementSpi instance");
            if (lastException != null) {
                e.initCause(lastException);
            }
            throw e;
        }
    }
    private final static int I_NO_PARAMS = 1;
    private final static int I_PARAMS    = 2;
    private void implInit(KeyAgreementSpi spi, int type, Key key,
            AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (type == I_NO_PARAMS) {
            spi.engineInit(key, random);
        } else { 
            spi.engineInit(key, params, random);
        }
    }
    private void chooseProvider(int initType, Key key,
            AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        synchronized (lock) {
            if (spi != null) {
                implInit(spi, initType, key, params, random);
                return;
            }
            Exception lastException = null;
            while ((firstService != null) || serviceIterator.hasNext()) {
                Service s;
                if (firstService != null) {
                    s = firstService;
                    firstService = null;
                } else {
                    s = (Service)serviceIterator.next();
                }
                if (s.supportsParameter(key) == false) {
                    continue;
                }
                if (JceSecurity.canUseProvider(s.getProvider()) == false) {
                    continue;
                }
                try {
                    KeyAgreementSpi spi = (KeyAgreementSpi)s.newInstance(null);
                    implInit(spi, initType, key, params, random);
                    provider = s.getProvider();
                    this.spi = spi;
                    firstService = null;
                    serviceIterator = null;
                    return;
                } catch (Exception e) {
                    if (lastException == null) {
                        lastException = e;
                    }
                }
            }
            if (lastException instanceof InvalidKeyException) {
                throw (InvalidKeyException)lastException;
            }
            if (lastException instanceof InvalidAlgorithmParameterException) {
                throw (InvalidAlgorithmParameterException)lastException;
            }
            if (lastException instanceof RuntimeException) {
                throw (RuntimeException)lastException;
            }
            String kName = (key != null) ? key.getClass().getName() : "(null)";
            throw new InvalidKeyException
                ("No installed provider supports this key: "
                + kName, lastException);
        }
    }
    public final Provider getProvider() {
        chooseFirstProvider();
        return this.provider;
    }
    public final void init(Key key) throws InvalidKeyException {
        init(key, JceSecurity.RANDOM);
    }
    public final void init(Key key, SecureRandom random)
            throws InvalidKeyException {
        if (spi != null) {
            spi.engineInit(key, random);
        } else {
            try {
                chooseProvider(I_NO_PARAMS, key, null, random);
            } catch (InvalidAlgorithmParameterException e) {
                throw new InvalidKeyException(e);
            }
        }
    }
    public final void init(Key key, AlgorithmParameterSpec params)
        throws InvalidKeyException, InvalidAlgorithmParameterException
    {
        init(key, params, JceSecurity.RANDOM);
    }
    public final void init(Key key, AlgorithmParameterSpec params,
                           SecureRandom random)
        throws InvalidKeyException, InvalidAlgorithmParameterException
    {
        if (spi != null) {
            spi.engineInit(key, params, random);
        } else {
            chooseProvider(I_PARAMS, key, params, random);
        }
    }
    public final Key doPhase(Key key, boolean lastPhase)
        throws InvalidKeyException, IllegalStateException
    {
        chooseFirstProvider();
        return spi.engineDoPhase(key, lastPhase);
    }
    public final byte[] generateSecret() throws IllegalStateException {
        chooseFirstProvider();
        return spi.engineGenerateSecret();
    }
    public final int generateSecret(byte[] sharedSecret, int offset)
        throws IllegalStateException, ShortBufferException
    {
        chooseFirstProvider();
        return spi.engineGenerateSecret(sharedSecret, offset);
    }
    public final SecretKey generateSecret(String algorithm)
        throws IllegalStateException, NoSuchAlgorithmException,
            InvalidKeyException
    {
        chooseFirstProvider();
        return spi.engineGenerateSecret(algorithm);
    }
}
