public class MGF1ParameterSpec implements AlgorithmParameterSpec {
    public static final MGF1ParameterSpec SHA1 =
        new MGF1ParameterSpec("SHA-1"); 
    public static final MGF1ParameterSpec SHA256 =
        new MGF1ParameterSpec("SHA-256"); 
    public static final MGF1ParameterSpec SHA384 =
        new MGF1ParameterSpec("SHA-384"); 
    public static final MGF1ParameterSpec SHA512 =
        new MGF1ParameterSpec("SHA-512"); 
    private final String mdName;
    public MGF1ParameterSpec(String mdName) {
        this.mdName = mdName;
        if (this.mdName == null) {
            throw new NullPointerException(Messages.getString("security.80")); 
        }
    }
    public String getDigestAlgorithm() {
        return mdName;
    }
}
