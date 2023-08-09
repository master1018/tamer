public class PrivateKeyStub implements PrivateKey {
    private static final long serialVersionUID = 111111111L;
    String algorithm = null;
    String format = null;
    byte [] encoded = null;
    public PrivateKeyStub(String algorithm, String format, byte[] encoded) {
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
