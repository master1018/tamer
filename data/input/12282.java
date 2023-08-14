public class SelectAndCancel {
    static SelectionKey sk;
    public static void main(String[] args) throws Exception {
        final Selector selector = Selector.open();
        final ServerSocketChannel ssc =
            ServerSocketChannel.open().bind(new InetSocketAddress(0));
        final InetSocketAddress isa =
            new InetSocketAddress(InetAddress.getLocalHost(), ssc.socket().getLocalPort());
        new Thread(new Runnable() {
                public void run() {
                    try {
                        ssc.configureBlocking(false);
                        sk = ssc.register(selector, SelectionKey.OP_ACCEPT);
                        selector.select();
                    } catch (IOException e) {
                        System.err.println("error in selecting thread");
                        e.printStackTrace();
                    }
                }
            }).start();
        Thread.sleep(3000);
        new Thread(new Runnable() {
                public void run() {
                    try {
                        SocketChannel sc = SocketChannel.open();
                        sc.connect(isa);
                        ssc.close();
                        sk.cancel();
                        sc.close();
                    } catch (IOException e) {
                        System.err.println("error in closing thread");
                        System.err.println(e);
                    }
                }
            }).start();
        Thread.sleep(3000);
        selector.wakeup();
        selector.close();
    }
}
