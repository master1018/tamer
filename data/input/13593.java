public class OutOfBand {
    private static final Random rand = new Random();
    public static void main(String[] args) throws Exception {
        ServerSocketChannel ssc = null;
        SocketChannel sc1 = null;
        SocketChannel sc2 = null;
        try {
            ssc = ServerSocketChannel.open().bind(new InetSocketAddress(0));
            InetAddress lh = InetAddress.getLocalHost();
            SocketAddress remote =
                new InetSocketAddress(lh, ssc.socket().getLocalPort());
            sc1 = SocketChannel.open(remote);
            sc2 = ssc.accept();
            sc2.socket().setOOBInline(true);
            test1(sc1, sc2);
            test2(sc1, sc2);
            test3(sc1, sc2);
            test4(sc1);
        } finally {
            if (sc1 != null) sc1.close();
            if (sc2 != null) sc2.close();
            if (ssc != null) ssc.close();
        }
    }
    static void test1(SocketChannel client, SocketChannel server)
        throws Exception
    {
        assert server.socket().getOOBInline();
        ByteBuffer bb = ByteBuffer.allocate(100);
        for (int i=0; i<1000; i++) {
            int b1 = -127 + rand.nextInt(384);
            client.socket().sendUrgentData(b1);
            bb.clear();
            if (server.read(bb) != 1)
                throw new RuntimeException("One byte expected");
            bb.flip();
            byte b2 = bb.get();
            if ((byte)b1 != b2)
                throw new RuntimeException("Unexpected byte");
        }
    }
    static void test2(final SocketChannel client, SocketChannel server)
        throws Exception
    {
        assert server.socket().getOOBInline();
        Runnable sender = new Runnable() {
            public void run() {
                try {
                    for (int i=0; i<256; i++)
                        client.socket().sendUrgentData(i);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        };
        Thread thr = new Thread(sender);
        thr.start();
        ByteBuffer bb = ByteBuffer.allocate(256);
        while (bb.hasRemaining()) {
            if (server.read(bb) < 0)
                throw new RuntimeException("Unexpected EOF");
        }
        bb.flip();
        byte expect = 0;
        while (bb.hasRemaining()) {
            if (bb.get() != expect)
                throw new RuntimeException("Unexpected byte");
            expect++;
        }
        thr.join();
    }
    static void test3(SocketChannel client, final SocketChannel server)
        throws Exception
    {
        final int STOP = rand.nextInt(256);
        assert server.socket().getOOBInline();
        Runnable reader = new Runnable() {
            public void run() {
                ByteBuffer bb = ByteBuffer.allocate(100);
                try {
                    int n = server.read(bb);
                    if (n != 1) {
                        String msg = (n < 0) ? "Unexpected EOF" :
                                               "One byte expected";
                        throw new RuntimeException(msg);
                    }
                    bb.flip();
                    if (bb.get() != (byte)STOP)
                        throw new RuntimeException("Unexpected byte");
                    bb.flip();
                    server.write(bb);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        };
        Thread thr = new Thread(reader);
        thr.start();
        client.socket().sendUrgentData(STOP);
        ByteBuffer bb = ByteBuffer.allocate(100);
        int n = client.read(bb);
        if (n != 1)
            throw new RuntimeException("Unexpected number of bytes");
        bb.flip();
        if (bb.get() != (byte)STOP)
            throw new RuntimeException("Unexpected reply");
        thr.join();
    }
    static void test4(SocketChannel sc) throws IOException {
        boolean blocking = sc.isBlocking();
        sc.configureBlocking(false);
        try {
            sc.socket().sendUrgentData(0);
            throw new RuntimeException("IllegalBlockingModeException expected");
        } catch (IllegalBlockingModeException x) {
        } finally {
            sc.configureBlocking(blocking);
        }
    }
}
