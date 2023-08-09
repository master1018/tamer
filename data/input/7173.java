public class NotSerializable {
    public static void main(String[] args) throws Exception {
        System.err.println("\nRegression test for bug 4460983\n");
        Activatable act = new FakeActivatable();
        try {
            ObjectOutputStream out =
                new ObjectOutputStream(new ByteArrayOutputStream());
            try {
                out.writeObject(act);
                throw new RuntimeException("TEST FAILED: " +
                    "Activatable instance successfully serialized");
            } catch (NotSerializableException e) {
                System.err.println("NotSerializableException as expected:");
                e.printStackTrace();
            } 
            System.err.println("TEST PASSED");
        } finally {
            try {
                Activatable.unexportObject(act, true);
            } catch (NoSuchObjectException e) {
            }
        }
    }
    private static class FakeActivatable extends Activatable {
        FakeActivatable() throws RemoteException {
            super(new ActivationID(new FakeActivator()), 0);
        }
    }
    private static class FakeActivator
        extends RemoteStub implements Activator
    {
        FakeActivator() {
            super(new FakeRemoteRef("FakeRef"));
        }
        public MarshalledObject activate(ActivationID id, boolean force) {
            return null;
        }
    }
    private static class FakeRemoteRef implements RemoteRef {
        private final String refType;
        FakeRemoteRef(String refType) {
            this.refType = refType;
        }
        public Object invoke(Remote obj,
                             Method method,
                             Object[] params,
                             long opnum)
        {
            throw new UnsupportedOperationException();
        }
        public RemoteCall newCall(RemoteObject obj,
                                  Operation[] op,
                                  int opnum,
                                  long hash)
        {
            throw new UnsupportedOperationException();
        }
        public void invoke(RemoteCall call) {
            throw new UnsupportedOperationException();
        }
        public void done(RemoteCall call) {
            throw new UnsupportedOperationException();
        }
        public String getRefClass(java.io.ObjectOutput out) {
            return refType;
        }
        public int remoteHashCode() { return hashCode(); }
        public boolean remoteEquals(RemoteRef obj) { return equals(obj); }
        public String remoteToString() { return toString(); }
        public void readExternal(ObjectInput in) {
            throw new UnsupportedOperationException();
        }
        public void writeExternal(ObjectOutput out) {
        }
    }
}
