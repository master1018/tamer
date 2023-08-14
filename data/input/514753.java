public class BasicPoolEntry extends AbstractPoolEntry {
    private final BasicPoolEntryRef reference;
    public BasicPoolEntry(ClientConnectionOperator op,
                          HttpRoute route,
                          ReferenceQueue<Object> queue) {
        super(op, route);
        if (route == null) {
            throw new IllegalArgumentException("HTTP route may not be null");
        }
        this.reference = new BasicPoolEntryRef(this, queue);
    }
    protected final OperatedClientConnection getConnection() {
        return super.connection;
    }
    protected final HttpRoute getPlannedRoute() {
        return super.route;
    }
    protected final BasicPoolEntryRef getWeakRef() {
        return this.reference;
    }
} 
