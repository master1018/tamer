public class PinClientSocketFactory {
    private static final int PORT = 2345;
    private static final int SESSIONS = 50;
    public interface Factory extends Remote {
        Session getSession() throws RemoteException;
    }
    public interface Session extends Remote {
        void ping() throws RemoteException;
    }
    private static class FactoryImpl implements Factory {
        FactoryImpl() { }
        public Session getSession() throws RemoteException {
            Session impl = new SessionImpl();
            UnicastRemoteObject.exportObject(impl, 0, new CSF(), new SSF());
            return impl;
        }
    }
    private static class SessionImpl implements Session {
        SessionImpl() { }
        public void ping() { }
    }
    public static void main(String[] args) throws Exception {
        System.err.println("\nRegression test for bug 4486732\n");
        Factory factoryImpl = new FactoryImpl();
        Factory factoryStub =
            (Factory) UnicastRemoteObject.exportObject(factoryImpl, 0);
        for (int i = 0; i < SESSIONS; i++) {
            Session session = factoryStub.getSession();
            session.ping();
        }
        UnicastRemoteObject.unexportObject(factoryImpl, true);
        Registry registryImpl = LocateRegistry.createRegistry(PORT);
        CSF csf = new CSF();
        Reference<CSF> registryRef = new WeakReference<CSF>(csf);
        Registry registryStub = LocateRegistry.getRegistry("", PORT, csf);
        csf = null;
        registryStub.list();
        registryStub = null;
        UnicastRemoteObject.unexportObject(registryImpl, true);
        System.gc();
        Thread.sleep(3 * Long.getLong("sun.rmi.transport.connectionTimeout",
                                      15000));
        System.gc();
        if (CSF.deserializedInstances.size() != SESSIONS) {
            throw new Error("unexpected number of deserialized instances: " +
                            CSF.deserializedInstances.size());
        }
        int nonNullCount = 0;
        for (Reference<CSF> ref : CSF.deserializedInstances) {
            csf = ref.get();
            if (csf != null) {
                System.err.println("non-null deserialized instance: " + csf);
                nonNullCount++;
            }
        }
        if (nonNullCount > 0) {
            throw new Error("TEST FAILED: " +
                            nonNullCount + " non-null deserialized instances");
        }
        csf = registryRef.get();
        if (csf != null) {
            System.err.println("non-null registry instance: " + csf);
            throw new Error("TEST FAILED: non-null registry instance");
        }
        System.err.println("TEST PASSED");
    }
    private static class CSF implements RMIClientSocketFactory, Serializable {
        static final List<Reference<CSF>> deserializedInstances =
            Collections.synchronizedList(new ArrayList<Reference<CSF>>());
        private static final AtomicInteger count = new AtomicInteger(0);
        private int num = count.incrementAndGet();
        CSF() { }
        public Socket createSocket(String host, int port) throws IOException {
            return new Socket(host, port);
        }
        public int hashCode() {
            return num;
        }
        public boolean equals(Object obj) {
            return obj instanceof CSF && ((CSF) obj).num == num;
        }
        private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException
        {
            in.defaultReadObject();
            deserializedInstances.add(new WeakReference<CSF>(this));
        }
    }
    private static class SSF implements RMIServerSocketFactory {
        SSF() { }
        public ServerSocket createServerSocket(int port) throws IOException {
            return new ServerSocket(port);
        }
    }
}
