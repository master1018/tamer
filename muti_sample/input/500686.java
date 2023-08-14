public class DHPrivateKeySpec implements KeySpec {
    private final BigInteger x;
    private final BigInteger p;
    private final BigInteger g;
    public DHPrivateKeySpec(BigInteger x, BigInteger p, BigInteger g) {
        this.x = x;
        this.p = p;
        this.g = g;
    }
    public BigInteger getX() {
        return x;
    }
    public BigInteger getP() {
        return p;
    }
    public BigInteger getG() {
        return g;
    }
}
