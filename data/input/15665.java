public abstract class ExemptionMechanismSpi {
    protected abstract int engineGetOutputSize(int inputLen);
    protected abstract void engineInit(Key key)
    throws InvalidKeyException, ExemptionMechanismException;
    protected abstract void engineInit(Key key, AlgorithmParameterSpec params)
    throws InvalidKeyException, InvalidAlgorithmParameterException,
    ExemptionMechanismException;
    protected abstract void engineInit(Key key, AlgorithmParameters params)
    throws InvalidKeyException, InvalidAlgorithmParameterException,
    ExemptionMechanismException;
    protected abstract byte[] engineGenExemptionBlob()
        throws ExemptionMechanismException;
    protected abstract int engineGenExemptionBlob
    (byte[] output, int outputOffset)
        throws ShortBufferException, ExemptionMechanismException;
}
