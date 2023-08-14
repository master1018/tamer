public class CloseAfterConnect {
    public static void main(String[] args) throws Exception {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(new InetSocketAddress(0));
        InetAddress lh = InetAddress.getLocalHost();
        final SocketChannel sc = SocketChannel.open();
        final InetSocketAddress isa =
            new InetSocketAddress(lh, ssc.socket().getLocalPort());
        Runnable connector =
            new Runnable() {
                public void run() {
                    try {
                        sc.connect(isa);
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
            };
        Thread thr = new Thread(connector);
        thr.start();
        do {
            try {
                thr.join();
            } catch (InterruptedException x) { }
        } while (thr.isAlive());
        if (!sc.isConnected()) {
            throw new RuntimeException("SocketChannel not connected");
        }
        sc.close();
        ssc.accept().close();
        ssc.close();
    }
}
