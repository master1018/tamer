public abstract class ResolverConfiguration {
    private static final Object lock = new Object();
    private static ResolverConfiguration provider;
    protected ResolverConfiguration() { }
    public static ResolverConfiguration open() {
        synchronized (lock) {
            if (provider == null) {
                provider = new sun.net.dns.ResolverConfigurationImpl();
            }
            return provider;
        }
    }
    public abstract List<String> searchlist();
    public abstract List<String> nameservers();
    public static abstract class Options {
        public int attempts() {
            return -1;
        }
        public int retrans() {
            return -1;
        }
    }
    public abstract Options options();
}
