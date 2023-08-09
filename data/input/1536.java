public class HelperSlowToDie {
    private static final int CHANNELS_PER_THREAD = 1023;
    private static final int TEST_ITERATIONS = 200;
    private static volatile boolean done;
    public static void main(String[] args) throws IOException {
        if (!System.getProperty("os.name").startsWith("Windows")) {
            System.out.println("Test skipped as it verifies a Windows specific bug");
            return;
        }
        Selector sel = Selector.open();
        SocketChannel[] channels = new SocketChannel[CHANNELS_PER_THREAD];
        for (int i=0; i<CHANNELS_PER_THREAD; i++) {
            SocketChannel sc = SocketChannel.open();
            sc.configureBlocking(false);
            sc.register(sel, SelectionKey.OP_CONNECT);
            channels[i] = sc;
        }
        sel.selectNow();
        Runnable busy = new Runnable() {
            public void run() {
                while (!done) ;  
            }
        };
        int ncores = Runtime.getRuntime().availableProcessors();
        for (int i=0; i<ncores-1; i++)
            new Thread(busy).start();
        for (int i=0; i<TEST_ITERATIONS; i++) {
            SocketChannel sc = SocketChannel.open();
            sc.configureBlocking(false);
            sc.register(sel, SelectionKey.OP_CONNECT);
            sel.selectNow();   
            sc.close();
            sel.selectNow();  
        }
        done = true;
        for (int i=0; i<CHANNELS_PER_THREAD; i++) {
            channels[i].close();
        }
        sel.close();
    }
}
