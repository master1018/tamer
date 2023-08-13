public class RegAfterPreClose {
    static final int TEST_ITERATIONS = 300;
    static volatile boolean done;
    static class Connector implements Runnable {
        private final SocketAddress sa;
        Connector(int port) throws IOException {
            InetAddress lh = InetAddress.getLocalHost();
            this.sa = new InetSocketAddress(lh, port);
        }
        public void run() {
            while (!done) {
                try {
                    SocketChannel.open(sa).close();
                } catch (IOException x) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException  ignore) { }
                }
            }
        }
    }
    static class Closer implements Runnable {
        private final Channel channel;
        Closer(Channel sc) {
            this.channel = sc;
        }
        public void run() {
            try {
                channel.close();
            } catch (IOException ignore) { }
        }
    }
    public static void main(String[] args) throws Exception {
        InetSocketAddress isa = new InetSocketAddress(0);
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(isa);
        final Selector sel = Selector.open();
        ssc.configureBlocking(false);
        SelectionKey key = ssc.register(sel, SelectionKey.OP_ACCEPT);
        ThreadFactory factory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            }
        };
        ExecutorService executor = Executors.newFixedThreadPool(2, factory);
        executor.execute(new Connector(ssc.socket().getLocalPort()));
        int remaining = TEST_ITERATIONS;
        while (remaining > 0) {
            sel.select();
            if (key.isAcceptable()) {
                SocketChannel sc = ssc.accept();
                if (sc != null) {
                    remaining--;
                    sc.configureBlocking(false);
                    sc.register(sel, SelectionKey.OP_READ);
                    executor.execute(new Closer(sc));
                }
            }
            sel.selectedKeys().clear();
        }
        done = true;
        sel.close();
        executor.shutdown();
    }
}
