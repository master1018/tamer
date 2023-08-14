public class TlsRsaPremasterSecretParameterSpec
        implements AlgorithmParameterSpec {
    private final int majorVersion;
    private final int minorVersion;
    public TlsRsaPremasterSecretParameterSpec(int majorVersion,
            int minorVersion) {
        this.majorVersion =
            TlsMasterSecretParameterSpec.checkVersion(majorVersion);
        this.minorVersion =
            TlsMasterSecretParameterSpec.checkVersion(minorVersion); }
    public int getMajorVersion() {
        return majorVersion;
    }
    public int getMinorVersion() {
        return minorVersion;
    }
}
