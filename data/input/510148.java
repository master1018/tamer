public class RSAKeyGenParameterSpec implements AlgorithmParameterSpec {    
    public static final BigInteger F0 = BigInteger.valueOf(3L);
    public static final BigInteger F4 = BigInteger.valueOf(65537L);
    private final int keysize;
    private final BigInteger publicExponent;
    public RSAKeyGenParameterSpec(int keysize, BigInteger publicExponent) {
        this.keysize = keysize;
        this.publicExponent = publicExponent;
    }
    public int getKeysize() {
        return keysize;
    }
    public BigInteger getPublicExponent() {
        return publicExponent;
    }
}
