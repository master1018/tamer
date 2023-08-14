public class DHGenParameterSpec implements AlgorithmParameterSpec {
    private int primeSize;
    private int exponentSize;
    public DHGenParameterSpec(int primeSize, int exponentSize) {
        this.primeSize = primeSize;
        this.exponentSize = exponentSize;
    }
    public int getPrimeSize() {
        return this.primeSize;
    }
    public int getExponentSize() {
        return this.exponentSize;
    }
}
