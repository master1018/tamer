public class ReuseDefaultPort implements Remote {
    private static final int PORT = 2223;
    private ReuseDefaultPort() { }
    public static void main(String[] args) throws Exception {
        System.err.println("\nRegression test for bug 6269166\n");
        RMISocketFactory.setSocketFactory(new SF());
        Remote impl = new ReuseDefaultPort();
        Remote stub = UnicastRemoteObject.exportObject(impl, 0);
        System.err.println("- exported object: " + stub);
        try {
            Registry registry = LocateRegistry.createRegistry(PORT);
            System.err.println("- exported registry: " + registry);
            System.err.println("TEST PASSED");
        } finally {
            UnicastRemoteObject.unexportObject(impl, true);
        }
    }
    private static class SF extends RMISocketFactory {
        private static RMISocketFactory defaultFactory =
            RMISocketFactory.getDefaultSocketFactory();
        SF() { }
        public Socket createSocket(String host, int port) throws IOException {
            return defaultFactory.createSocket(host, port);
        }
        public ServerSocket createServerSocket(int port) throws IOException {
            if (port == 0) {
                port = PORT;
            }
            return defaultFactory.createServerSocket(port);
        }
    }
}
