public class DHParametersGenerator {
    private int size;
    private int certainty;
    private RandomGenerator random;
    private static final BigIntegerCrypto TWO = BigIntegerCrypto.valueOf(2);
    public void init(int size, int certainty, RandomGenerator random) {
        this.size = size;
        this.certainty = certainty;
        this.random = random;
    }
    public DHParameters generateParameters() {
        BigIntegerCrypto[] safePrimes = DHParametersHelper.generateSafePrimes(size, certainty, random);
        BigIntegerCrypto p = safePrimes[0];
        BigIntegerCrypto q = safePrimes[1];
        BigIntegerCrypto g = DHParametersHelper.selectGenerator(p, q, random);
        return new DHParameters(p, g, q, TWO, null);
    }
}
