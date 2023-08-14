public class UseCustomRef
        extends RemoteServer
        implements Ping
{
    public UseCustomRef() throws RemoteException {
        exportObject();
    }
    public void exportObject() throws RemoteException {
        ref = new CustomServerRef(new LiveRef(0));
        ((ServerRef) ref).exportObject(this, null);
    }
    public RemoteRef getRef() { return ref; }
    public void ping() {}
    public void receiveAndPing(Ping p) throws RemoteException {
        p.ping();
    }
    public static void main(String[] args) {
        Ping obj = null;
        Registry registry = null;
        try {
            TestLibrary.suggestSecurityManager("java.rmi.RMISecurityManager");
            System.err.println("creating Registry...");
            registry = LocateRegistry.createRegistry(TestLibrary.REGISTRY_PORT);
            System.err.println("creating UseCustomRef...");
            UseCustomRef cr = new UseCustomRef();
            RemoteRef ref = cr.getRef();
            if (!(ref instanceof CustomServerRef)) {
                TestLibrary.bomb("test failed: reference not " +
                                "instanceof CustomServerRef");
            }
            String name = "
            System.err.println("binding object in registry...");
            Naming.rebind(name, cr);
            System.err.println("ping object...");
            obj = (Ping) Naming.lookup(name);
            obj.ping();
            System.err.println("pass object in remote call...");
            obj.receiveAndPing(cr);
            System.err.println("writing remote object to stream...");
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bout);
            out.writeObject(cr);
            out.flush();
            out.close();
            System.err.println("reading remote object from stream...");
            ObjectInputStream in = new ObjectInputStream(
                new ByteArrayInputStream(bout.toByteArray()));
            cr = (UseCustomRef) in.readObject();
            System.err.println("re-export object read...");
            cr.exportObject();
            System.err.println("look up object again...");
            Naming.rebind(name, cr);
            System.err.println("ping object read...");
            obj = (Ping) Naming.lookup(name);
            obj.ping();
            System.err.println("TEST PASSED");
            Naming.unbind(name);
            cr = null;
        } catch (Exception e) {
            TestLibrary.bomb("test failed with exception: ", e);
        } finally {
            TestLibrary.unexport(obj);
            TestLibrary.unexport(registry);
            registry = null;
            obj = null;
        }
    }
    public static class CustomServerRef
        extends sun.rmi.server.UnicastServerRef
    {
        public CustomServerRef() {}
        public CustomServerRef(LiveRef ref) {
            super(ref);
        }
        public String getRefClass(ObjectOutput out) {
            return "";
        }
        protected void unmarshalCustomCallData(ObjectInput in)
            throws IOException, ClassNotFoundException
        {
            System.err.println("unmarshalling call data...");
            String s = (String) (in.readObject());
            System.err.println(s);
        }
        protected RemoteRef getClientRef() {
            return new CustomRef(ref);
        }
    }
    public static class CustomRef extends sun.rmi.server.UnicastRef {
        public CustomRef() {
        }
        public CustomRef(sun.rmi.transport.LiveRef ref) {
            super(ref);
        }
        protected void marshalCustomCallData(ObjectOutput out)
            throws IOException
        {
            System.err.println("marshalling call data...");
            out.writeObject("hello there.");
        }
        public String getRefClass(ObjectOutput out) {
            return "";
        }
    }
}
