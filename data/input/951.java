public class DHPublicKeySpec implements java.security.spec.KeySpec {
    private BigInteger y;
    private BigInteger p;
    private BigInteger g;
    public DHPublicKeySpec(BigInteger y, BigInteger p, BigInteger g) {
        this.y = y;
        this.p = p;
        this.g = g;
    }
    public BigInteger getY() {
        return this.y;
    }
    public BigInteger getP() {
        return this.p;
    }
    public BigInteger getG() {
        return this.g;
    }
}
