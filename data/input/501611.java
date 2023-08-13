public abstract class KeyGeneratorSpi {
    public KeyGeneratorSpi() {
    }
    protected abstract SecretKey engineGenerateKey();
    protected abstract void engineInit(AlgorithmParameterSpec params,
            SecureRandom random) throws InvalidAlgorithmParameterException;
    protected abstract void engineInit(int keysize, SecureRandom random);
    protected abstract void engineInit(SecureRandom random);
}