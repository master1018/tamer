public class CloseableTest {
    private static final Class closeArray[] = {
        JMXConnector.class,
        RMIConnector.class,
        RMIConnection.class,
        RMIConnectionImpl.class,
        RMIConnectionImpl_Stub.class,
        _RMIConnection_Stub.class,
        RMIServerImpl.class,
        RMIIIOPServerImpl.class,
        RMIJRMPServerImpl.class
    };
    public static void main(String[] args) throws Exception {
        System.out.println("Test that all the JMX Remote API classes that " +
                           "define\nthe method \"void close() throws " +
                           "IOException;\" extend\nor implement the " +
                           "java.io.Closeable interface.");
        int error = 0;
        for (Class c : closeArray) {
            System.out.println("\nTest " + c);
            if (Closeable.class.isAssignableFrom(c)) {
                System.out.println("Test passed!");
            } else {
                error++;
                System.out.println("Test failed!");
            }
        }
        if (error > 0) {
            final String msg = "\nTest FAILED! Got " + error + " error(s)";
            System.out.println(msg);
            throw new IllegalArgumentException(msg);
        } else {
            System.out.println("\nTest PASSED!");
        }
    }
}
