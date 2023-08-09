public class RemoveProviderByIdentity {
    public static void main(String[] args) throws Exception {
        String PROVIDER_NAME = "myprovider";
        Security.addProvider(new MyProvider(PROVIDER_NAME, 1, "test"));
        if (Security.getProvider(PROVIDER_NAME) == null)
            throw new Exception("provider not registered");
        Security.removeProvider(PROVIDER_NAME);
        if (Security.getProvider(PROVIDER_NAME) != null)
            throw new Exception("provider not removed");
    }
}
class MyProvider extends Provider {
    public MyProvider(String name, double version, String info) {
        super(name, version, info);
        put("Signature.sigalg", "sigimpl");
    }
}
