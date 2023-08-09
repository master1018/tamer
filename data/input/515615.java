public class RSAPrivateKeySpec implements KeySpec {    
    private final BigInteger modulus;
    private final BigInteger privateExponent;
    public RSAPrivateKeySpec(BigInteger modulus, BigInteger privateExponent) {
        this.modulus = modulus;
        this.privateExponent = privateExponent;
    }
    public BigInteger getModulus() {
        return modulus;
    }
    public BigInteger getPrivateExponent() {
        return privateExponent;
    }
}
