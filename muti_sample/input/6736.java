public class RMIClientFactory
    implements RMIClientSocketFactory, Serializable {
    private String test;
    private boolean throwException;
    public RMIClientFactory(String test) {
        this.test = test;
        throwException = test.equals("test_client_factory") ? true : false;
    }
    public Socket createSocket(String host, int port) throws IOException {
        if (throwException) {
            throw new RuntimeException(test);
        } else {
            System.out.println("Calling createSocket("+host+","+port+")");
            Socket s = new Socket(host, port);
            System.out.println("Socket = " + s);
            return s;
        }
    }
}
