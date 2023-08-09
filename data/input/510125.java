public class DHPublicKeySpec implements KeySpec {
    private final BigInteger y;
    private final BigInteger p;
    private final BigInteger g;
    public DHPublicKeySpec(BigInteger y, BigInteger p, BigInteger g) {
        this.y = y;
        this.p = p;
        this.g = g;
    }
    public BigInteger getY() {
        return y;
    }
    public BigInteger getP() {
        return p;
    }
    public BigInteger getG() {
        return g;
    }
}
