public class RuntimeThreadInheritanceLeak implements Remote {
    private static final int TIMEOUT = 20000;
    public static void main(String[] args) {
        System.err.println("\nRegression test for bug 4404702\n");
        java.util.logging.LogManager.getLogManager();
        (new java.security.SecureRandom()).nextInt();
        RuntimeThreadInheritanceLeak obj = new RuntimeThreadInheritanceLeak();
        try {
            ClassLoader loader = URLClassLoader.newInstance(new URL[0]);
            ReferenceQueue refQueue = new ReferenceQueue();
            Reference loaderRef = new WeakReference(loader, refQueue);
            System.err.println("created loader: " + loader);
            Thread.currentThread().setContextClassLoader(loader);
            UnicastRemoteObject.exportObject(obj);
            Thread.currentThread().setContextClassLoader(
                ClassLoader.getSystemClassLoader());
            System.err.println(
                "exported remote object with loader as context class loader");
            loader = null;
            System.err.println("nulled strong reference to loader");
            UnicastRemoteObject.unexportObject(obj, true);
            System.err.println("unexported remote object");
            Thread.sleep(2000);
            System.gc();
            System.err.println(
                "waiting to be notified of loader being weakly reachable...");
            Reference dequeued = refQueue.remove(TIMEOUT);
            if (dequeued == null) {
                System.err.println(
                    "TEST FAILED: loader not deteced weakly reachable");
                dumpThreads();
                throw new RuntimeException(
                    "TEST FAILED: loader not detected weakly reachable");
            }
            System.err.println(
                "TEST PASSED: loader detected weakly reachable");
            dumpThreads();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("TEST FAILED: unexpected exception", e);
        } finally {
            try {
                UnicastRemoteObject.unexportObject(obj, true);
            } catch (RemoteException e) {
            }
        }
    }
    private static void dumpThreads() {
        System.err.println(
            "current live threads and their context class loaders:");
        Map threads = Thread.getAllStackTraces();
        for (Iterator iter = threads.entrySet().iterator(); iter.hasNext();) {
            Map.Entry e = (Map.Entry) iter.next();
            Thread t = (Thread) e.getKey();
            System.err.println("  thread: " + t);
            System.err.println("  context class loader: " +
                               t.getContextClassLoader());
            StackTraceElement[] trace = (StackTraceElement[]) e.getValue();
            for (int i = 0; i < trace.length; i++) {
                System.err.println("    " + trace[i]);
            }
        }
    }
}
