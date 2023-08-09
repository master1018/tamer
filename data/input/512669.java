public class PSSParameterSpec implements AlgorithmParameterSpec {   
    public static final PSSParameterSpec DEFAULT = new PSSParameterSpec(20);
    private final String mdName;
    private final String mgfName;
    private final AlgorithmParameterSpec mgfSpec;
    private final int trailerField;
    private final int saltLen;
    public PSSParameterSpec(int saltLen) {
        if (saltLen < 0) {
            throw new IllegalArgumentException(Messages.getString("security.7F")); 
        }
        this.saltLen = saltLen;
        this.mdName = "SHA-1"; 
        this.mgfName = "MGF1"; 
        this.mgfSpec = MGF1ParameterSpec.SHA1;
        this.trailerField = 1;
    }
    public PSSParameterSpec(String mdName, String mgfName,
            AlgorithmParameterSpec mgfSpec, int saltLen, int trailerField) {
        if (mdName == null) {
            throw new NullPointerException(Messages.getString("security.80")); 
        }
        if (mgfName == null) {
            throw new NullPointerException(Messages.getString("security.81")); 
        }
        if (saltLen < 0) {
            throw new IllegalArgumentException(Messages.getString("security.7F")); 
        }
        if (trailerField < 0) {
            throw new IllegalArgumentException(Messages.getString("security.82")); 
        }
        this.mdName = mdName;
        this.mgfName = mgfName;
        this.mgfSpec = mgfSpec;
        this.saltLen = saltLen;
        this.trailerField = trailerField;
    }
    public int getSaltLength() {
        return saltLen;
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
    public int getTrailerField() {
        return trailerField;
    }
}
