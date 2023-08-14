public class KeepAliveDuringCall implements ShutdownMonitor {
    public static final String BINDING = "KeepAliveDuringCall";
    private static final int TIMEOUT = 20000;
    private Object lock = new Object();
    private Shutdown shutdown = null;
    private boolean stillAlive = false;
    public void submitShutdown(Shutdown shutdown) {
        synchronized (lock) {
            this.shutdown = shutdown;
            lock.notifyAll();
        }
    }
    public void declareStillAlive() {
        synchronized (lock) {
            stillAlive = true;
            lock.notifyAll();
        }
    }
    public static void main(String[] args) {
        System.err.println("\nRegression test for bug 4308492\n");
        KeepAliveDuringCall obj = new KeepAliveDuringCall();
        try {
            UnicastRemoteObject.exportObject(obj);
            System.err.println("exported shutdown monitor");
            Registry localRegistry =
                LocateRegistry.createRegistry(TestLibrary.REGISTRY_PORT);
            System.err.println("created local registry");
            localRegistry.bind(BINDING, obj);
            System.err.println("bound shutdown monitor in local registry");
            System.err.println("starting remote ShutdownImpl VM...");
            (new JavaVM("ShutdownImpl")).start();
            Shutdown s;
            synchronized (obj.lock) {
                System.err.println(
                    "waiting for submission of object to shutdown...");
                while ((s = obj.shutdown) == null) {
                    obj.lock.wait(TIMEOUT);
                }
                if (s == null) {
                    throw new RuntimeException(
                        "TEST FAILED: timeout waiting for shutdown object " +
                        "to make initial contact");
                }
                System.err.println("shutdown object submitted: " + s);
            }
            try {
                s.shutdown();
            } catch (RemoteException e) {
                throw new RuntimeException(
                    "TEST FAILED: shutdown method threw remote exception", e);
            }
            synchronized (obj.lock) {
                if (!obj.stillAlive) {
                    throw new RuntimeException("TEST FAILED: " +
                        "shutdown object not detected alive after unexport");
                }
            }
            System.err.println("TEST PASSED: " +
                "shutdown object detected still alive after unexport");
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(
                    "TEST FAILED: unexpected exception", e);
            }
        } finally {
            try {
                UnicastRemoteObject.unexportObject(obj, true);
            } catch (RemoteException e) {
            }
        }
    }
}
