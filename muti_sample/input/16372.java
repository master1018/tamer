public class Alias {
    static int success = 0;
    static int LIMIT = 20; 
    public static void main(String[] args) throws Exception {
        test1();
    }
    public static void test1() throws Exception {
        Selector selector = SelectorProvider.provider().openSelector();
        InetAddress myAddress=InetAddress.getByName(TestUtil.HOST);
        InetSocketAddress isa = new InetSocketAddress(myAddress,13);
        for (int j=0; j<LIMIT; j++) {
            SocketChannel sc = SocketChannel.open();
            sc.configureBlocking(false);
            boolean result = sc.connect(isa);
            if (!result) {
                SelectionKey key = sc.register(selector,
                                               SelectionKey.OP_CONNECT);
                while (!result) {
                    int keysAdded = selector.select(100);
                    if (keysAdded > 0) {
                        Set readyKeys = selector.selectedKeys();
                        Iterator i = readyKeys.iterator();
                        while (i.hasNext()) {
                            SelectionKey sk = (SelectionKey)i.next();
                            SocketChannel nextReady =
                                (SocketChannel)sk.channel();
                            result = nextReady.finishConnect();
                        }
                    }
                }
                key.cancel();
            }
            read(sc);
        }
        selector.close();
    }
    static void read(SocketChannel sc) throws Exception {
        ByteBuffer bb = ByteBuffer.allocateDirect(100);
        int n = 0;
        while (n == 0) 
            n = sc.read(bb);
        sc.close();
        success++;
    }
}
