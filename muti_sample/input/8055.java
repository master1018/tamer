public class LeaseCheckInterval implements Remote, Unreferenced {
    public static final String BINDING = "LeaseCheckInterval";
    private static final long LEASE_VALUE = 10000;
    private static final long TIMEOUT = 20000;
    private Object lock = new Object();
    private boolean unreferencedInvoked = false;
    public void unreferenced() {
        System.err.println("unreferenced() method invoked");
        synchronized (lock) {
            unreferencedInvoked = true;
            lock.notify();
        }
    }
    public static void main(String[] args) throws Exception {
        System.err.println("\nRegression test for bug 4285878\n");
        System.setProperty("java.rmi.dgc.leaseValue",
                           String.valueOf(LEASE_VALUE));
        LeaseCheckInterval obj = new LeaseCheckInterval();
        try {
            UnicastRemoteObject.exportObject(obj);
            System.err.println("exported remote object");
            Registry localRegistry =
                LocateRegistry.createRegistry(TestLibrary.REGISTRY_PORT);
            System.err.println("created local registry");
            localRegistry.bind(BINDING, obj);
            System.err.println("bound remote object in local registry");
            synchronized (obj.lock) {
                System.err.println("starting remote client VM...");
                (new JavaVM("SelfTerminator")).start();
                System.err.println("waiting for unreferenced() callback...");
                obj.lock.wait(TIMEOUT);
                if (obj.unreferencedInvoked) {
                    System.err.println("TEST PASSED: " +
                        "unreferenced() invoked in timely fashion");
                } else {
                    throw new RuntimeException(
                        "TEST FAILED: unreferenced() not invoked after " +
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
