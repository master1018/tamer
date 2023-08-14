public abstract class KeyPairGeneratorSpi {
    public KeyPairGeneratorSpi() {
    }
    public abstract KeyPair generateKeyPair();
    public abstract void initialize(int keysize, SecureRandom random);
    public void initialize(AlgorithmParameterSpec params, SecureRandom random)
            throws InvalidAlgorithmParameterException {
        throw new UnsupportedOperationException(Messages.getString("security.2E")); 
    }
}
