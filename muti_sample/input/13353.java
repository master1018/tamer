public class UnreferencedContext implements Remote, Unreferenced, Runnable {
    private final static String BINDING = "UnreferencedContext";
    private final static long GC_INTERVAL = 6000;
    private final static long TIMEOUT = 60000;
    private Object lock = new Object();
    private boolean unreferencedInvoked = false;
    private ClassLoader unreferencedContext;
    public void run() {
        System.err.println("unreferenced method created thread succesfully");
    }
    public void unreferenced() {
        System.setSecurityManager(new java.rmi.RMISecurityManager());
        (new Thread(this)).start();
        System.err.println("unreferenced() method invoked");
        synchronized (lock) {
            unreferencedInvoked = true;
            unreferencedContext =
                Thread.currentThread().getContextClassLoader();
            lock.notify();
        }
    }
    public static void main(String[] args) {
        System.err.println("\nRegression test for bug 4171278\n");
        System.setProperty("sun.rmi.dgc.client.gcInterval",
            String.valueOf(GC_INTERVAL));
        UnreferencedContext obj = new UnreferencedContext();
        try {
            UnicastRemoteObject.exportObject(obj);
            UnicastRemoteObject.unexportObject(obj, true);
            ClassLoader intendedContext = new URLClassLoader(new URL[0]);
            Thread.currentThread().setContextClassLoader(intendedContext);
            System.err.println(
                "created and set intended context class loader: " +
                intendedContext);
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
                for (int i = 0; i < 10; i++) {
                    System.gc();
                    obj.lock.wait(TIMEOUT / 10);
                    if (obj.unreferencedInvoked) {
                        break;
                    }
                }
                if (obj.unreferencedInvoked) {
                    System.err.println(
                        "invoked with context class loader: " +
                        obj.unreferencedContext);
                    if (obj.unreferencedContext == intendedContext) {
                        System.err.println(
                            "TEST PASSED: unreferenced() invoked" +
                            " with intended context class loader");
                    } else {
                        throw new RuntimeException(
                            "TEST FAILED: unreferenced() invoked" +
                            " with incorrect context class loader");
                    }
                } else {
                    throw new RuntimeException(
                        "TEST FAILED: unreferenced() not invoked after " +
                        ((double) TIMEOUT / 1000.0) + " seconds or unreferenced failed to create a thread");
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
