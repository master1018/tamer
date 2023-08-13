public class DieBeforeComplete {
    public static void main(String[] args) throws Exception {
        final AsynchronousServerSocketChannel listener =
                AsynchronousServerSocketChannel.open().bind(new InetSocketAddress(0));
        InetAddress lh = InetAddress.getLocalHost();
        int port = ((InetSocketAddress) (listener.getLocalAddress())).getPort();
        final SocketAddress sa = new InetSocketAddress(lh, port);
        Future<AsynchronousSocketChannel> r1 =
                initiateAndDie(new Task<AsynchronousSocketChannel>() {
            public Future<AsynchronousSocketChannel> run() {
                return listener.accept();
            }});
        SocketChannel peer = SocketChannel.open(sa);
        final AsynchronousSocketChannel channel = r1.get();
        final ByteBuffer dst = ByteBuffer.allocate(100);
        Future<Integer> r2 = initiateAndDie(new Task<Integer>() {
            public Future<Integer> run() {
                return channel.read(dst);
            }});
        peer.write(ByteBuffer.wrap("hello".getBytes()));
        int nread = r2.get();
        if (nread <= 0)
            throw new RuntimeException("Should have read at least one byte");
        boolean completedImmediately;
        Future<Integer> r3;
        do {
            final ByteBuffer src = ByteBuffer.wrap(new byte[10000]);
            r3 = initiateAndDie(new Task<Integer>() {
                public Future<Integer> run() {
                    return channel.write(src);
                }});
            try {
                int nsent = r3.get(5, TimeUnit.SECONDS);
                if (nsent <= 0)
                    throw new RuntimeException("Should have wrote at least one byte");
                completedImmediately = true;
            } catch (TimeoutException x) {
                completedImmediately = false;
            }
        } while (completedImmediately);
        peer.configureBlocking(false);
        ByteBuffer src = ByteBuffer.allocateDirect(10000);
        do {
            src.clear();
            nread = peer.read(src);
            if (nread == 0) {
                Thread.sleep(100);
                nread = peer.read(src);
            }
        } while (nread > 0);
        int nsent = r3.get();
        if (nsent <= 0)
            throw new RuntimeException("Should have wrote at least one byte");
    }
    static interface Task<T> {
        Future<T> run();
    }
    static <T> Future<T> initiateAndDie(final Task<T> task) {
        final AtomicReference<Future<T>> result = new AtomicReference<Future<T>>();
        Runnable r = new Runnable() {
            public void run() {
                result.set(task.run());
            }
        };
        Thread t = new Thread(r);
        t.start();
        while (t.isAlive()) {
            try {
                t.join();
            } catch (InterruptedException x) {
            }
        }
        return result.get();
    }
}
