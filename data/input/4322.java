public class RemoveProviders {
    public static void main(String[] args) throws Exception {
        Provider[] providers = Security.getProviders();
        System.out.println("Providers: " + Arrays.asList(providers));
        for (int i = 0; i < providers.length; i++) {
            Provider p = providers[i];
            String name = p.getName();
            Security.removeProvider(name);
            if (Security.getProvider(name) != null) {
                throw new Exception("Provider not removed: " + name);
            }
            Security.addProvider(p);
        }
        if (Arrays.equals(providers, Security.getProviders()) == false) {
            throw new Exception("Provider mismatch: " + Arrays.asList(Security.getProviders()));
        }
        Security.removeProvider("foo");
        if (Arrays.equals(providers, Security.getProviders()) == false) {
            throw new Exception("Provider mismatch: " + Arrays.asList(Security.getProviders()));
        }
        Security.removeProvider("SunJCE");
        if (Security.getProvider("SunJCE") != null) {
            throw new Exception("not removed");
        }
        System.out.println("Done.");
    }
}
