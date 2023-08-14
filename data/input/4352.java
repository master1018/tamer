public class VerifyRemoteEquals {
    public interface Test extends Remote {
    }
    public static final class TestImpl
        extends UnicastRemoteObject implements Test
    {
        public TestImpl() throws RemoteException {
            super();
        }
        public TestImpl(RMIClientSocketFactory clientFactory,
                        RMIServerSocketFactory serverFactory)
            throws RemoteException
        {
            super(0, clientFactory, serverFactory);
        }
        public TestImpl(RMISocketFactory factory)
            throws RemoteException
        {
            super(0, factory, factory);
        }
    }
    public interface TestHome extends Remote {
        public Test get() throws RemoteException;
    }
    public static final class TestHomeImpl
        extends UnicastRemoteObject implements TestHome
    {
        private Test test;
        public TestHomeImpl(Test test)
            throws RemoteException {
            super();
            this.test = test;
        }
        public Test get() {
            return test;
        }
    }
    public static final class ServerSocketAndFactory
        extends ServerSocket implements RMIServerSocketFactory, Serializable
    {
        ServerSocketAndFactory() throws IOException, java.net.UnknownHostException {
            super(0);
        }
        ServerSocketAndFactory(int port) throws IOException,
            java.net.UnknownHostException
        {
            super(port);
        }
        public ServerSocket createServerSocket(int port)
            throws IOException
        {
            return new ServerSocketAndFactory(port);
        }
        public int hashCode() {
            return getLocalPort();
        }
        public boolean equals(Object obj) {
            if (obj instanceof ServerSocketAndFactory) {
                ServerSocketAndFactory ssf = (ServerSocketAndFactory) obj;
                if (getLocalPort() == ssf.getLocalPort()) {
                    return true;
                }
            }
            return false;
        }
    }
    public static final class ClientSocketAndFactory
        extends Socket implements RMIClientSocketFactory, Serializable
    {
        ClientSocketAndFactory() {
        }
        ClientSocketAndFactory(String host, int port) throws IOException {
            super(host, port);
        }
        public Socket createSocket(String host, int port)
            throws IOException {
            return new ClientSocketAndFactory(host, port);
        }
        public int hashCode() {
            return getPort();
        }
        public boolean equals(Object obj) {
            if (obj instanceof ClientSocketAndFactory) {
                ClientSocketAndFactory csf = (ClientSocketAndFactory) obj;
                if (getPort() == csf.getPort()) {
                    return true;
                }
            }
            return false;
        }
    }
    public static void main(String[] args) {
        try {
            System.out.println("\n\nRegression test for, 4251010\n\n");
            Test test = new TestImpl(new ClientSocketAndFactory(),
                                     new ServerSocketAndFactory());
            TestHome home = new TestHomeImpl(test);
            Test test0 = ((Test) RemoteObject.toStub(test));
            Test test1 = ((TestHome) RemoteObject.toStub(home)).get();
            Test test2 = ((TestHome) RemoteObject.toStub(home)).get();
            Test test3 = ((Test) (new MarshalledObject(test)).get());
            if (test0.equals(test1)) {
                System.out.println("test0, test1, stubs equal");
            } else {
                TestLibrary.bomb("test0, test1, stubs not equal");
            }
            if (test1.equals(test2)) {
                System.out.println("test1, test2, stubs equal");
            } else {
                TestLibrary.bomb("test1, test2, stubs not equal");
            }
            if (test2.equals(test3)) {
                System.out.println("test2, test3, stubs equal");
            } else {
                TestLibrary.bomb("test2, test3, stubs not equal");
            }
            test0 = null;
            test1 = null;
            test2 = null;
            test3 = null;
            TestLibrary.unexport(test);
            TestLibrary.unexport(home);
            System.err.println("test passed: stubs were equal");
        } catch (Exception e) {
            TestLibrary.bomb("test got unexpected exception", e);
        }
    }
}
