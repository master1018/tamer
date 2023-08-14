public class DHGenParameterSpec implements AlgorithmParameterSpec {
    private final int primeSize;
    private final int exponentSize;
    public DHGenParameterSpec(int primeSize, int exponentSize) {
        this.primeSize = primeSize;
        this.exponentSize = exponentSize;
    }
    public int getPrimeSize() {
        return primeSize;
    }
    public int getExponentSize() {
        return exponentSize;
    }
}
