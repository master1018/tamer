public class MyKeyPairGeneratorSpi extends KeyPairGeneratorSpi {
    public void initialize(int keysize, SecureRandom random) {
        if (keysize < 100) {
            throw new InvalidParameterException(
                    "Invalid keysize: less than 100");
        }
        if (random == null) {
            throw new IllegalArgumentException("Invalid random");
        }
    }
    public KeyPair generateKeyPair() {
        return null;
    }
    public void initialize(AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidAlgorithmParameterException {
        if (random == null) {
            throw new UnsupportedOperationException(
                    "Not supported for null random");
        }
    }
}