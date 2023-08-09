public class KeyGenerator {
    private final static int I_NONE   = 1;
    private final static int I_RANDOM = 2;
    private final static int I_PARAMS = 3;
    private final static int I_SIZE   = 4;
    private Provider provider;
    private volatile KeyGeneratorSpi spi;
    private final String algorithm;
    private final Object lock = new Object();
    private Iterator serviceIterator;
    private int initType;
    private int initKeySize;
    private AlgorithmParameterSpec initParams;
    private SecureRandom initRandom;
    protected KeyGenerator(KeyGeneratorSpi keyGenSpi, Provider provider,
                           String algorithm) {
        this.spi = keyGenSpi;
        this.provider = provider;
        this.algorithm = algorithm;
    }
    private KeyGenerator(String algorithm) throws NoSuchAlgorithmException {
        this.algorithm = algorithm;
        List list = GetInstance.getServices("KeyGenerator", algorithm);
        serviceIterator = list.iterator();
        initType = I_NONE;
        if (nextSpi(null, false) == null) {
            throw new NoSuchAlgorithmException
                (algorithm + " KeyGenerator not available");
        }
    }
    public final String getAlgorithm() {
        return this.algorithm;
    }
    public static final KeyGenerator getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        return new KeyGenerator(algorithm);
    }
    public static final KeyGenerator getInstance(String algorithm,
            String provider) throws NoSuchAlgorithmException,
            NoSuchProviderException {
        Instance instance = JceSecurity.getInstance("KeyGenerator",
                KeyGeneratorSpi.class, algorithm, provider);
        return new KeyGenerator((KeyGeneratorSpi)instance.impl,
                instance.provider, algorithm);
    }
    public static final KeyGenerator getInstance(String algorithm,
            Provider provider) throws NoSuchAlgorithmException {
        Instance instance = JceSecurity.getInstance("KeyGenerator",
                KeyGeneratorSpi.class, algorithm, provider);
        return new KeyGenerator((KeyGeneratorSpi)instance.impl,
                instance.provider, algorithm);
    }
    public final Provider getProvider() {
        synchronized (lock) {
            disableFailover();
            return provider;
        }
    }
    private KeyGeneratorSpi nextSpi(KeyGeneratorSpi oldSpi,
            boolean reinit) {
        synchronized (lock) {
            if ((oldSpi != null) && (oldSpi != spi)) {
                return spi;
            }
            if (serviceIterator == null) {
                return null;
            }
            while (serviceIterator.hasNext()) {
                Service s = (Service)serviceIterator.next();
                if (JceSecurity.canUseProvider(s.getProvider()) == false) {
                    continue;
                }
                try {
                    Object inst = s.newInstance(null);
                    if (inst instanceof KeyGeneratorSpi == false) {
                        continue;
                    }
                    KeyGeneratorSpi spi = (KeyGeneratorSpi)inst;
                    if (reinit) {
                        if (initType == I_SIZE) {
                            spi.engineInit(initKeySize, initRandom);
                        } else if (initType == I_PARAMS) {
                            spi.engineInit(initParams, initRandom);
                        } else if (initType == I_RANDOM) {
                            spi.engineInit(initRandom);
                        } else if (initType != I_NONE) {
                            throw new AssertionError
                                ("KeyGenerator initType: " + initType);
                        }
                    }
                    provider = s.getProvider();
                    this.spi = spi;
                    return spi;
                } catch (Exception e) {
                }
            }
            disableFailover();
            return null;
        }
    }
    void disableFailover() {
        serviceIterator = null;
        initType = 0;
        initParams = null;
        initRandom = null;
    }
    public final void init(SecureRandom random) {
        if (serviceIterator == null) {
            spi.engineInit(random);
            return;
        }
        RuntimeException failure = null;
        KeyGeneratorSpi mySpi = spi;
        do {
            try {
                mySpi.engineInit(random);
                initType = I_RANDOM;
                initKeySize = 0;
                initParams = null;
                initRandom = random;
                return;
            } catch (RuntimeException e) {
                if (failure == null) {
                    failure = e;
                }
                mySpi = nextSpi(mySpi, false);
            }
        } while (mySpi != null);
        throw failure;
    }
    public final void init(AlgorithmParameterSpec params)
        throws InvalidAlgorithmParameterException
    {
        init(params, JceSecurity.RANDOM);
    }
    public final void init(AlgorithmParameterSpec params, SecureRandom random)
        throws InvalidAlgorithmParameterException
    {
        if (serviceIterator == null) {
            spi.engineInit(params, random);
            return;
        }
        Exception failure = null;
        KeyGeneratorSpi mySpi = spi;
        do {
            try {
                mySpi.engineInit(params, random);
                initType = I_PARAMS;
                initKeySize = 0;
                initParams = params;
                initRandom = random;
                return;
            } catch (Exception e) {
                if (failure == null) {
                    failure = e;
                }
                mySpi = nextSpi(mySpi, false);
            }
        } while (mySpi != null);
        if (failure instanceof InvalidAlgorithmParameterException) {
            throw (InvalidAlgorithmParameterException)failure;
        }
        if (failure instanceof RuntimeException) {
            throw (RuntimeException)failure;
        }
        throw new InvalidAlgorithmParameterException("init() failed", failure);
    }
    public final void init(int keysize) {
        init(keysize, JceSecurity.RANDOM);
    }
    public final void init(int keysize, SecureRandom random) {
        if (serviceIterator == null) {
            spi.engineInit(keysize, random);
            return;
        }
        RuntimeException failure = null;
        KeyGeneratorSpi mySpi = spi;
        do {
            try {
                mySpi.engineInit(keysize, random);
                initType = I_SIZE;
                initKeySize = keysize;
                initParams = null;
                initRandom = random;
                return;
            } catch (RuntimeException e) {
                if (failure == null) {
                    failure = e;
                }
                mySpi = nextSpi(mySpi, false);
            }
        } while (mySpi != null);
        throw failure;
    }
    public final SecretKey generateKey() {
        if (serviceIterator == null) {
            return spi.engineGenerateKey();
        }
        RuntimeException failure = null;
        KeyGeneratorSpi mySpi = spi;
        do {
            try {
                return mySpi.engineGenerateKey();
            } catch (RuntimeException e) {
                if (failure == null) {
                    failure = e;
                }
                mySpi = nextSpi(mySpi, true);
            }
        } while (mySpi != null);
        throw failure;
   }
}
