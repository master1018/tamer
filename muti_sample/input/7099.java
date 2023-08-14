public class UnexportLeak implements Ping {
    private static int PORT = 2006;
    public void ping() {
    }
    public static void main(String[] args) {
        try {
            System.err.println("\nRegression test for bug 4331349\n");
            LocateRegistry.createRegistry(PORT);
            Remote obj = new UnexportLeak();
            WeakReference wr = new WeakReference(obj);
            UnicastRemoteObject.exportObject(obj);
            LocateRegistry.getRegistry(PORT).rebind("UnexportLeak", obj);
            UnicastRemoteObject.unexportObject(obj, true);
            obj = null;
            flushRefs();
            if (wr.get() != null) {
                System.err.println("FAILED: unexported object not collected");
                throw new RuntimeException(
                    "FAILED: unexported object not collected");
            } else {
                System.err.println("PASSED: unexported object collected");
            }
        } catch (RemoteException e) {
            System.err.println(
                "FAILED: RemoteException encountered: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("FAILED: RemoteException encountered");
        }
    }
    private static void flushRefs() {
        java.util.Vector chain = new java.util.Vector();
        try {
            while (true) {
                int[] hungry = new int[65536];
                chain.addElement(hungry);
            }
        } catch (OutOfMemoryError e) {
        }
    }
}
