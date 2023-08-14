public class SelectorLimit {
    static PrintStream log = System.err;
    static class Listener
        extends TestThread
    {
        volatile int count = 0;
        private ServerSocketChannel ssc;
        Listener(ServerSocketChannel ssc) {
            super("Listener");
            this.ssc = ssc;
        }
        void go() throws IOException {
            for (;;) {
                ssc.accept();
                count++;
            }
        }
    }
    static final int N_KEYS = 500;
    static final int MIN_KEYS = 100;
    public static void main(String[] args) throws Exception {
        String osName = System.getProperty("os.name");
        if (osName.toLowerCase().startsWith("win")) {
            if (!(osName.equals("Windows NT")
                  || osName.equals("Windows 2000")
                  || osName.equals("Windows XP")))
                return;
        }
        ServerSocketChannel ssc = ServerSocketChannel.open();
        TestUtil.bind(ssc);
        Listener lth = new Listener(ssc);
        lth.start();
        Selector sel = Selector.open();
        SocketChannel[] sca = new SocketChannel[N_KEYS];
        for (int i = 0; i < N_KEYS; i++) {
            SocketChannel sc = SocketChannel.open();
            sc.configureBlocking(false);
            sc.register(sel, SelectionKey.OP_CONNECT | SelectionKey.OP_WRITE);
            sc.connect(ssc.socket().getLocalSocketAddress());
        }
        for (int i = 0; i < 10; i++) {
            if (lth.count >= MIN_KEYS)
                break;
            Thread.sleep(1000);
        }
        log.println(lth.count + " connections accepted");
        Thread.sleep(500);
        int n = sel.select();
        log.println(n + " keys selected");
        if (n < MIN_KEYS)
            throw new Exception("Only selected " + n + " keys");
    }
}
