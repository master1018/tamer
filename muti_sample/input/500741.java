public class DSAParameterSpec implements AlgorithmParameterSpec, DSAParams {
    private final BigInteger p;
    private final BigInteger q;
    private final BigInteger g;
    public DSAParameterSpec(BigInteger p, BigInteger q, BigInteger g) {
        this.p = p;
        this.q = q;
        this.g = g;
    }
    public BigInteger getG() {
        return g;
    }
    public BigInteger getP() {
        return p;
    }
    public BigInteger getQ() {
        return q;
    }
}
