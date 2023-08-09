public class InheritedChannelNotServerSocket {
    private static final int PORT = 5398;
    private static final Object lock = new Object();
    private static boolean notified = false;
    private InheritedChannelNotServerSocket() { }
    public interface Callback extends Remote {
        void notifyTest() throws RemoteException;
    }
    public static class CallbackImpl implements Callback {
        CallbackImpl() { }
        public void notifyTest() {
            synchronized (lock) {
                notified = true;
                System.err.println("notification received.");
                lock.notifyAll();
            }
        }
    }
    public static void main(String[] args) throws Exception {
        System.err.println("\nRegression test for bug 6261402\n");
        RMID rmid = null;
        Callback obj = null;
        try {
            System.err.println("export callback object and bind in registry");
            obj = new CallbackImpl();
            Callback proxy =
                (Callback) UnicastRemoteObject.exportObject(obj, 0);
            Registry registry =
                LocateRegistry.createRegistry(TestLibrary.REGISTRY_PORT);
            registry.bind("Callback", proxy);
            System.err.println("start rmid with inherited channel");
            RMID.removeLog();
            rmid = RMID.createRMID(System.out, System.err, true, true, PORT);
            rmid.addOptions(new String[]{
                "-Djava.nio.channels.spi.SelectorProvider=" +
                "InheritedChannelNotServerSocket$SP"});
            rmid.start();
            System.err.println("get activation system");
            ActivationSystem system = ActivationGroup.getSystem();
            System.err.println("ActivationSystem = " + system);
            synchronized (lock) {
                while (!notified) {
                    lock.wait();
                }
            }
            System.err.println("TEST PASSED");
        } finally {
            if (obj != null) {
                UnicastRemoteObject.unexportObject(obj, true);
            }
            ActivationLibrary.rmidCleanup(rmid, PORT);
        }
    }
    public static class SP extends SelectorProvider {
        private final SelectorProvider provider;
        private volatile SocketChannel channel = null;
        public SP() {
            provider = sun.nio.ch.DefaultSelectorProvider.create();
        }
        public DatagramChannel openDatagramChannel() throws IOException {
            return provider.openDatagramChannel();
        }
        public DatagramChannel openDatagramChannel(ProtocolFamily family)
            throws IOException
        {
            return provider.openDatagramChannel(family);
        }
        public Pipe openPipe() throws IOException {
            return provider.openPipe();
        }
        public AbstractSelector openSelector() throws IOException {
            return provider.openSelector();
        }
        public ServerSocketChannel openServerSocketChannel()
            throws IOException
        {
            return provider.openServerSocketChannel();
        }
        public SocketChannel openSocketChannel() throws IOException {
            return provider.openSocketChannel();
        }
        public synchronized Channel inheritedChannel() throws IOException {
            System.err.println("SP.inheritedChannel");
            if (channel == null) {
                channel = SocketChannel.open();
                Socket socket = channel.socket();
                System.err.println("socket = " + socket);
                try {
                    System.err.println("notify test...");
                    Registry registry =
                        LocateRegistry.getRegistry(TestLibrary.REGISTRY_PORT);
                    Callback obj = (Callback) registry.lookup("Callback");
                    obj.notifyTest();
                } catch (NotBoundException nbe) {
                    throw (IOException)
                        new IOException("callback object not bound").
                            initCause(nbe);
                }
            }
            return channel;
        }
    }
}
