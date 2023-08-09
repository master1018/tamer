public class BasicConnect {
    static final int PORT = 7;          
    static final String HOST = TestUtil.HOST;
    public static void main(String[] args) throws Exception {
        Selector connectSelector =
            SelectorProvider.provider().openSelector();
        InetSocketAddress isa
            = new InetSocketAddress(InetAddress.getByName(HOST), PORT);
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);
        boolean result = sc.connect(isa);
        while (!result) {
            SelectionKey connectKey = sc.register(connectSelector,
                                                  SelectionKey.OP_CONNECT);
            int keysAdded = connectSelector.select();
            if (keysAdded > 0) {
                Set readyKeys = connectSelector.selectedKeys();
                Iterator i = readyKeys.iterator();
                while (i.hasNext()) {
                    SelectionKey sk = (SelectionKey)i.next();
                    i.remove();
                    SocketChannel nextReady = (SocketChannel)sk.channel();
                    result = nextReady.finishConnect();
                    if (result)
                        sk.cancel();
                }
            }
        }
        byte[] bs = new byte[] { (byte)0xca, (byte)0xfe,
                                 (byte)0xba, (byte)0xbe };
        ByteBuffer bb = ByteBuffer.wrap(bs);
        sc.configureBlocking(true);
        sc.write(bb);
        bb.rewind();
        ByteBuffer bb2 = ByteBuffer.allocateDirect(100);
        int n = sc.read(bb2);
        bb2.flip();
        sc.close();
        connectSelector.close();
        if (!bb.equals(bb2))
            throw new Exception("Echoed bytes incorrect: Sent "
                                + bb + ", got " + bb2);
    }
}
