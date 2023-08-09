public class SSLParameters {
    private String[] cipherSuites;
    private String[] protocols;
    private boolean wantClientAuth;
    private boolean needClientAuth;
    private String identificationAlgorithm;
    private AlgorithmConstraints algorithmConstraints;
    public SSLParameters() {
    }
    public SSLParameters(String[] cipherSuites) {
        setCipherSuites(cipherSuites);
    }
    public SSLParameters(String[] cipherSuites, String[] protocols) {
        setCipherSuites(cipherSuites);
        setProtocols(protocols);
    }
    private static String[] clone(String[] s) {
        return (s == null) ? null : s.clone();
    }
    public String[] getCipherSuites() {
        return clone(cipherSuites);
    }
    public void setCipherSuites(String[] cipherSuites) {
        this.cipherSuites = clone(cipherSuites);
    }
    public String[] getProtocols() {
        return clone(protocols);
    }
    public void setProtocols(String[] protocols) {
        this.protocols = clone(protocols);
    }
    public boolean getWantClientAuth() {
        return wantClientAuth;
    }
    public void setWantClientAuth(boolean wantClientAuth) {
        this.wantClientAuth = wantClientAuth;
        this.needClientAuth = false;
    }
    public boolean getNeedClientAuth() {
        return needClientAuth;
    }
    public void setNeedClientAuth(boolean needClientAuth) {
        this.wantClientAuth = false;
        this.needClientAuth = needClientAuth;
    }
    public AlgorithmConstraints getAlgorithmConstraints() {
        return algorithmConstraints;
    }
    public void setAlgorithmConstraints(AlgorithmConstraints constraints) {
        this.algorithmConstraints = constraints;
    }
    public String getEndpointIdentificationAlgorithm() {
        return identificationAlgorithm;
    }
    public void setEndpointIdentificationAlgorithm(String algorithm) {
        this.identificationAlgorithm = algorithm;
    }
}
