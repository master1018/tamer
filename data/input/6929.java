public final class PersistenceDelegateFinder
        extends InstanceFinder<PersistenceDelegate> {
    private final Map<Class<?>, PersistenceDelegate> registry;
    public PersistenceDelegateFinder() {
        super(PersistenceDelegate.class, true, "PersistenceDelegate");
        this.registry = new HashMap<Class<?>, PersistenceDelegate>();
    }
    public void register(Class<?> type, PersistenceDelegate delegate) {
        synchronized (this.registry) {
            if (delegate != null) {
                this.registry.put(type, delegate);
            }
            else {
                this.registry.remove(type);
            }
        }
    }
    @Override
    public PersistenceDelegate find(Class<?> type) {
        PersistenceDelegate delegate;
        synchronized (this.registry) {
            delegate = this.registry.get(type);
        }
        return (delegate != null) ? delegate : super.find(type);
    }
}
