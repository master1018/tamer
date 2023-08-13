public class MyKeyAgreementSpi extends KeyAgreementSpi {
    @Override
    protected Key engineDoPhase(Key key, boolean lastPhase)
            throws InvalidKeyException, IllegalStateException {
        if (!lastPhase) {
            throw new IllegalStateException("last Phase is false");
        }
        return null;
    }
    @Override
    protected byte[] engineGenerateSecret() throws IllegalStateException {
        return new byte[0];
    }
    @Override
    protected int engineGenerateSecret(byte[] sharedSecret, int offset)
            throws IllegalStateException, ShortBufferException {
        return -1;
    }
    @Override
    protected SecretKey engineGenerateSecret(String algorithm)
            throws IllegalStateException, NoSuchAlgorithmException,
            InvalidKeyException {
        if (algorithm.length() == 0) {
            throw new NoSuchAlgorithmException("Algorithm is empty");
        }
        return null;
    }
    @Override
    protected void engineInit(Key key, SecureRandom random)
            throws InvalidKeyException {
        throw new IllegalArgumentException("Invalid parameter");
    }
    @Override
    protected void engineInit(Key key, AlgorithmParameterSpec params,
            SecureRandom random) throws InvalidKeyException,
            InvalidAlgorithmParameterException {
        throw new IllegalArgumentException("Invalid parameter");
    }
}
