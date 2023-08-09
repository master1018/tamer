public class TlsKeyMaterialSpec implements KeySpec, SecretKey {
    static final long serialVersionUID = 812912859129525028L;
    private final SecretKey clientMacKey, serverMacKey;
    private final SecretKey clientCipherKey, serverCipherKey;
    private final IvParameterSpec clientIv, serverIv;
    public TlsKeyMaterialSpec(SecretKey clientMacKey, SecretKey serverMacKey) {
        this(clientMacKey, serverMacKey, null, null, null, null);
    }
    public TlsKeyMaterialSpec(SecretKey clientMacKey, SecretKey serverMacKey,
            SecretKey clientCipherKey, SecretKey serverCipherKey) {
        this(clientMacKey, serverMacKey, clientCipherKey, null,
            serverCipherKey, null);
    }
    public TlsKeyMaterialSpec(SecretKey clientMacKey, SecretKey serverMacKey,
            SecretKey clientCipherKey, IvParameterSpec clientIv,
            SecretKey serverCipherKey, IvParameterSpec serverIv) {
        if ((clientMacKey == null) || (serverMacKey == null)) {
            throw new NullPointerException("MAC keys must not be null");
        }
        this.clientMacKey = clientMacKey;
        this.serverMacKey = serverMacKey;
        this.clientCipherKey = clientCipherKey;
        this.serverCipherKey = serverCipherKey;
        this.clientIv = clientIv;
        this.serverIv = serverIv;
    }
    public String getAlgorithm() {
        return "TlsKeyMaterial";
    }
    public String getFormat() {
        return null;
    }
    public byte[] getEncoded() {
        return null;
    }
    public SecretKey getClientMacKey() {
        return clientMacKey;
    }
    public SecretKey getServerMacKey() {
        return serverMacKey;
    }
    public SecretKey getClientCipherKey() {
        return clientCipherKey;
    }
    public IvParameterSpec getClientIv() {
        return clientIv;
    }
    public SecretKey getServerCipherKey() {
        return serverCipherKey;
    }
    public IvParameterSpec getServerIv() {
        return serverIv;
    }
}
