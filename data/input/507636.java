public class DSAPrivateKeySpec implements KeySpec {
    private final BigInteger x;
    private final BigInteger p;
     private final BigInteger q;
    private final BigInteger g;
    public DSAPrivateKeySpec(BigInteger x, BigInteger p,
            BigInteger q, BigInteger g) {
        this.x = x;
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
    public BigInteger getX() {
        return x;
    }
}
