public class SecureRandom extends Random {
    private static final long serialVersionUID = 4940670005562187L;
    private static final transient String SERVICE = "SecureRandom"; 
    private static transient Engine engine = new Engine(SERVICE);
    private Provider provider;
    private SecureRandomSpi secureRandomSpi;
    private String algorithm;
    private byte[] state;
    private byte[] randomBytes;
    private int randomBytesUsed;
    private long counter;
    private static transient SecureRandom internalSecureRandom;
    public SecureRandom() {
        super(0);
        Provider.Service service = findService();
        if (service == null) {
            this.provider = null;
            this.secureRandomSpi = new SHA1PRNG_SecureRandomImpl();
            this.algorithm = "SHA1PRNG"; 
        } else {
            try {
                this.provider = service.getProvider();
                this.secureRandomSpi = (SecureRandomSpi)service.newInstance(null);
                this.algorithm = service.getAlgorithm();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }            
        }    
    }
    public SecureRandom(byte[] seed) {
        this();
        setSeed(seed);
    }
    private Provider.Service findService() {
        Set s;
        Provider.Service service;
        for (Iterator it1 = Services.getProvidersList().iterator(); it1.hasNext();) {
            service = ((Provider)it1.next()).getService("SecureRandom"); 
            if (service != null) {
                return service;
            }
        }
        return null;
    }
    protected SecureRandom(SecureRandomSpi secureRandomSpi,
                           Provider provider) {
        this(secureRandomSpi, provider, "unknown"); 
    }
    private SecureRandom(SecureRandomSpi secureRandomSpi,
                         Provider provider,
                         String algorithm) {
        super(0);
        this.provider = provider;
        this.algorithm = algorithm;
        this.secureRandomSpi = secureRandomSpi;
    }
    public static SecureRandom getInstance(String algorithm)
                                throws NoSuchAlgorithmException {
        if (algorithm == null) {
            throw new NullPointerException(Messages.getString("security.01")); 
        }
        synchronized (engine) {
            engine.getInstance(algorithm, null);
            return new SecureRandom((SecureRandomSpi)engine.spi, engine.provider, algorithm);
        }
    }
    public static SecureRandom getInstance(String algorithm, String provider)
                                throws NoSuchAlgorithmException, NoSuchProviderException {
        if ((provider == null) || (provider.length() == 0)) {
            throw new IllegalArgumentException(
                    Messages.getString("security.02")); 
        }
        Provider p = Security.getProvider(provider);
        if (p == null) {
            throw new NoSuchProviderException(Messages.getString("security.03", provider));  
        }
        return getInstance(algorithm, p);    
    }
    public static SecureRandom getInstance(String algorithm, Provider provider)
                                throws NoSuchAlgorithmException {
        if (provider == null) {
            throw new IllegalArgumentException(Messages.getString("security.04")); 
        }
        if (algorithm == null) {
            throw new NullPointerException(Messages.getString("security.01")); 
        }
        synchronized (engine) {
            engine.getInstance(algorithm, provider, null);
            return new SecureRandom((SecureRandomSpi)engine.spi, provider, algorithm);
        }
    }
    public final Provider getProvider() {
        return provider;
    }
    public String getAlgorithm() {
        return algorithm;
    }
    public synchronized void setSeed(byte[] seed) {
        secureRandomSpi.engineSetSeed(seed);
    }
    @Override
    public void setSeed(long seed) {
        if (seed == 0) {    
            return;
        }
        byte[] byteSeed = {
                (byte)((seed >> 56) & 0xFF),
                (byte)((seed >> 48) & 0xFF),
                (byte)((seed >> 40) & 0xFF),
                (byte)((seed >> 32) & 0xFF),
                (byte)((seed >> 24) & 0xFF),
                (byte)((seed >> 16) & 0xFF),
                (byte)((seed >> 8) & 0xFF),
                (byte)((seed) & 0xFF)
        };
        setSeed(byteSeed);
    }
    @Override
    public synchronized void nextBytes(byte[] bytes) {
        secureRandomSpi.engineNextBytes(bytes);
    }
    @Override
    protected final int next(int numBits) {
        if (numBits < 0) {
            numBits = 0;
        } else {
            if (numBits > 32) {
                numBits = 32;
            }
        }
        int bytes = (numBits+7)/8;
        byte[] next = new byte[bytes];
        int ret = 0;
        nextBytes(next);
        for (int i = 0; i < bytes; i++) {
            ret = (next[i] & 0xFF) | (ret << 8);
        }    
        ret = ret >>> (bytes*8 - numBits);
        return ret;
    }
    public static byte[] getSeed(int numBytes) {
        if (internalSecureRandom == null) {
            internalSecureRandom = new SecureRandom();
        }
        return internalSecureRandom.generateSeed(numBytes);
    }
    public byte[] generateSeed(int numBytes) {
        return secureRandomSpi.engineGenerateSeed(numBytes);
    }
}
