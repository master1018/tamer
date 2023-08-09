public abstract class AlgorithmParameterGeneratorSpi {
    protected abstract void engineInit(int size, SecureRandom random);
    protected abstract void engineInit(AlgorithmParameterSpec genParamSpec,
                                       SecureRandom random)
        throws InvalidAlgorithmParameterException;
    protected abstract AlgorithmParameters engineGenerateParameters();
}
