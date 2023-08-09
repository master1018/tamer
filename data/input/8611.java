final class ConnectionsRef {
    final private Connections conns;
    ConnectionsRef(Connections conns) {
        this.conns = conns;
    }
    Connections getConnections() {
        return conns;
    }
}
