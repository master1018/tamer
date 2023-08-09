public class KeysReady {
    static final int DAYTIME_PORT = 13;
    static final String DAYTIME_HOST = TestUtil.HOST;
    static void test() throws Exception {
        InetSocketAddress isa
            = new InetSocketAddress(InetAddress.getByName(DAYTIME_HOST),
                                    DAYTIME_PORT);
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);
        sc.connect(isa);
        Selector selector = SelectorProvider.provider().openSelector();
        try {
            SelectionKey key = sc.register(selector, SelectionKey.OP_CONNECT);
            int keysAdded = selector.select();
            if (keysAdded > 0) {
                keysAdded = selector.select(1000);
                if (keysAdded > 0)
                    throw new Exception("Same key reported added twice");
            }
        } finally {
            selector.close();
            sc.close();
        }
    }
    public static void main(String[] args) throws Exception {
        test();
    }
}
