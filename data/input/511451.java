public class DSAPublicKeySpec implements KeySpec {
    private final BigInteger y;
    private final BigInteger p;
    private final BigInteger q;
    private final BigInteger g;
    public DSAPublicKeySpec(BigInteger y, BigInteger p,
            BigInteger q, BigInteger g) {
        this.y = y;
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
    public BigInteger getY() {
        return y;
    }
}
