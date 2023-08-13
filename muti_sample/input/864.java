public class StubProvider extends Provider {
    public StubProvider() {
        super( "StubProvider", 1.1, "No Info");
        put("CertPathBuilder.PKIX", "StubProviderImpl");
    }
}
