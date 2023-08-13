public class MyAlgorithmParameterGeneratorSpi 
        extends AlgorithmParameterGeneratorSpi {
    public void engineInit(int keysize, SecureRandom random) {
        if (keysize < 0) {
            throw new IllegalArgumentException("keysize < 0");
        }
    }
    public void engineInit(AlgorithmParameterSpec genParamSpec,
            SecureRandom random) {
        if (random == null) {
            throw new IllegalArgumentException("random is null");
        }
    }
    public AlgorithmParameters engineGenerateParameters() {
        return null;
    }
}