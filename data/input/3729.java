public class SSLSocketParametersTest implements Serializable {
    public interface Hello extends Remote {
        public String sayHello() throws RemoteException;
    }
    public class HelloImpl extends UnicastRemoteObject implements Hello {
        public HelloImpl(int port,
                         RMIClientSocketFactory csf,
                         RMIServerSocketFactory ssf)
            throws RemoteException {
            super(port, csf, ssf);
        }
        public String sayHello() {
            return "Hello World!";
        }
        public Remote runServer() throws IOException {
            System.out.println("Inside HelloImpl::runServer");
            Remote stub = toStub(this);
            System.out.println("Stub = " + stub);
            return stub;
        }
    }
    public class HelloClient {
        public void runClient(Remote stub) throws IOException {
            System.out.println("Inside HelloClient::runClient");
            Hello obj = (Hello) stub;
            String message = obj.sayHello();
            System.out.println(message);
        }
    }
    public class ClientFactory extends SslRMIClientSocketFactory {
        public ClientFactory() {
            super();
        }
        public Socket createSocket(String host, int port) throws IOException {
            System.out.println("ClientFactory::Calling createSocket(" +
                               host + "," + port + ")");
            return super.createSocket(host, port);
        }
    }
    public class ServerFactory extends SslRMIServerSocketFactory {
        public ServerFactory() {
            super();
        }
        public ServerFactory(String[] ciphers,
                             String[] protocols,
                             boolean need) {
            super(ciphers, protocols, need);
        }
        public ServerFactory(SSLContext context,
                             String[] ciphers,
                             String[] protocols,
                             boolean need) {
            super(context, ciphers, protocols, need);
        }
        public ServerSocket createServerSocket(int port) throws IOException {
            System.out.println("ServerFactory::Calling createServerSocket(" +
                               port + ")");
            return super.createServerSocket(port);
        }
    }
    public void runTest(String[] args) {
        int test = Integer.parseInt(args[0]);
        String msg1 = "Running SSLSocketParametersTest [" + test + "]";
        String msg2 = "SSLSocketParametersTest [" + test + "] PASSED!";
        String msg3 = "SSLSocketParametersTest [" + test + "] FAILED!";
        switch (test) {
        case 1: 
            System.out.println(msg1);
            try {
                HelloImpl server = new HelloImpl(
                          0,
                          new ClientFactory(),
                          new ServerFactory());
                Remote stub = server.runServer();
                HelloClient client = new HelloClient();
                client.runClient(stub);
                System.out.println(msg2);
            } catch (Exception e) {
                System.out.println(msg3 + " Exception: " + e.toString());
                e.printStackTrace(System.out);
                System.exit(1);
            }
            break;
        case 2: 
            System.out.println(msg1);
            try {
                HelloImpl server = new HelloImpl(
                          0,
                          new ClientFactory(),
                          new ServerFactory(null,
                                            null,
                                            false));
                Remote stub = server.runServer();
                HelloClient client = new HelloClient();
                client.runClient(stub);
                System.out.println(msg2);
            } catch (Exception e) {
                System.out.println(msg3 + " Exception: " + e.toString());
                e.printStackTrace(System.out);
                System.exit(1);
            }
            break;
        case 3: 
            System.out.println(msg1);
            try {
                HelloImpl server = new HelloImpl(
                          0,
                          new ClientFactory(),
                          new ServerFactory(null,
                                            null,
                                            null,
                                            true));
                Remote stub = server.runServer();
                HelloClient client = new HelloClient();
                client.runClient(stub);
                System.out.println(msg2);
            } catch (Exception e) {
                System.out.println(msg3 + " Exception: " + e.toString());
                e.printStackTrace(System.out);
                System.exit(1);
            }
            break;
        case 4: 
            System.out.println(msg1);
            try {
                HelloImpl server = new HelloImpl(
                          0,
                          new ClientFactory(),
                          new ServerFactory(SSLContext.getDefault(),
                                            new String[] {"dummy_ciphersuite"},
                                            null,
                                            false));
                Remote stub = server.runServer();
                HelloClient client = new HelloClient();
                client.runClient(stub);
                System.out.println(msg3);
                System.exit(1);
            } catch (Exception e) {
                System.out.println(msg2 + " Exception: " + e.toString());
                System.exit(0);
            }
            break;
        case 5: 
            System.out.println(msg1);
            try {
                HelloImpl server = new HelloImpl(
                          0,
                          new ClientFactory(),
                          new ServerFactory(null,
                                            new String[] {"dummy_protocol"},
                                            false));
                Remote stub = server.runServer();
                HelloClient client = new HelloClient();
                client.runClient(stub);
                System.out.println(msg3);
                System.exit(1);
            } catch (Exception e) {
                System.out.println(msg2 + " Exception: " + e.toString());
                System.exit(0);
            }
            break;
        case 6: 
            System.out.println(msg1);
            try {
                System.setProperty("javax.rmi.ssl.client.enabledCipherSuites",
                                   "dummy_ciphersuite");
                HelloImpl server = new HelloImpl(
                          0,
                          new ClientFactory(),
                          new ServerFactory());
                Remote stub = server.runServer();
                HelloClient client = new HelloClient();
                client.runClient(stub);
                System.out.println(msg3);
                System.exit(1);
            } catch (Exception e) {
                System.out.println(msg2 + " Exception: " + e.toString());
                System.exit(0);
            }
            break;
        case 7: 
            System.out.println(msg1);
            try {
                System.setProperty("javax.rmi.ssl.client.enabledProtocols",
                                   "dummy_protocol");
                HelloImpl server = new HelloImpl(
                          0,
                          new ClientFactory(),
                          new ServerFactory());
                Remote stub = server.runServer();
                HelloClient client = new HelloClient();
                client.runClient(stub);
                System.out.println(msg3);
                System.exit(1);
            } catch (Exception e) {
                System.out.println(msg2 + " Exception: " + e.toString());
                System.exit(0);
            }
            break;
        default:
            throw new IllegalArgumentException("invalid test number");
        }
    }
    public static void main(String[] args) {
        final String keystore = System.getProperty("test.src") +
            File.separator + "keystore";
        System.out.println("KeyStore = " + keystore);
        System.setProperty("javax.net.ssl.keyStore", keystore);
        System.setProperty("javax.net.ssl.keyStorePassword", "password");
        final String truststore = System.getProperty("test.src") +
            File.separator + "truststore";
        System.out.println("TrustStore = " + truststore);
        System.setProperty("javax.net.ssl.trustStore", truststore);
        System.setProperty("javax.net.ssl.trustStorePassword", "trustword");
        SSLSocketParametersTest test = new SSLSocketParametersTest();
        test.runTest(args);
        System.exit(0);
    }
}
