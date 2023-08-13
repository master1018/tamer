public class ShutdownImpl implements Shutdown {
    private static Remote impl;         
    private final ShutdownMonitor monitor;
    private ShutdownImpl(ShutdownMonitor monitor) {
        this.monitor = monitor;
    }
    public void shutdown() {
        try {
            System.err.println(
                "(ShutdownImpl.shutdown) shutdown method invoked:");
            UnicastRemoteObject.unexportObject(this, true);
            System.err.println(
                "(ShutdownImpl.shutdown) shutdown object unexported");
            Thread.sleep(500);
            System.err.println("(ShutDownImpl.shutdown) FEE");
            Thread.sleep(500);
            System.err.println("(ShutDownImpl.shutdown) FIE");
            Thread.sleep(500);
            System.err.println("(ShutDownImpl.shutdown) FOE");
            Thread.sleep(500);
            System.err.println("(ShutDownImpl.shutdown) FOO");
            monitor.declareStillAlive();
            System.err.println("(ShutDownImpl.shutdown) still alive!");
        } catch (Exception e) {
            throw new RuntimeException(
                "unexpected exception occurred in shutdown method", e);
        }
    }
    public static void main(String[] args) {
        try {
            Registry registry =
                LocateRegistry.getRegistry("", TestLibrary.REGISTRY_PORT);
            ShutdownMonitor monitor = (ShutdownMonitor)
                registry.lookup(KeepAliveDuringCall.BINDING);
            System.err.println("(ShutdownImpl) retrieved shutdown monitor");
            impl = new ShutdownImpl(monitor);
            Shutdown stub = (Shutdown) UnicastRemoteObject.exportObject(impl);
            System.err.println("(ShutdownImpl) exported shutdown object");
            monitor.submitShutdown(stub);
            System.err.println("(ShutdownImpl) submitted shutdown object");
        } catch (Exception e) {
            System.err.println("(ShutdownImpl) TEST SUBPROCESS FAILURE:");
            e.printStackTrace();
        }
    }
}
