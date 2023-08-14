public class StubProviderImpl extends CertPathBuilderSpi {
    public boolean called;
    public StubProviderImpl() {
        super();
        called = false;
    }
    public CertPathBuilderResult engineBuild(CertPathParameters params) {
        called = true;
        return null;
    }
}
