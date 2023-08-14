public class PinLastArguments {
    public interface Ping extends Remote {
        void ping(Object first, Object second) throws RemoteException;
    }
    private static class PingImpl implements Ping {
        PingImpl() { }
        public void ping(Object first, Object second) {
            System.err.println("ping invoked: " + first + ", " + second);
        }
    }
    public static void main(String[] args) throws Exception {
        System.err.println("\nRegression test for bug 6332349\n");
        Ping impl = new PingImpl();
        Reference<?> ref = new WeakReference<Ping>(impl);
        try {
            Ping stub = (Ping) UnicastRemoteObject.exportObject(impl, 0);
            Object notSerializable = new Object();
            stub.ping(impl, null);
            try {
                stub.ping(impl, notSerializable);
            } catch (MarshalException e) {
                if (e.getCause() instanceof NotSerializableException) {
                    System.err.println("ping invocation failed as expected");
                } else {
                    throw e;
                }
            }
        } finally {
            UnicastRemoteObject.unexportObject(impl, true);
        }
        impl = null;
        System.gc();
        if (ref.get() != null) {
            throw new Error("TEST FAILED: impl not garbage collected");
        }
        System.err.println("TEST PASSED");
    }
}
