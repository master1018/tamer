class ConnectionsWeakRef extends WeakReference {
    private final Connections conns;
    ConnectionsWeakRef (ConnectionsRef connsRef, ReferenceQueue queue) {
        super(connsRef, queue);
        this.conns = connsRef.getConnections();
    }
    Connections getConnections() {
        return conns;
    }
}
