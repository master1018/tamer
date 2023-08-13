public class FinishConnect {
    static final int DAYTIME_PORT = 13;
    static final String DAYTIME_HOST = TestUtil.HOST;
    public static void main(String[] args) throws Exception {
        test1(true, true);
        test1(true, false);
        test1(false, true);
        test1(false, false);
        test2();
    }
    static void test1(boolean select, boolean setBlocking) throws Exception {
        InetSocketAddress isa
            = new InetSocketAddress(InetAddress.getByName(DAYTIME_HOST),
                                    DAYTIME_PORT);
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);
        boolean connected = sc.connect(isa);
        int attempts = 0;
        try {
            sc.connect(isa);
            throw new RuntimeException("Allowed another connect call");
        } catch (IllegalStateException ise) {
        }
        if (setBlocking)
            sc.configureBlocking(true);
        if (!connected && select && !setBlocking) {
            Selector selector = SelectorProvider.provider().openSelector();
            sc.register(selector, SelectionKey.OP_CONNECT);
            while (!connected) {
                int keysAdded = selector.select(100);
                if (keysAdded > 0) {
                    Set readyKeys = selector.selectedKeys();
                    Iterator i = readyKeys.iterator();
                    while (i.hasNext()) {
                        SelectionKey sk = (SelectionKey)i.next();
                        SocketChannel nextReady =
                            (SocketChannel)sk.channel();
                        connected = sc.finishConnect();
                    }
                }
            }
            selector.close();
        }
        while (!connected) {
            if (attempts++ > 30)
                throw new RuntimeException("Failed to connect");
            Thread.sleep(100);
            connected = sc.finishConnect();
        }
        ByteBuffer bb = ByteBuffer.allocateDirect(100);
        int bytesRead = 0;
        int totalRead = 0;
        while (totalRead < 20) {
            bytesRead = sc.read(bb);
            if (bytesRead > 0)
                totalRead += bytesRead;
            if (bytesRead < 0)
                throw new RuntimeException("Message shorter than expected");
        }
        bb.position(bb.position() - 2);         
        bb.flip();
        CharBuffer cb = Charset.forName("US-ASCII").newDecoder().decode(bb);
        System.err.println(isa + " says: \"" + cb + "\"");
        sc.close();
    }
    static void test2() throws Exception {
        InetSocketAddress isa
            = new InetSocketAddress(InetAddress.getByName(DAYTIME_HOST),
                                    DAYTIME_PORT);
        boolean done = false;
        int globalAttempts = 0;
        while (!done) {
            if (globalAttempts++ > 50)
                throw new RuntimeException("Failed to connect");
            SocketChannel sc = SocketChannel.open();
            sc.configureBlocking(false);
            boolean connected = sc.connect(isa);
            int localAttempts = 0;
            while (!connected) {
                if (localAttempts++ > 500)
                    throw new RuntimeException("Failed to connect");
                connected = sc.finishConnect();
                if (connected) {
                    done = true;
                    break;
                }
                Thread.sleep(10);
            }
            sc.close();
        }
    }
}
