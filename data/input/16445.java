public class FiniteGCLatency implements Remote, Unreferenced {
    private final static String BINDING = "FiniteGCLatency";
    private final static long GC_INTERVAL = 6000;
    private final static long TIMEOUT = 50000;
    private Object lock = new Object();
    private boolean unreferencedInvoked = false;
    public void unreferenced() {
        System.err.println("unreferenced() method invoked");
        synchronized (lock) {
            unreferencedInvoked = true;
            lock.notify();
        }
    }
    public static void main(String[] args) {
        System.err.println("\nRegression test for bug 4164696\n");
        System.setProperty("sun.rmi.dgc.client.gcInterval",
            String.valueOf(GC_INTERVAL));
        FiniteGCLatency obj = new FiniteGCLatency();
        try {
            UnicastRemoteObject.exportObject(obj);
            System.err.println("exported remote object");
            LocateRegistry.createRegistry(TestLibrary.REGISTRY_PORT);
            System.err.println("created registry");
            Registry registry = LocateRegistry.getRegistry("", TestLibrary.REGISTRY_PORT);
            registry.bind(BINDING, obj);
            System.err.println("bound remote object in registry");
            synchronized (obj.lock) {
                registry.unbind(BINDING);
                System.err.println("unbound remote object from registry; " +
                    "waiting for unreferenced() callback...");
                obj.lock.wait(TIMEOUT);
                if (obj.unreferencedInvoked) {
                    System.err.println("TEST PASSED: unreferenced() invoked");
                } else {
                    throw new RuntimeException(
                        "TEST FAILED: unrefereced() not invoked after " +
                        ((double) TIMEOUT / 1000.0) + " seconds");
                }
            }
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(
                    "TEST FAILED: unexpected exception: " + e.toString());
            }
        } finally {
            try {
                UnicastRemoteObject.unexportObject(obj, true);
            } catch (RemoteException e) {
            }
        }
    }
}
