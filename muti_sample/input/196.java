public class CheckLocking implements Runnable {
    private static Selector selector;
    public CheckLocking() {
    }
    public void run() {
        try {
            selector.select();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }
    private static void doSelect() throws Exception {
        Thread thread = new Thread(new CheckLocking());
        thread.start();
        Thread.sleep(1000);
    }
    public static void main (String[] args) throws Exception {
        selector = SelectorProvider.provider().openSelector();
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);
        SelectionKey sk = sc.register(selector,0,null);
        doSelect();
        sk.interestOps(SelectionKey.OP_READ);
        selector.wakeup();
        sc.close();
        selector.close();
    }
}
