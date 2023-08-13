public class VectorParams {
    static java.io.PrintStream out = System.out;
    static final int DAYTIME_PORT = 13;
    static final String DAYTIME_HOST = TestUtil.HOST;
    static final int testSize = 10;
    static ByteBuffer[] bufs = null;
    static InetSocketAddress isa = null;
    public static void main(String[] args) throws Exception {
        initBufs();
        testSocketChannelVectorParams();
        testDatagramChannelVectorParams();
        testPipeVectorParams();
        testFileVectorParams();
    }
    static void initBufs() throws Exception {
        bufs = new ByteBuffer[testSize];
        for(int i=0; i<testSize; i++) {
            String source = "buffer" + i;
            bufs[i] = ByteBuffer.allocate(source.length());
            bufs[i].put(source.getBytes("8859_1"));
            bufs[i].flip();
        }
        isa =  new InetSocketAddress(InetAddress.getByName(DAYTIME_HOST),
                                    DAYTIME_PORT);
    }
    static void testSocketChannelVectorParams() throws Exception {
        SocketChannel sc = SocketChannel.open(isa);
        tryBadWrite(sc, bufs, 0, -1);
        tryBadWrite(sc, bufs, -1, 0);
        tryBadWrite(sc, bufs, 0, 1000);
        tryBadWrite(sc, bufs, 1000, 1);
        tryBadRead(sc, bufs, 0, -1);
        tryBadRead(sc, bufs, -1, 0);
        tryBadRead(sc, bufs, 0, 1000);
        tryBadRead(sc, bufs, 1000, 1);
        sc.close();
    }
    static void testDatagramChannelVectorParams() throws Exception {
        DatagramChannel dc = DatagramChannel.open();
        dc.connect(isa);
        tryBadRead(dc, bufs, 0, -1);
        tryBadRead(dc, bufs, -1, 0);
        tryBadRead(dc, bufs, 0, 1000);
        tryBadRead(dc, bufs, 1000, 1);
        tryBadWrite(dc, bufs, 0, -1);
        tryBadWrite(dc, bufs, -1, 0);
        tryBadWrite(dc, bufs, 0, 1000);
        tryBadWrite(dc, bufs, 1000, 1);
        dc.close();
    }
    static void testPipeVectorParams() throws Exception {
        Pipe p = Pipe.open();
        Pipe.SinkChannel sink = p.sink();
        Pipe.SourceChannel source = p.source();
        tryBadWrite(sink, bufs, 0, -1);
        tryBadWrite(sink, bufs, -1, 0);
        tryBadWrite(sink, bufs, 0, 1000);
        tryBadWrite(sink, bufs, 1000, 1);
        tryBadRead(source, bufs, 0, -1);
        tryBadRead(source, bufs, -1, 0);
        tryBadRead(source, bufs, 0, 1000);
        tryBadRead(source, bufs, 1000, 1);
        sink.close();
        source.close();
    }
    static void testFileVectorParams() throws Exception {
        File testFile = File.createTempFile("filevec", null);
        testFile.deleteOnExit();
        RandomAccessFile raf = new RandomAccessFile(testFile, "rw");
        FileChannel fc = raf.getChannel();
        tryBadWrite(fc, bufs, 0, -1);
        tryBadWrite(fc, bufs, -1, 0);
        tryBadWrite(fc, bufs, 0, 1000);
        tryBadWrite(fc, bufs, 1000, 1);
        tryBadRead(fc, bufs, 0, -1);
        tryBadRead(fc, bufs, -1, 0);
        tryBadRead(fc, bufs, 0, 1000);
        tryBadRead(fc, bufs, 1000, 1);
        fc.close();
    }
    private static void tryBadWrite(GatheringByteChannel gbc,
                                    ByteBuffer[] bufs, int offset, int len)
        throws Exception
    {
        try {
            gbc.write(bufs, offset, len);
            throw new RuntimeException("Expected exception not thrown");
        } catch (IndexOutOfBoundsException ioobe) {
        }
    }
    private static void tryBadRead(ScatteringByteChannel sbc,
                                   ByteBuffer[] bufs, int offset, int len)
        throws Exception
    {
        try {
            sbc.read(bufs, offset, len);
            throw new RuntimeException("Expected exception not thrown");
        } catch (IndexOutOfBoundsException ioobe) {
        }
    }
}
