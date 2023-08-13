public class Leaky {
    static final int K = 1024;
    static class Connection {
        private final AsynchronousSocketChannel client;
        private final SocketChannel peer;
        private final ByteBuffer dst;
        private Future<Integer> readResult;
        Connection(AsynchronousChannelGroup group) throws Exception {
            ServerSocketChannel ssc =
                ServerSocketChannel.open().bind(new InetSocketAddress(0));
            InetAddress lh = InetAddress.getLocalHost();
            int port = ((InetSocketAddress)(ssc.getLocalAddress())).getPort();
            SocketAddress remote = new InetSocketAddress(lh, port);
            client = AsynchronousSocketChannel.open(group);
            client.connect(remote).get();
            peer = ssc.accept();
            ssc.close();
            dst = ByteBuffer.allocate(K*K);
        }
        void startRead() {
            dst.clear();
            readResult = client.read(dst);
        }
        void write() throws Exception {
            peer.write(ByteBuffer.wrap("X".getBytes()));
        }
        void finishRead() throws Exception {
            readResult.get();
        }
    }
    public static void main(String[] args) throws Exception {
        ThreadFactory threadFactory = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                return t;
            }
        };
        AsynchronousChannelGroup group =
            AsynchronousChannelGroup.withFixedThreadPool(4, threadFactory);
        final int CONNECTION_COUNT = 10;
        Connection[] connections = new Connection[CONNECTION_COUNT];
        for (int i=0; i<CONNECTION_COUNT; i++) {
            connections[i] = new Connection(group);
        }
        for (int i=0; i<1024; i++) {
            for (Connection conn: connections) {
                conn.startRead();
            }
            for (Connection conn: connections) {
                conn.write();
            }
            for (Connection conn: connections) {
                conn.finishRead();
            }
        }
        List<BufferPoolMXBean> pools =
            ManagementFactory.getPlatformMXBeans(BufferPoolMXBean.class);
        for (BufferPoolMXBean pool: pools)
            System.out.format("         %8s             ", pool.getName());
        System.out.println();
        for (int i=0; i<pools.size(); i++)
            System.out.format("%6s %10s %10s  ",  "Count", "Capacity", "Memory");
        System.out.println();
        for (BufferPoolMXBean pool: pools) {
            System.out.format("%6d %10d %10d  ",
                pool.getCount(), pool.getTotalCapacity(), pool.getMemoryUsed());
        }
        System.out.println();
    }
}
