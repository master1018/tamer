public class RmidViaInheritedChannel implements Callback {
    private static final int PORT = 5398;
    private static final Object lock = new Object();
    private static boolean notified = false;
    private RmidViaInheritedChannel() {}
    public void notifyTest() {
        synchronized (lock) {
            notified = true;
            System.err.println("notification received.");
            lock.notifyAll();
        }
    }
    public static void main(String[] args) throws Exception {
        RMID rmid = null;
        Callback obj = null;
        try {
            System.err.println("export callback object and bind in registry");
            obj = new RmidViaInheritedChannel();
            Callback proxy = (Callback)
                UnicastRemoteObject.exportObject(obj, 0);
            Registry registry =
                LocateRegistry.createRegistry(TestLibrary.REGISTRY_PORT);
            registry.bind("Callback", proxy);
            System.err.println("start rmid with inherited channel");
            RMID.removeLog();
            rmid = RMID.createRMID(System.out, System.err, true, false, PORT);
            rmid.addOptions(new String[]{
                "-Djava.nio.channels.spi.SelectorProvider=RmidViaInheritedChannel$RmidSelectorProvider"});
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
    public static class RmidSelectorProvider extends SelectorProvider {
        private final SelectorProvider provider;
        private ServerSocketChannel channel = null;
        public RmidSelectorProvider() {
            provider =  sun.nio.ch.DefaultSelectorProvider.create();
        }
        public DatagramChannel openDatagramChannel()
            throws IOException
        {
            return provider.openDatagramChannel();
        }
        public DatagramChannel openDatagramChannel(ProtocolFamily family)
            throws IOException
        {
            return provider.openDatagramChannel(family);
        }
        public Pipe openPipe()
            throws IOException
        {
            return provider.openPipe();
        }
        public AbstractSelector openSelector()
            throws IOException
        {
            return provider.openSelector();
        }
        public ServerSocketChannel openServerSocketChannel()
            throws IOException
        {
            return provider.openServerSocketChannel();
        }
        public SocketChannel openSocketChannel()
             throws IOException
        {
            return provider.openSocketChannel();
        }
        public synchronized Channel inheritedChannel() throws IOException {
            System.err.println("RmidSelectorProvider.inheritedChannel");
            if (channel == null) {
                channel = ServerSocketChannel.open();
                ServerSocket serverSocket = channel.socket();
                serverSocket.bind(
                     new InetSocketAddress(InetAddress.getLocalHost(), PORT));
                System.err.println("serverSocket = " + serverSocket);
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
interface Callback extends Remote {
    void notifyTest() throws IOException;
}
