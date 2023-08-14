public class MyKeyPairGenerator1 extends KeyPairGenerator {
    public int keySize;
    public SecureRandom secureRandom;
    public AlgorithmParameterSpec paramSpec;
    public MyKeyPairGenerator1() {
        super("MyKeyPairGenerator1");
    }
    public MyKeyPairGenerator1(String pp) {
        super(pp);
    }
    public String getAlgorithm() {
        return "MyKeyPairGenerator1";
    }
    public static final String getResAlgorithm() {
        return "MyKeyPairGenerator1";
    }
    public void initialize(int keysize, SecureRandom random) {
        if ((keysize < 0) || ((keysize % 100) != 0)) {
            throw new InvalidParameterException("Incorrect keysize parameter");
        }
        if (random == null) {
            throw new InvalidParameterException("Incorrect random");
        }
        keySize = keysize;
        secureRandom = random;
    }
    public KeyPair generateKeyPair() {
        try {
            return new KeyPair(new PubKey(), new PrivKey());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public void initialize(AlgorithmParameterSpec param, SecureRandom random)
            throws InvalidAlgorithmParameterException {
        if (random == null) {
            throw new InvalidParameterException("Incorrect random");
        }
        if (param == null) {
            throw new InvalidAlgorithmParameterException("Incorrect param");
        }
        paramSpec = param;
        secureRandom = random;
    }
    public class PubKey implements PublicKey {
        private String algorithm;
        private String format;
        private byte[] encoded;
        public PubKey() {
            this.algorithm = "MyKeyPairGenerator1";
            this.format = "test1";
            this.encoded = new byte[10];
        }
        public String getAlgorithm() {
            return algorithm;
        }
        public String getFormat() {
            return format;
        }
        public byte[] getEncoded() {
            return encoded;
        }
    }
    public class PrivKey implements PrivateKey {
        private String algorithm;
        private String format;
        private byte[] encoded;
        public PrivKey() {
            this.algorithm = "MyKeyPairGenerator1";
            this.format = "test1";
            this.encoded = new byte[10];
        }
        public String getAlgorithm() {
            return algorithm;
        }
        public String getFormat() {
            return format;
        }
        public byte[] getEncoded() {
            return encoded;
        }
    }
}