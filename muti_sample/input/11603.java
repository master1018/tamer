public class TlsKeyMaterialParameterSpec implements AlgorithmParameterSpec {
    private final SecretKey masterSecret;
    private final int majorVersion, minorVersion;
    private final byte[] clientRandom, serverRandom;
    private final String cipherAlgorithm;
    private final int cipherKeyLength, ivLength, macKeyLength;
    private final int expandedCipherKeyLength; 
    private final String prfHashAlg;
    private final int prfHashLength;
    private final int prfBlockSize;
    public TlsKeyMaterialParameterSpec(SecretKey masterSecret,
            int majorVersion, int minorVersion, byte[] clientRandom,
            byte[] serverRandom, String cipherAlgorithm, int cipherKeyLength,
            int expandedCipherKeyLength, int ivLength, int macKeyLength,
            String prfHashAlg, int prfHashLength, int prfBlockSize) {
        if (masterSecret.getAlgorithm().equals("TlsMasterSecret") == false) {
            throw new IllegalArgumentException("Not a TLS master secret");
        }
        if (cipherAlgorithm == null) {
            throw new NullPointerException();
        }
        this.masterSecret = masterSecret;
        this.majorVersion =
            TlsMasterSecretParameterSpec.checkVersion(majorVersion);
        this.minorVersion =
            TlsMasterSecretParameterSpec.checkVersion(minorVersion);
        this.clientRandom = clientRandom.clone();
        this.serverRandom = serverRandom.clone();
        this.cipherAlgorithm = cipherAlgorithm;
        this.cipherKeyLength = checkSign(cipherKeyLength);
        this.expandedCipherKeyLength = checkSign(expandedCipherKeyLength);
        this.ivLength = checkSign(ivLength);
        this.macKeyLength = checkSign(macKeyLength);
        this.prfHashAlg = prfHashAlg;
        this.prfHashLength = prfHashLength;
        this.prfBlockSize = prfBlockSize;
    }
    private static int checkSign(int k) {
        if (k < 0) {
            throw new IllegalArgumentException("Value must not be negative");
        }
        return k;
    }
    public SecretKey getMasterSecret() {
        return masterSecret;
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
    public String getCipherAlgorithm() {
        return cipherAlgorithm;
    }
    public int getCipherKeyLength() {
        return cipherKeyLength;
    }
    public int getExpandedCipherKeyLength() {
        if (majorVersion >= 0x03 && minorVersion >= 0x02) {
            return 0;
        }
        return expandedCipherKeyLength;
    }
    public int getIvLength() {
        if (majorVersion >= 0x03 && minorVersion >= 0x02) {
            return 0;
        }
        return ivLength;
    }
    public int getMacKeyLength() {
        return macKeyLength;
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
