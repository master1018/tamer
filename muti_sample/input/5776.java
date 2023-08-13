public class CertPathTrustManagerParameters implements ManagerFactoryParameters {
    private final CertPathParameters parameters;
    public CertPathTrustManagerParameters(CertPathParameters parameters) {
        this.parameters = (CertPathParameters)parameters.clone();
    }
    public CertPathParameters getParameters() {
        return (CertPathParameters)parameters.clone();
    }
}
