public class PublicKeyStub implements PublicKey {
    private static final long serialVersionUID = 333333333L;
    String algorithm = null;
    String format = null;
    byte [] encoded = null;
    public PublicKeyStub(String algorithm, String format, byte[] encoded) {
        this.algorithm = algorithm;
        this.format = format;
        this.encoded = encoded;
    }
    public String getAlgorithm() {
        return algorithm;
    }
    public String getFormat() {
        return format;
    }
    public byte[] getEncoded() {
        return encoded;
    }
}