class ObjectEndpoint {
    private final ObjID id;
    private final Transport transport;
    ObjectEndpoint(ObjID id, Transport transport) {
        if (id == null) {
            throw new NullPointerException();
        }
        assert transport != null || id.equals(new ObjID(ObjID.DGC_ID));
        this.id = id;
        this.transport = transport;
    }
    public boolean equals(Object obj) {
        if (obj instanceof ObjectEndpoint) {
            ObjectEndpoint oe = (ObjectEndpoint) obj;
            return id.equals(oe.id) && transport == oe.transport;
        } else {
            return false;
        }
    }
    public int hashCode() {
        return id.hashCode() ^ (transport != null ? transport.hashCode() : 0);
    }
    public String toString() {
        return id.toString();
    }
}
