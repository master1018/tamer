public class UnrecognizedRefType {
    public static void main(String[] args) throws Exception {
        System.err.println("\nRegression test for bug 4460983\n");
        test(new FakeRemoteObject("ActivatableServerRef"));
        test(new FakeRemoteObject("MarshalInputStream"));
        test(new FakeRemoteObject("XXX"));
        System.err.println("TEST PASSED");
    }
    private static void test(RemoteObject obj) throws Exception {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bout);
        out.writeObject(obj);
        ByteArrayInputStream bin =
            new ByteArrayInputStream(bout.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bin);
        try {
            Object obj2 = in.readObject();
            System.err.println(
                "Object unexpectedly deserialized successfully: " + obj2);
            throw new RuntimeException(
                "TEST FAILED: object successfully deserialized");
        } catch (ClassNotFoundException e) {
            System.err.println("ClassNotFoundException as expected:");
            e.printStackTrace();
        } 
    }
    private static class FakeRemoteObject extends RemoteObject {
        FakeRemoteObject(String refType) {
            super(new FakeRemoteRef(refType));
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
