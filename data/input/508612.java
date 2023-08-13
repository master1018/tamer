public class MyKeyGeneratorSpi  extends KeyGeneratorSpi {
    @Override
    protected SecretKey engineGenerateKey() {
        return null;
    }
    @Override
    protected void engineInit(AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidAlgorithmParameterException {
        if (params == null) {
            throw new InvalidAlgorithmParameterException("params is null");
        }
    }
    @Override
    protected void engineInit(int keysize, SecureRandom random) {
        if (keysize <= 77) {
            throw new IllegalArgumentException("Invalid keysize");
        }
    }
    @Override
    protected void engineInit(SecureRandom random) {
        throw new IllegalArgumentException("Invalid random");
    }
}
