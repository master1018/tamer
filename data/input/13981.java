public class ConnectWrite {
    public static void main(String[] args) throws Exception {
        test1(13);
    }
    public static void test1(int port) throws Exception {
        Selector selector = SelectorProvider.provider().openSelector();
        InetAddress myAddress=InetAddress.getByName(TestUtil.HOST);
        InetSocketAddress isa = new InetSocketAddress(myAddress, port);
        SocketChannel sc = SocketChannel.open();
        try {
            sc.configureBlocking(false);
            SelectionKey key = sc.register(selector, SelectionKey.OP_CONNECT);
            boolean result = sc.connect(isa);
            while (!result) {
                int keysAdded = selector.select(1000);
                if (keysAdded > 0) {
                    Set readyKeys = selector.selectedKeys();
                    Iterator i = readyKeys.iterator();
                    while (i.hasNext()) {
                        SelectionKey sk = (SelectionKey)i.next();
                        readyKeys.remove(sk);
                        SocketChannel nextReady = (SocketChannel)sk.channel();
                        result = nextReady.finishConnect();
                    }
                }
            }
            if (key != null) {
                key.interestOps(SelectionKey.OP_WRITE);
                int keysAdded = selector.select(1000);
                if (keysAdded <= 0)
                    throw new Exception("connect->write failed");
                if (keysAdded > 0) {
                    Set readyKeys = selector.selectedKeys();
                    Iterator i = readyKeys.iterator();
                    while (i.hasNext()) {
                        SelectionKey sk = (SelectionKey)i.next();
                        if (!sk.isWritable())
                            throw new Exception("connect->write failed");
                    }
                }
            }
        } finally {
            sc.close();
            selector.close();
        }
    }
}
