public class MarshalledObjectGet implements Remote, Unreferenced {
    private static final String BINDING = "MarshalledObjectGet";
    private static final long GC_INTERVAL = 6000;
    private static final long TIMEOUT = 50000;
    private Object lock = new Object();
    private boolean unreferencedInvoked;
    public void unreferenced() {
        System.err.println("unreferenced() method invoked");
        synchronized (lock) {
            unreferencedInvoked = true;
            lock.notify();
        }
    }
    public static void main(String[] args) {
        System.err.println(
            "\nTest to verify correction interaction of " +
            "MarshalledObject.get and DGC registration\n");
        System.setProperty("sun.rmi.dgc.client.gcInterval",
            String.valueOf(GC_INTERVAL));
        MarshalledObjectGet obj = new MarshalledObjectGet();
        try {
            Remote stub = UnicastRemoteObject.exportObject(obj);
            System.err.println("exported remote object");
            MarshalledObject mobj = new MarshalledObject(stub);
            Remote unmarshalledStub = (Remote) mobj.get();
            System.err.println("unmarshalled stub from marshalled object");
            synchronized (obj.lock) {
                obj.unreferencedInvoked = false;
                unmarshalledStub = null;
                System.gc();
                System.err.println("cleared unmarshalled stub");
                System.err.println("waiting for unreferenced() callback " +
                                   "(SHOULD happen)...");
                obj.lock.wait(TIMEOUT);
                if (obj.unreferencedInvoked) {
                } else {
                    throw new RuntimeException(
                        "TEST FAILED: unrefereced() not invoked after " +
                        ((double) TIMEOUT / 1000.0) + " seconds");
                }
            }
            System.err.println("TEST PASSED");
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(
                    "TEST FAILED: unexpected exception: " + e.toString());
            }
        } finally {
            if (obj != null) {
                try {
                    UnicastRemoteObject.unexportObject(obj, true);
                } catch (Exception e) {
                }
            }
        }
    }
}
