public class CloseServerSocket implements Remote {
    private static final int PORT = 2020;
    private CloseServerSocket() { }
    public static void main(String[] args) throws Exception {
        System.err.println("\nRegression test for bug 4457683\n");
        verifyPortFree(PORT);
        Registry registry = LocateRegistry.createRegistry(PORT);
        System.err.println("- exported registry: " + registry);
        verifyPortInUse(PORT);
        UnicastRemoteObject.unexportObject(registry, true);
        System.err.println("- unexported registry");
        Thread.sleep(1);        
        verifyPortFree(PORT);
        System.err.println("TEST PASSED");
    }
    private static void verifyPortFree(int port) throws IOException {
        ServerSocket ss = new ServerSocket(PORT);
        ss.close();
        System.err.println("- port " + port + " is free");
    }
    private static void verifyPortInUse(int port) throws IOException {
        try {
            verifyPortFree(port);
        } catch (BindException e) {
            System.err.println("- port " + port + " is in use");
            return;
        }
    }
    private static class SSF implements RMIServerSocketFactory {
        boolean serverSocketClosed = false;
        SSF() { };
        public ServerSocket createServerSocket(int port) throws IOException {
            return new SS(port);
        }
        private class SS extends ServerSocket {
            SS(int port) throws IOException {
                super(port);
                System.err.println("- created server socket: " + this);
            };
            public void close() throws IOException {
                synchronized (SSF.this) {
                    serverSocketClosed = true;
                    SSF.this.notifyAll();
                }
                System.err.println("- closing server socket: " + this);
                super.close();
            }
        }
    }
}
