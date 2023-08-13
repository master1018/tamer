public class DHParameterSpec implements AlgorithmParameterSpec {
    private final BigInteger p;
    private final BigInteger g;
    private final int l;
    public DHParameterSpec(BigInteger p, BigInteger g) {
        this.p = p;
        this.g = g;
        this.l = 0;
    }
    public DHParameterSpec(BigInteger p, BigInteger g, int l) {
        this.p = p;
        this.g = g;
        this.l = l;
    }
    public BigInteger getP() {
        return p;
    }
    public BigInteger getG() {
        return g;
    }
    public int getL() {
        return l;
    }
}
