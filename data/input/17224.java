public class Mac implements Cloneable {
    private static final Debug debug =
                        Debug.getInstance("jca", "Mac");
    private Provider provider;
    private MacSpi spi;
    private final String algorithm;
    private boolean initialized = false;
    private Service firstService;
    private Iterator serviceIterator;
    private final Object lock;
    protected Mac(MacSpi macSpi, Provider provider, String algorithm) {
        this.spi = macSpi;
        this.provider = provider;
        this.algorithm = algorithm;
        serviceIterator = null;
        lock = null;
    }
    private Mac(Service s, Iterator t, String algorithm) {
        firstService = s;
        serviceIterator = t;
        this.algorithm = algorithm;
        lock = new Object();
    }
    public final String getAlgorithm() {
        return this.algorithm;
    }
    public static final Mac getInstance(String algorithm)
            throws NoSuchAlgorithmException {
        List services = GetInstance.getServices("Mac", algorithm);
        Iterator t = services.iterator();
        while (t.hasNext()) {
            Service s = (Service)t.next();
            if (JceSecurity.canUseProvider(s.getProvider()) == false) {
                continue;
            }
            return new Mac(s, t, algorithm);
        }
        throw new NoSuchAlgorithmException
                                ("Algorithm " + algorithm + " not available");
    }
    public static final Mac getInstance(String algorithm, String provider)
            throws NoSuchAlgorithmException, NoSuchProviderException {
        Instance instance = JceSecurity.getInstance
                ("Mac", MacSpi.class, algorithm, provider);
        return new Mac((MacSpi)instance.impl, instance.provider, algorithm);
    }
    public static final Mac getInstance(String algorithm, Provider provider)
            throws NoSuchAlgorithmException {
        Instance instance = JceSecurity.getInstance
                ("Mac", MacSpi.class, algorithm, provider);
        return new Mac((MacSpi)instance.impl, instance.provider, algorithm);
    }
    private static int warnCount = 10;
    void chooseFirstProvider() {
        if ((spi != null) || (serviceIterator == null)) {
            return;
        }
        synchronized (lock) {
            if (spi != null) {
                return;
            }
            if (debug != null) {
                int w = --warnCount;
                if (w >= 0) {
                    debug.println("Mac.init() not first method "
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
                    if (obj instanceof MacSpi == false) {
                        continue;
                    }
                    spi = (MacSpi)obj;
                    provider = s.getProvider();
                    firstService = null;
                    serviceIterator = null;
                    return;
                } catch (NoSuchAlgorithmException e) {
                    lastException = e;
                }
            }
            ProviderException e = new ProviderException
                    ("Could not construct MacSpi instance");
            if (lastException != null) {
                e.initCause(lastException);
            }
            throw e;
        }
    }
    private void chooseProvider(Key key, AlgorithmParameterSpec params)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        synchronized (lock) {
            if (spi != null) {
                spi.engineInit(key, params);
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
                    MacSpi spi = (MacSpi)s.newInstance(null);
                    spi.engineInit(key, params);
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
    public final int getMacLength() {
        chooseFirstProvider();
        return spi.engineGetMacLength();
    }
    public final void init(Key key) throws InvalidKeyException {
        try {
            if (spi != null) {
                spi.engineInit(key, null);
            } else {
                chooseProvider(key, null);
            }
        } catch (InvalidAlgorithmParameterException e) {
            throw new InvalidKeyException("init() failed", e);
        }
        initialized = true;
    }
    public final void init(Key key, AlgorithmParameterSpec params)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (spi != null) {
            spi.engineInit(key, params);
        } else {
            chooseProvider(key, params);
        }
        initialized = true;
    }
    public final void update(byte input) throws IllegalStateException {
        chooseFirstProvider();
        if (initialized == false) {
            throw new IllegalStateException("MAC not initialized");
        }
        spi.engineUpdate(input);
    }
    public final void update(byte[] input) throws IllegalStateException {
        chooseFirstProvider();
        if (initialized == false) {
            throw new IllegalStateException("MAC not initialized");
        }
        if (input != null) {
            spi.engineUpdate(input, 0, input.length);
        }
    }
    public final void update(byte[] input, int offset, int len)
            throws IllegalStateException {
        chooseFirstProvider();
        if (initialized == false) {
            throw new IllegalStateException("MAC not initialized");
        }
        if (input != null) {
            if ((offset < 0) || (len > (input.length - offset)) || (len < 0))
                throw new IllegalArgumentException("Bad arguments");
            spi.engineUpdate(input, offset, len);
        }
    }
    public final void update(ByteBuffer input) {
        chooseFirstProvider();
        if (initialized == false) {
            throw new IllegalStateException("MAC not initialized");
        }
        if (input == null) {
            throw new IllegalArgumentException("Buffer must not be null");
        }
        spi.engineUpdate(input);
    }
    public final byte[] doFinal() throws IllegalStateException {
        chooseFirstProvider();
        if (initialized == false) {
            throw new IllegalStateException("MAC not initialized");
        }
        byte[] mac = spi.engineDoFinal();
        spi.engineReset();
        return mac;
    }
    public final void doFinal(byte[] output, int outOffset)
        throws ShortBufferException, IllegalStateException
    {
        chooseFirstProvider();
        if (initialized == false) {
            throw new IllegalStateException("MAC not initialized");
        }
        int macLen = getMacLength();
        if (output == null || output.length-outOffset < macLen) {
            throw new ShortBufferException
                ("Cannot store MAC in output buffer");
        }
        byte[] mac = doFinal();
        System.arraycopy(mac, 0, output, outOffset, macLen);
        return;
    }
    public final byte[] doFinal(byte[] input) throws IllegalStateException
    {
        chooseFirstProvider();
        if (initialized == false) {
            throw new IllegalStateException("MAC not initialized");
        }
        update(input);
        return doFinal();
    }
    public final void reset() {
        chooseFirstProvider();
        spi.engineReset();
    }
    public final Object clone() throws CloneNotSupportedException {
        chooseFirstProvider();
        Mac that = (Mac)super.clone();
        that.spi = (MacSpi)this.spi.clone();
        return that;
    }
}
