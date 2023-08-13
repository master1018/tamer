public class HandshakeFailure {
    private static final int PORT = 2020;
    private static final int TIMEOUT = 10000;
    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(PORT);
        Registry registry = LocateRegistry.getRegistry(PORT);
        Connector connector = new Connector(registry);
        Thread t = new Thread(connector);
        t.setDaemon(true);
        t.start();
        Socket socket = serverSocket.accept();
        socket.getOutputStream().write("Wrong way".getBytes());
        socket.close();
        t.join(TIMEOUT);
        synchronized (connector) {
            if (connector.success) {
                throw new RuntimeException(
                    "TEST FAILED: remote call succeeded??");
            }
            if (connector.exception == null) {
                throw new RuntimeException(
                    "TEST FAILED: remote call did not time out");
            } else {
                System.err.println("remote call failed with exception:");
                connector.exception.printStackTrace();
                System.err.println();
                if (connector.exception instanceof MarshalException) {
                    System.err.println(
                        "TEST FAILED: MarshalException thrown, expecting " +
                        "java.rmi.ConnectException or ConnectIOException");
                } else if (connector.exception instanceof ConnectException ||
                           connector.exception instanceof ConnectIOException)
                {
                    System.err.println(
                        "TEST PASSED: java.rmi.ConnectException or " +
                        "ConnectIOException thrown");
                } else {
                    throw new RuntimeException(
                        "TEST FAILED: unexpected Exception thrown",
                        connector.exception);
                }
            }
        }
    }
    private static class Connector implements Runnable {
        private final Registry registry;
        boolean success = false;
        Exception exception = null;
        Connector(Registry registry) {
            this.registry = registry;
        }
        public void run() {
            try {
                registry.lookup("Dale Cooper");
                synchronized (this) {
                    success = true;
                }
            } catch (Exception e) {
                synchronized (this) {
                    exception = e;
                }
            }
        }
    }
}
