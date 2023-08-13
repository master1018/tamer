public class VectorIO {
    static Random generator = new Random();
    static int testSize;
    public static void main(String[] args) throws Exception {
        testSize = 1;
        runTest();
        for(int i=15; i<18; i++) {
            testSize = i;
            runTest();
        }
    }
    static void runTest() throws Exception {
        System.err.println("Length " + testSize);
        Server sv = new Server(testSize);
        sv.start();
        bufferTest(sv.port());
        if (sv.finish(8000) == 0)
            throw new Exception("Failed: Length = " + testSize);
    }
    static void bufferTest(int port) throws Exception {
        ByteBuffer[] bufs = new ByteBuffer[testSize];
        long total = 0L;
        for(int i=0; i<testSize; i++) {
            String source = "buffer" + i;
            if (generator.nextBoolean())
                bufs[i] = ByteBuffer.allocateDirect(source.length());
            else
                bufs[i] = ByteBuffer.allocate(source.length());
            bufs[i].put(source.getBytes("8859_1"));
            bufs[i].flip();
            total += bufs[i].remaining();
        }
        InetAddress lh = InetAddress.getLocalHost();
        InetSocketAddress isa = new InetSocketAddress(lh, port);
        SocketChannel sc = SocketChannel.open();
        sc.connect(isa);
        sc.configureBlocking(generator.nextBoolean());
        long rem = total;
        while (rem > 0L) {
            long bytesWritten = sc.write(bufs);
            if (bytesWritten == 0) {
                if (sc.isBlocking())
                    throw new RuntimeException("write did not block");
                Thread.sleep(50);
            } else {
                rem -= bytesWritten;
            }
        }
        sc.close();
    }
    static class Server
        extends TestThread
    {
        static Random generator = new Random();
        final int testSize;
        final ServerSocketChannel ssc;
        Server(int testSize) throws IOException {
            super("Server " + testSize);
            this.testSize = testSize;
            this.ssc = ServerSocketChannel.open().bind(new InetSocketAddress(0));
        }
        int port() {
            return ssc.socket().getLocalPort();
        }
        void go() throws Exception {
            bufferTest();
        }
        void bufferTest() throws Exception {
            long total = 0L;
            ByteBuffer[] bufs = new ByteBuffer[testSize];
            for(int i=0; i<testSize; i++) {
                String source = "buffer" + i;
                if (generator.nextBoolean())
                    bufs[i] = ByteBuffer.allocateDirect(source.length());
                else
                    bufs[i] = ByteBuffer.allocate(source.length());
                total += bufs[i].capacity();
            }
            SocketChannel sc = null;
            try {
                ssc.configureBlocking(false);
                for (;;) {
                    sc = ssc.accept();
                    if (sc != null)
                        break;
                    Thread.sleep(50);
                }
                sc.configureBlocking(generator.nextBoolean());
                long avail = total;
                while (avail > 0) {
                    long bytesRead = sc.read(bufs);
                    if (bytesRead < 0)
                        break;
                    if (bytesRead == 0) {
                        if (sc.isBlocking())
                            throw new RuntimeException("read did not block");
                        Thread.sleep(50);
                    }
                    avail -= bytesRead;
                }
                for(int i=0; i<testSize; i++) {
                    String expected = "buffer" + i;
                    bufs[i].flip();
                    int size = bufs[i].capacity();
                    byte[] data = new byte[size];
                    for(int j=0; j<size; j++)
                        data[j] = bufs[i].get();
                    String message = new String(data, "8859_1");
                    if (!message.equals(expected))
                        throw new Exception("Wrong data: Got "
                                            + message + ", expected "
                                            + expected);
                }
            } finally {
                ssc.close();
                if (sc != null)
                    sc.close();
            }
        }
    }
}
