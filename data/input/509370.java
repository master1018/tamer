public class MyKeyPairGenerator2 extends KeyPairGenerator {
    int keySize;
    SecureRandom secureRandom;
    public MyKeyPairGenerator2() {
        super("MyKeyPairGenerator2");
    }
    public String getAlgorithm() {
        return "MyKeyPairGenerator2";
    }
    public static final String getResAlgorithm() {
        return "MyKeyPairGenerator2";
    }
    public MyKeyPairGenerator2(String pp) {
        super(pp);
    }
    public void initialize(int keysize, SecureRandom random) {
        if (keysize < 64) {
            throw new InvalidParameterException("Incorrect keysize parameter");
        }
        keySize = keysize;
        secureRandom = random;
    }
    public KeyPair generateKeyPair() {
        return null;
    }
}
