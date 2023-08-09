public class RMIServerFactory
    implements RMIServerSocketFactory, Serializable {
    private String test;
    private boolean throwException;
    public RMIServerFactory(String test) {
        this.test = test;
        throwException = test.equals("test_server_factory") ? true : false;
    }
    public ServerSocket createServerSocket(int port) throws IOException {
        if (throwException) {
            throw new RuntimeException(test);
        } else {
            System.out.println("Calling createServerSocket(" + port + ")");
            ServerSocket ss = new ServerSocket(port);
            System.out.println("ServerSocket = " + ss);
            return ss;
        }
    }
}
