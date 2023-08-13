public class IsConnectable {
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
                boolean result = sc.finishConnect();
                if (result) {
                    keysAdded = selector.select(5000);
                    if (keysAdded > 0)
                        throw new Exception("Test failed: 4750573 detected");
                    Set<SelectionKey> sel = selector.selectedKeys();
                    Iterator<SelectionKey> i = sel.iterator();
                    SelectionKey sk = i.next();
                    if (sk.isConnectable())
                        throw new Exception("Test failed: 4737146 detected");
                }
            } else {
                throw new Exception("Select failed");
            }
        } finally {
            sc.close();
            selector.close();
        }
    }
    public static void main(String[] args) throws Exception {
        test();
    }
}
