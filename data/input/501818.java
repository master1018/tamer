public class CertPathTrustManagerParameters implements ManagerFactoryParameters {
    private final CertPathParameters param;
    public CertPathTrustManagerParameters(CertPathParameters parameters) {
        param = (CertPathParameters) parameters.clone();
    }
    public CertPathParameters getParameters() {
        return (CertPathParameters) param.clone();
    }
}
