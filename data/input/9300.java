public class ChangeProviders extends Provider {
    private ChangeProviders() {
        super("Foo", 47.23d, "none");
    }
    private static int plen() {
        return Security.getProviders().length;
    }
    public static void main(String[] args) throws Exception {
        long start = System.currentTimeMillis();
        Provider p = new ChangeProviders();
        int n = plen();
        Security.addProvider(p);
        if (plen() != n + 1) {
            throw new Exception("Provider not added");
        }
        Security.addProvider(p);
        if (plen() != n + 1) {
            throw new Exception("Provider readded");
        }
        Security.insertProviderAt(p, 1);
        if (plen() != n + 1) {
            throw new Exception("Provider readded");
        }
        Security.removeProvider(p.getName());
        if ((plen() != n) || (Security.getProvider(p.getName()) != null)) {
            throw new Exception("Provider not removed");
        }
        Security.insertProviderAt(p, 1);
        if (plen() != n + 1) {
            throw new Exception("Provider not added");
        }
        if (Security.getProviders()[0] != p) {
            throw new Exception("Provider not at pos 1");
        }
        System.out.println("All tests passed.");
    }
}
