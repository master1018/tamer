public class BasicAccept {
    static void server(ServerSocketChannel ssc) throws Exception {
        Selector acceptSelector = Selector.open();
        try {
            ssc.configureBlocking(false);
            SelectionKey acceptKey
                = ssc.register(acceptSelector, SelectionKey.OP_ACCEPT);
            for (;;) {
                int n = acceptSelector.select();
                if (Thread.interrupted())
                    break;
                if (n == 0)
                    continue;
                Set<SelectionKey> readyKeys = acceptSelector.selectedKeys();
                Iterator<SelectionKey> i = readyKeys.iterator();
                while (i.hasNext()) {
                    SelectionKey sk = i.next();
                    i.remove();
                    ServerSocketChannel nextReady
                        = (ServerSocketChannel)sk.channel();
                    SocketChannel sc = nextReady.accept();
                    ByteBuffer bb = ByteBuffer.wrap(new byte[] { 42 });
                    sc.write(bb);
                    sc.close();
                }
            }
        } finally {
            acceptSelector.close();
        }
    }
    private static class Server extends TestThread {
        final ServerSocketChannel ssc;
        Server() throws IOException {
            super("Server", System.err);
            this.ssc = ServerSocketChannel.open()
                .bind(new InetSocketAddress(0));
        }
        int port() {
            return ssc.socket().getLocalPort();
        }
        void go() throws Exception {
            try {
                server(ssc);
            } finally {
                ssc.close();
            }
        }
    }
    static void client(int port) throws Exception {
        InetAddress lh = InetAddress.getLocalHost();
        InetSocketAddress isa
            = new InetSocketAddress(lh, port);
        int connectFailures = 0;
        boolean result = false;
        SocketChannel sc = SocketChannel.open();
        for (;;) {
            try {
                result = sc.connect(isa);
                break;
            } catch (java.net.ConnectException e) {
                connectFailures++;
                if (connectFailures > 30)
                    throw new RuntimeException("Cannot connect");
                Thread.currentThread().sleep(100);
                sc = SocketChannel.open();
            }
        }
        if (result) {
            System.err.println("Connected");
        } else {
            System.err.println("Connection pending...");
            connectFailures = 0;
            while (!result) {
                try {
                    result = sc.finishConnect();
                    if (!result)
                        System.err.println("Not finished");
                    Thread.sleep(50);
                } catch (java.net.ConnectException e) {
                    Thread.sleep(100);
                    connectFailures++;
                    if (connectFailures > 30)
                        throw new RuntimeException("Cannot finish connecting");
                }
            }
            System.err.println("Finished connecting");
        }
        ByteBuffer bb = ByteBuffer.allocateDirect(1024);
        if (sc.read(bb) < 0)
            throw new RuntimeException("Failed to read from server");
        if (bb.get(0) != 42)
            throw new RuntimeException("Read wrong byte from server");
        System.err.println("Read from server");
        sc.close();
    }
    public static void main(String[] args) throws Exception {
        Server server = new Server();
        server.start();
        try {
            client(server.port());
        } finally {
            server.interrupt();
            server.finish(2000);
        }
    }
}
