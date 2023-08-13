public class X509PublicKey implements PublicKey {
    private final String algorithm;
    private final byte[] encoded;
    private final byte[] keyBytes;
    public X509PublicKey(String algorithm, byte[] encoded, byte[] keyBytes) {
        this.algorithm = algorithm;
        this.encoded = encoded;
        this.keyBytes = keyBytes;
    }
    public String getAlgorithm() {
        return algorithm;
    }
    public String getFormat() {
        return "X.509"; 
    }
    public byte[] getEncoded() {
        return encoded;
    }
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder("algorithm = "); 
        buf.append(algorithm);
        buf.append(", params unparsed, unparsed keybits = \n"); 
        return buf.toString();
    }
}
