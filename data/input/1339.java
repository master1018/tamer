public class ProxyRef implements RemoteRef {
    private static final long serialVersionUID = -6503061366316814723L;
    public ProxyRef(RemoteRef ref) {
        this.ref = ref;
    }
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        ref.readExternal(in);
    }
    public void writeExternal(ObjectOutput out) throws IOException {
        ref.writeExternal(out);
    }
    @Deprecated
    public void invoke(java.rmi.server.RemoteCall call) throws Exception {
        ref.invoke(call);
    }
    public Object invoke(Remote obj, Method method, Object[] params,
                         long opnum) throws Exception {
        return ref.invoke(obj, method, params, opnum);
    }
    @Deprecated
    public void done(java.rmi.server.RemoteCall call) throws RemoteException {
        ref.done(call);
    }
    public String getRefClass(ObjectOutput out) {
        return ref.getRefClass(out);
    }
    @Deprecated
    public java.rmi.server.RemoteCall newCall(RemoteObject obj,
            java.rmi.server.Operation[] op, int opnum,
                              long hash) throws RemoteException {
        return ref.newCall(obj, op, opnum, hash);
    }
    public boolean remoteEquals(RemoteRef obj) {
        return ref.remoteEquals(obj);
    }
    public int remoteHashCode() {
        return ref.remoteHashCode();
    }
    public String remoteToString() {
        return ref.remoteToString();
    }
    protected RemoteRef ref;
}
