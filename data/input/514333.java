public class BasicPoolEntryRef extends WeakReference<BasicPoolEntry> {
    private final HttpRoute route;
    public BasicPoolEntryRef(BasicPoolEntry entry,
                             ReferenceQueue<Object> queue) {
        super(entry, queue);
        if (entry == null) {
            throw new IllegalArgumentException
                ("Pool entry must not be null.");
        }
        route = entry.getPlannedRoute();
    }
    public final HttpRoute getRoute() {
        return this.route;
    }
} 
