public class ProviderLoader {
    public static void go(final String config) throws Exception {
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                SunPKCS11 provider = new SunPKCS11(config);
                Security.addProvider(provider);
                return null;
            }
        });
    }
}
