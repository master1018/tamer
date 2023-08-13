public class PutAllAction implements PrivilegedAction<Void> {
    private final Provider provider;
    private final Map map;
    public PutAllAction(Provider provider, Map map) {
        this.provider = provider;
        this.map = map;
    }
    public Void run() {
        provider.putAll(map);
        return null;
    }
}
