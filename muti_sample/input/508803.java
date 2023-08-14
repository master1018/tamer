public class RSAPublicKeySpec implements KeySpec {
    private final BigInteger modulus;
    private final BigInteger publicExponent;
    public RSAPublicKeySpec(BigInteger modulus, BigInteger publicExponent) {
        this.modulus = modulus;
        this.publicExponent = publicExponent;
    }
    public BigInteger getModulus() {
        return modulus;
    }
    public BigInteger getPublicExponent() {
        return publicExponent;
    }
}
