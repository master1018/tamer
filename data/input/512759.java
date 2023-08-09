public class DSAKeyPairGeneratorImpl implements DSAKeyPairGenerator {
    private KeyPairGenerator dsaKeyPairGenerator = null;
    private SecureRandom secureRandom = null;
    private DSAParams dsaParams = null;
    private int lengthModulus = 0;
    public DSAKeyPairGeneratorImpl(DSAParams dsap) {
        dsaKeyPairGenerator = null;
        try {
            dsaKeyPairGenerator = KeyPairGenerator.getInstance("DSA");
        } catch (Exception e) {
            dsaKeyPairGenerator = null;
        }
        dsaParams = dsap;
    }
    public void initialize(DSAParams params, SecureRandom random)
                           throws InvalidParameterException {
        if (random == null) {
            throw new InvalidParameterException("Incorrect random");
        }
        if (params == null) {
            throw new InvalidParameterException("Incorrect params");
        }
        secureRandom = random;
        dsaParams = params;
    }
    public void initialize(int modlen, boolean genParams, SecureRandom random)
                           throws InvalidParameterException {
        int len = 512;
        while (len <= 1024) {
            if (len == modlen) {
                lengthModulus = modlen;
                break;
            } else {
                len = len + 8;
                if (len == 1032) {
                    throw new InvalidParameterException("Incorrect modlen");
                }
            }
        }
        if (modlen < 512 || modlen > 1024) {
            throw new InvalidParameterException("Incorrect modlen");
        }
        if (random == null) {
            throw new InvalidParameterException("Incorrect random");
        }
        if (genParams == false && dsaParams == null) {
            throw new InvalidParameterException("there are not precomputed parameters");
        }
        secureRandom = random;
    }
}
