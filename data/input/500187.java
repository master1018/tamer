public class PublicKeyImpl implements PublicKey {
    private static final long serialVersionUID = 7179022516819534075L;
    private byte[] encoding;
    private String algorithm;
    public PublicKeyImpl(String algorithm) { 
        this.algorithm = algorithm;
    }
    public String getAlgorithm() {
        return algorithm;
    }
    public String getFormat() {
        return "X.509"; 
    }
    public byte[] getEncoded() {
        byte[] result = new byte[encoding.length];
        System.arraycopy(encoding, 0, result, 0, encoding.length);
        return result;
    }
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }
    public void setEncoding(byte[] encoding) {
        this.encoding = new byte[encoding.length];
        System.arraycopy(encoding, 0, this.encoding, 0, encoding.length);
    }
}
