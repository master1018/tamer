public class TlsMasterSecretParameterSpec implements AlgorithmParameterSpec {
    private final SecretKey premasterSecret;
    private final int majorVersion, minorVersion;
    private final byte[] clientRandom, serverRandom;
    private final String prfHashAlg;
    private final int prfHashLength;
    private final int prfBlockSize;
    public TlsMasterSecretParameterSpec(SecretKey premasterSecret,
            int majorVersion, int minorVersion,
            byte[] clientRandom, byte[] serverRandom,
            String prfHashAlg, int prfHashLength, int prfBlockSize) {
        if (premasterSecret == null) {
            throw new NullPointerException("premasterSecret must not be null");
        }
        this.premasterSecret = premasterSecret;
        this.majorVersion = checkVersion(majorVersion);
        this.minorVersion = checkVersion(minorVersion);
        this.clientRandom = clientRandom.clone();
        this.serverRandom = serverRandom.clone();
        this.prfHashAlg = prfHashAlg;
        this.prfHashLength = prfHashLength;
        this.prfBlockSize = prfBlockSize;
    }
    static int checkVersion(int version) {
        if ((version < 0) || (version > 255)) {
            throw new IllegalArgumentException(
                        "Version must be between 0 and 255");
        }
        return version;
    }
    public SecretKey getPremasterSecret() {
        return premasterSecret;
    }
    public int getMajorVersion() {
        return majorVersion;
    }
    public int getMinorVersion() {
        return minorVersion;
    }
    public byte[] getClientRandom() {
        return clientRandom.clone();
    }
    public byte[] getServerRandom() {
        return serverRandom.clone();
    }
    public String getPRFHashAlg() {
        return prfHashAlg;
    }
    public int getPRFHashLength() {
        return prfHashLength;
    }
    public int getPRFBlockSize() {
        return prfBlockSize;
    }
}
