final class KeyGeneratorCore {
    private final String name;
    private final int defaultKeySize;
    private int keySize;
    private SecureRandom random;
    KeyGeneratorCore(String name, int defaultKeySize) {
        this.name = name;
        this.defaultKeySize = defaultKeySize;
        implInit(null);
    }
    void implInit(SecureRandom random) {
        this.keySize = defaultKeySize;
        this.random = random;
    }
    void implInit(AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidAlgorithmParameterException {
        throw new InvalidAlgorithmParameterException
            (name + " key generation does not take any parameters");
    }
    void implInit(int keysize, SecureRandom random) {
        if (keysize < 40) {
            throw new InvalidParameterException
                ("Key length must be at least 40 bits");
        }
        this.keySize = keysize;
        this.random = random;
    }
    SecretKey implGenerateKey() {
        if (random == null) {
            random = SunJCE.RANDOM;
        }
        byte[] b = new byte[(keySize + 7) >> 3];
        random.nextBytes(b);
        return new SecretKeySpec(b, name);
    }
    public static final class HmacSHA256KG extends KeyGeneratorSpi {
        private final KeyGeneratorCore core;
        public HmacSHA256KG() {
            core = new KeyGeneratorCore("HmacSHA256", 256);
        }
        protected void engineInit(SecureRandom random) {
            core.implInit(random);
        }
        protected void engineInit(AlgorithmParameterSpec params,
                SecureRandom random) throws InvalidAlgorithmParameterException {
            core.implInit(params, random);
        }
        protected void engineInit(int keySize, SecureRandom random) {
            core.implInit(keySize, random);
        }
        protected SecretKey engineGenerateKey() {
            return core.implGenerateKey();
        }
    }
    public static final class HmacSHA384KG extends KeyGeneratorSpi {
        private final KeyGeneratorCore core;
        public HmacSHA384KG() {
            core = new KeyGeneratorCore("HmacSHA384", 384);
        }
        protected void engineInit(SecureRandom random) {
            core.implInit(random);
        }
        protected void engineInit(AlgorithmParameterSpec params,
                SecureRandom random) throws InvalidAlgorithmParameterException {
            core.implInit(params, random);
        }
        protected void engineInit(int keySize, SecureRandom random) {
            core.implInit(keySize, random);
        }
        protected SecretKey engineGenerateKey() {
            return core.implGenerateKey();
        }
    }
    public static final class HmacSHA512KG extends KeyGeneratorSpi {
        private final KeyGeneratorCore core;
        public HmacSHA512KG() {
            core = new KeyGeneratorCore("HmacSHA512", 512);
        }
        protected void engineInit(SecureRandom random) {
            core.implInit(random);
        }
        protected void engineInit(AlgorithmParameterSpec params,
                SecureRandom random) throws InvalidAlgorithmParameterException {
            core.implInit(params, random);
        }
        protected void engineInit(int keySize, SecureRandom random) {
            core.implInit(keySize, random);
        }
        protected SecretKey engineGenerateKey() {
            return core.implGenerateKey();
        }
    }
    public static final class RC2KeyGenerator extends KeyGeneratorSpi {
        private final KeyGeneratorCore core;
        public RC2KeyGenerator() {
            core = new KeyGeneratorCore("RC2", 128);
        }
        protected void engineInit(SecureRandom random) {
            core.implInit(random);
        }
        protected void engineInit(AlgorithmParameterSpec params,
                SecureRandom random) throws InvalidAlgorithmParameterException {
            core.implInit(params, random);
        }
        protected void engineInit(int keySize, SecureRandom random) {
            if ((keySize < 40) || (keySize > 1024)) {
                throw new InvalidParameterException("Key length for RC2"
                    + " must be between 40 and 1024 bits");
            }
            core.implInit(keySize, random);
        }
        protected SecretKey engineGenerateKey() {
            return core.implGenerateKey();
        }
    }
    public static final class ARCFOURKeyGenerator extends KeyGeneratorSpi {
        private final KeyGeneratorCore core;
        public ARCFOURKeyGenerator() {
            core = new KeyGeneratorCore("ARCFOUR", 128);
        }
        protected void engineInit(SecureRandom random) {
            core.implInit(random);
        }
        protected void engineInit(AlgorithmParameterSpec params,
                SecureRandom random) throws InvalidAlgorithmParameterException {
            core.implInit(params, random);
        }
        protected void engineInit(int keySize, SecureRandom random) {
            if ((keySize < 40) || (keySize > 1024)) {
                throw new InvalidParameterException("Key length for ARCFOUR"
                    + " must be between 40 and 1024 bits");
            }
            core.implInit(keySize, random);
        }
        protected SecretKey engineGenerateKey() {
            return core.implGenerateKey();
        }
    }
}
