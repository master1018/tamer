public abstract class KeyPairGeneratorSpi {
    public abstract void initialize(int keysize, SecureRandom random);
    public void initialize(AlgorithmParameterSpec params,
                           SecureRandom random)
        throws InvalidAlgorithmParameterException {
            throw new UnsupportedOperationException();
    }
    public abstract KeyPair generateKeyPair();
}
