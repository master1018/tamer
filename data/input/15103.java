public abstract class KeyAgreementSpi {
    protected abstract void engineInit(Key key, SecureRandom random)
        throws InvalidKeyException;
    protected abstract void engineInit(Key key, AlgorithmParameterSpec params,
                                       SecureRandom random)
        throws InvalidKeyException, InvalidAlgorithmParameterException;
    protected abstract Key engineDoPhase(Key key, boolean lastPhase)
        throws InvalidKeyException, IllegalStateException;
    protected abstract byte[] engineGenerateSecret()
        throws IllegalStateException;
    protected abstract int engineGenerateSecret(byte[] sharedSecret,
                                                int offset)
        throws IllegalStateException, ShortBufferException;
    protected abstract SecretKey engineGenerateSecret(String algorithm)
        throws IllegalStateException, NoSuchAlgorithmException,
            InvalidKeyException;
}
