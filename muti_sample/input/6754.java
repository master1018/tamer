public class MyProvider extends Provider {
    public MyProvider() {
        super("MyProvider", 1.0, "CertImpl");
        put("CertificateFactory.X.509", "MyCertificateFactory");
    }
}
