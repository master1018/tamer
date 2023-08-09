public class OAEPParameterSpec implements AlgorithmParameterSpec {
    private String mdName = "SHA-1";
    private String mgfName = "MGF1";
    private AlgorithmParameterSpec mgfSpec = MGF1ParameterSpec.SHA1;
    private PSource pSrc = PSource.PSpecified.DEFAULT;
    public static final OAEPParameterSpec DEFAULT = new OAEPParameterSpec();
    private OAEPParameterSpec() {
    }
    public OAEPParameterSpec(String mdName, String mgfName,
                             AlgorithmParameterSpec mgfSpec,
                             PSource pSrc) {
        if (mdName == null) {
            throw new NullPointerException("digest algorithm is null");
        }
        if (mgfName == null) {
            throw new NullPointerException("mask generation function " +
                                           "algorithm is null");
        }
        if (pSrc == null) {
            throw new NullPointerException("source of the encoding input " +
                                           "is null");
        }
        this.mdName =  mdName;
        this.mgfName =  mgfName;
        this.mgfSpec =  mgfSpec;
        this.pSrc =  pSrc;
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
