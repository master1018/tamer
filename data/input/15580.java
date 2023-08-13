public class EchoService {
    private static void doIt(SocketChannel sc, int closeAfter, int delay) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(1024);
        int total = 0;
        for (;;) {
            bb.clear();
            int n = sc.read(bb);
            if (n < 0) {
                break;
            }
            total += n;
            bb.flip();
            sc.write(bb);
            if (closeAfter > 0 && total >= closeAfter) {
                break;
            }
        }
        sc.close();
        if (delay > 0) {
            try {
                Thread.currentThread().sleep(delay);
            } catch (InterruptedException x) { }
        }
    }
    private static void doIt(DatagramChannel dc) throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(1024);
        SocketAddress sa = dc.receive(bb);
        bb.flip();
        dc.send(bb, sa);
        dc.close();
    }
    static class Worker implements Runnable {
        private static int count = 0;
        private static Object lock = new Object();
        public static int count() {
            synchronized (lock) {
                return count;
            }
        }
        private SocketChannel sc;
        Worker(SocketChannel sc) {
            this.sc = sc;
            synchronized (lock) {
                count++;
            }
        }
        public void run() {
            try {
                doIt(sc, -1, -1);
            } catch (IOException x) {
            } finally {
                synchronized (lock) {
                    count--;
                }
            }
        }
    }
    public static void main(String args[]) throws IOException {
        Channel c = System.inheritedChannel();
        if (c == null) {
            return;
        }
        if (c instanceof SocketChannel) {
            int closeAfter = 0;
            int delay = 0;
            if (args.length > 0) {
                closeAfter = Integer.parseInt(args[0]);
            }
            if (args.length > 1) {
                delay = Integer.parseInt(args[1]);
            }
            doIt((SocketChannel)c, closeAfter, delay);
        }
        if (c instanceof ServerSocketChannel) {
            ServerSocketChannel ssc = (ServerSocketChannel)c;
            ssc.configureBlocking(false);
            Selector sel = ssc.provider().openSelector();
            SelectionKey sk = ssc.register(sel, SelectionKey.OP_ACCEPT);
            SocketChannel sc;
            int count = 0;
            for (;;) {
                 sel.select(5000);
                 if (sk.isAcceptable() && ((sc = ssc.accept()) != null)) {
                    Worker w = new Worker(sc);
                    (new Thread(w)).start();
                 } else {
                     if (Worker.count() == 0) {
                        break;
                     }
                 }
            }
            ssc.close();
        }
        if (c instanceof DatagramChannel) {
            doIt((DatagramChannel)c);
        }
        if (args.length > 0) {
            int delay = Integer.parseInt(args[0]);
            try {
                Thread.currentThread().sleep(delay);
            } catch (InterruptedException x) { }
        }
    }
}
