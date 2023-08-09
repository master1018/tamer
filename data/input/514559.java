public class OAEPParameterSpec implements AlgorithmParameterSpec {
    private final String mdName;
    private final String mgfName;
    private final AlgorithmParameterSpec mgfSpec;
    private final PSource pSrc;
    public static final OAEPParameterSpec DEFAULT = new OAEPParameterSpec();
    private OAEPParameterSpec() {
        this.mdName = "SHA-1"; 
        this.mgfName = "MGF1"; 
        this.mgfSpec = MGF1ParameterSpec.SHA1;
        this.pSrc = PSource.PSpecified.DEFAULT;
    }
    public OAEPParameterSpec(String mdName, String mgfName,
                                AlgorithmParameterSpec mgfSpec, PSource pSrc) {
        if ((mdName == null) || (mgfName == null) || (pSrc == null)) {
            throw new NullPointerException();
        }
        this.mdName = mdName;
        this.mgfName = mgfName;
        this.mgfSpec = mgfSpec;
        this.pSrc = pSrc;
    }
    public String getDigestAlgorithm() {
        return mdName;
    }
    public String getMGFAlgorithm() {
        return mgfName;
    }
    public AlgorithmParameterSpec getMGFParameters() {
        return mgfSpec;
    }
    public PSource getPSource() {
        return pSrc;
    }
}
