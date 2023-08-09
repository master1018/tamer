public class Transfer {
    private static Random generator = new Random();
    private static int[] testSizes = {
        0, 10, 1023, 1024, 1025, 2047, 2048, 2049 };
    public static void main(String[] args) throws Exception {
        testFileChannel();
        for (int i=0; i<testSizes.length; i++)
            testReadableByteChannel(testSizes[i]);
        xferTest02(); 
        xferTest03(); 
        xferTest04(); 
        xferTest05(); 
        xferTest06(); 
        xferTest07(); 
        xferTest08(); 
        xferTest09(); 
    }
    private static void testFileChannel() throws Exception {
        File source = File.createTempFile("source", null);
        source.deleteOnExit();
        File sink = File.createTempFile("sink", null);
        sink.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(source);
        FileChannel sourceChannel = fos.getChannel();
        sourceChannel.write(ByteBuffer.wrap(
            "Use the source, Luke!".getBytes()));
        sourceChannel.close();
        FileInputStream fis = new FileInputStream(source);
        sourceChannel = fis.getChannel();
        RandomAccessFile raf = new RandomAccessFile(sink, "rw");
        FileChannel sinkChannel = raf.getChannel();
        long oldSinkPosition = sinkChannel.position();
        long oldSourcePosition = sourceChannel.position();
        long bytesWritten = sinkChannel.transferFrom(sourceChannel, 0, 10);
        if (bytesWritten != 10)
            throw new RuntimeException("Transfer failed");
        if (sourceChannel.position() == oldSourcePosition)
            throw new RuntimeException("Source position didn't change");
        if (sinkChannel.position() != oldSinkPosition)
            throw new RuntimeException("Sink position changed");
        if (sinkChannel.size() != 10)
            throw new RuntimeException("Unexpected sink size");
        bytesWritten = sinkChannel.transferFrom(sourceChannel, 1000, 10);
        if (bytesWritten > 0)
            throw new RuntimeException("Wrote past file size");
        sourceChannel.close();
        sinkChannel.close();
        source.delete();
        sink.delete();
    }
    private static void testReadableByteChannel(int size) throws Exception {
        SelectorProvider sp = SelectorProvider.provider();
        Pipe p = sp.openPipe();
        Pipe.SinkChannel sink = p.sink();
        Pipe.SourceChannel source = p.source();
        sink.configureBlocking(false);
        ByteBuffer outgoingdata = ByteBuffer.allocateDirect(size + 10);
        byte[] someBytes = new byte[size + 10];
        generator.nextBytes(someBytes);
        outgoingdata.put(someBytes);
        outgoingdata.flip();
        int totalWritten = 0;
        while (totalWritten < size + 10) {
            int written = sink.write(outgoingdata);
            if (written < 0)
                throw new Exception("Write failed");
            totalWritten += written;
        }
        File f = File.createTempFile("blah"+size, null);
        f.deleteOnExit();
        RandomAccessFile raf = new RandomAccessFile(f, "rw");
        FileChannel fc = raf.getChannel();
        long oldPosition = fc.position();
        long bytesWritten = fc.transferFrom(source, 0, size);
        fc.force(true);
        if (bytesWritten != size)
            throw new RuntimeException("Transfer failed");
        if (fc.position() != oldPosition)
            throw new RuntimeException("Position changed");
        if (fc.size() != size)
            throw new RuntimeException("Unexpected sink size "+ fc.size());
        fc.close();
        sink.close();
        source.close();
        f.delete();
    }
    public static void xferTest02() throws Exception {
        byte[] srcData = new byte[5000];
        for (int i=0; i<5000; i++)
            srcData[i] = (byte)generator.nextInt();
        File source = File.createTempFile("source", null);
        source.deleteOnExit();
        RandomAccessFile raf1 = new RandomAccessFile(source, "rw");
        FileChannel fc1 = raf1.getChannel();
        long bytesWritten = 0;
        while (bytesWritten < 5000) {
            bytesWritten = fc1.write(ByteBuffer.wrap(srcData));
        }
        File dest = File.createTempFile("dest", null);
        dest.deleteOnExit();
        RandomAccessFile raf2 = new RandomAccessFile(dest, "rw");
        FileChannel fc2 = raf2.getChannel();
        int bytesToWrite = 3000;
        int startPosition = 1000;
        bytesWritten = fc1.transferTo(startPosition, bytesToWrite, fc2);
        fc1.close();
        fc2.close();
        raf1.close();
        raf2.close();
        source.delete();
        dest.delete();
    }
    public static void xferTest03() throws Exception {
        byte[] srcData = new byte[] {1,2,3,4} ;
        File source = File.createTempFile("source", null);
        source.deleteOnExit();
        RandomAccessFile raf1 = new RandomAccessFile(source, "rw");
        FileChannel fc1 = raf1.getChannel();
        fc1.truncate(0);
        int bytesWritten = 0;
        while (bytesWritten < 4) {
            bytesWritten = fc1.write(ByteBuffer.wrap(srcData));
        }
        File dest = File.createTempFile("dest", null);
        dest.deleteOnExit();
        RandomAccessFile raf2 = new RandomAccessFile(dest, "rw");
        FileChannel fc2 = raf2.getChannel();
        fc2.truncate(0);
        fc1.transferTo(0, srcData.length + 1, fc2);
        if (fc2.size() > 4)
            throw new Exception("xferTest03 failed");
        fc1.close();
        fc2.close();
        raf1.close();
        raf2.close();
        source.delete();
        dest.delete();
    }
    public static void xferTest04() throws Exception {
        String osName = System.getProperty("os.name");
        if (!osName.startsWith("SunOS"))
            return;
        File source = File.createTempFile("blah", null);
        source.deleteOnExit();
        long testSize = ((long)Integer.MAX_VALUE) * 2;
        initTestFile(source, 10);
        RandomAccessFile raf = new RandomAccessFile(source, "rw");
        FileChannel fc = raf.getChannel();
        fc.write(ByteBuffer.wrap("Use the source!".getBytes()), testSize - 40);
        fc.close();
        raf.close();
        File sink = File.createTempFile("sink", null);
        sink.deleteOnExit();
        FileInputStream fis = new FileInputStream(source);
        FileChannel sourceChannel = fis.getChannel();
        raf = new RandomAccessFile(sink, "rw");
        FileChannel sinkChannel = raf.getChannel();
        long bytesWritten = sourceChannel.transferTo(testSize -40, 10,
                                                     sinkChannel);
        if (bytesWritten != 10) {
            throw new RuntimeException("Transfer test 4 failed " +
                                       bytesWritten);
        }
        sourceChannel.close();
        sinkChannel.close();
        source.delete();
        sink.delete();
    }
    public static void xferTest05() throws Exception {
        File source = File.createTempFile("blech", null);
        source.deleteOnExit();
        initTestFile(source, 100);
        File sink = null;
        FileChannel fc = null;
        while (fc == null) {
            sink = File.createTempFile("sink", null);
            sink.delete();
            try {
                fc = FileChannel.open(sink.toPath(),
                                      StandardOpenOption.CREATE_NEW,
                                      StandardOpenOption.WRITE,
                                      StandardOpenOption.SPARSE);
            } catch (FileAlreadyExistsException ignore) {
            }
        }
        sink.deleteOnExit();
        long testSize = ((long)Integer.MAX_VALUE) * 2;
        try {
            fc.write(ByteBuffer.wrap("Use the source!".getBytes()),
                     testSize - 40);
        } catch (IOException e) {
            System.err.println("xferTest05 was aborted.");
            return;
        } finally {
            fc.close();
        }
        FileChannel sourceChannel = new FileInputStream(source).getChannel();
        try {
            FileChannel sinkChannel = new RandomAccessFile(sink, "rw").getChannel();
            try {
                long bytesWritten = sinkChannel.transferFrom(sourceChannel,
                                                             testSize - 40, 10);
                if (bytesWritten != 10) {
                    throw new RuntimeException("Transfer test 5 failed " +
                                               bytesWritten);
                }
            } finally {
                sinkChannel.close();
            }
        } finally {
            sourceChannel.close();
        }
        source.delete();
        sink.delete();
    }
    static void checkFileData(File file, String expected) throws Exception {
        FileInputStream fis = new FileInputStream(file);
        Reader r = new BufferedReader(new InputStreamReader(fis, "ASCII"));
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = r.read()) != -1)
            sb.append((char)c);
        String contents = sb.toString();
        if (! contents.equals(expected))
            throw new Exception("expected: " + expected
                                + ", got: " + contents);
        r.close();
    }
    public static void xferTest06() throws Exception {
        String data = "Use the source, Luke!";
        File source = File.createTempFile("source", null);
        source.deleteOnExit();
        File sink = File.createTempFile("sink", null);
        sink.deleteOnExit();
        FileOutputStream fos = new FileOutputStream(source);
        fos.write(data.getBytes("ASCII"));
        fos.close();
        FileChannel sourceChannel =
            new RandomAccessFile(source, "rw").getChannel();
        sourceChannel.position(7);
        long remaining = sourceChannel.size() - sourceChannel.position();
        FileChannel sinkChannel =
            new RandomAccessFile(sink, "rw").getChannel();
        long n = sinkChannel.transferFrom(sourceChannel, 0L,
                                          sourceChannel.size()); 
        if (n != remaining)
            throw new Exception("n == " + n + ", remaining == " + remaining);
        sinkChannel.close();
        sourceChannel.close();
        checkFileData(source, data);
        checkFileData(sink, data.substring(7,data.length()));
        source.delete();
    }
    public static void xferTest07() throws Exception {
        File source = File.createTempFile("source", null);
        source.deleteOnExit();
        FileChannel sourceChannel = new RandomAccessFile(source, "rw")
            .getChannel();
        sourceChannel.position(32000L)
            .write(ByteBuffer.wrap("The End".getBytes()));
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(new InetSocketAddress(0));
        InetSocketAddress sa = new InetSocketAddress(
            InetAddress.getLocalHost(), ssc.socket().getLocalPort());
        SocketChannel sink = SocketChannel.open(sa);
        sink.configureBlocking(false);
        SocketChannel other = ssc.accept();
        long size = sourceChannel.size();
        long n;
        do {
            n = sourceChannel.transferTo(0, size, sink);
        } while (n > 0);
        sourceChannel.close();
        sink.close();
        other.close();
        ssc.close();
        source.delete();
    }
    public static void xferTest08() throws Exception {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Windows"))
            return;
        final long G = 1024L * 1024L * 1024L;
        File file = File.createTempFile("source", null);
        file.deleteOnExit();
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        FileChannel fc = raf.getChannel();
        try {
            fc.write(ByteBuffer.wrap("0123456789012345".getBytes("UTF-8")), 6*G);
        } catch (IOException x) {
            System.err.println("Unable to create test file:" + x);
            fc.close();
            return;
        }
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.socket().bind(new InetSocketAddress(0));
        InetAddress lh = InetAddress.getLocalHost();
        InetSocketAddress isa = new InetSocketAddress(lh, ssc.socket().getLocalPort());
        SocketChannel source = SocketChannel.open(isa);
        SocketChannel sink = ssc.accept();
        Thread thr = new Thread(new EchoServer(sink));
        thr.start();
        long testdata[][] = {
            { 2*G-1,    1 },
            { 2*G-1,    10 },       
            { 2*G,      1 },
            { 2*G,      10 },
            { 2*G+1,    1 },
            { 4*G-1,    1 },
            { 4*G-1,    10 },       
            { 4*G,      1 },
            { 4*G,      10 },
            { 4*G+1,    1 },
            { 5*G-1,    1 },
            { 5*G-1,    10 },
            { 5*G,      1 },
            { 5*G,      10 },
            { 5*G+1,    1 },
            { 6*G,      1 },
        };
        ByteBuffer sendbuf = ByteBuffer.allocateDirect(100);
        ByteBuffer readbuf = ByteBuffer.allocateDirect(100);
        try {
            byte value = 0;
            for (int i=0; i<testdata.length; i++) {
                long position = testdata[(int)i][0];
                long count = testdata[(int)i][1];
                for (long j=0; j<count; j++) {
                    sendbuf.put(++value);
                }
                sendbuf.flip();
                fc.write(sendbuf, position);
                fc.transferTo(position, count, source);
                long nread = 0;
                while (nread < count) {
                    int n = source.read(readbuf);
                    if (n < 0)
                        throw new RuntimeException("Premature EOF!");
                    nread += n;
                }
                readbuf.flip();
                sendbuf.flip();
                if (!readbuf.equals(sendbuf))
                    throw new RuntimeException("Echo'ed bytes do not match!");
                readbuf.clear();
                sendbuf.clear();
            }
        } finally {
            source.close();
            ssc.close();
            fc.close();
            file.delete();
        }
    }
    static void xferTest09() throws Exception {
        File source = File.createTempFile("source", null);
        source.deleteOnExit();
        File target = File.createTempFile("target", null);
        target.deleteOnExit();
        FileChannel fc1 = new FileOutputStream(source).getChannel();
        FileChannel fc2 = new RandomAccessFile(target, "rw").getChannel();
        try {
            fc2.transferFrom(fc1, 0L, 0);
            throw new RuntimeException("NonReadableChannelException expected");
        } catch (NonReadableChannelException expected) {
        } finally {
            fc1.close();
            fc2.close();
        }
    }
    private static void initTestFile(File blah, long size) throws Exception {
        if (blah.exists())
            blah.delete();
        FileOutputStream fos = new FileOutputStream(blah);
        BufferedWriter awriter
            = new BufferedWriter(new OutputStreamWriter(fos, "8859_1"));
        for(int i=0; i<size; i++) {
            awriter.write("e");
        }
        awriter.flush();
        awriter.close();
    }
    static class EchoServer implements Runnable {
        private SocketChannel sc;
        public EchoServer(SocketChannel sc) {
            this.sc = sc;
        }
        public void run() {
            ByteBuffer bb = ByteBuffer.allocateDirect(1024);
            try {
                for (;;) {
                    int n = sc.read(bb);
                    if (n < 0)
                        break;
                    bb.flip();
                    while (bb.remaining() > 0) {
                        sc.write(bb);
                    }
                    bb.clear();
                }
            } catch (IOException x) {
                x.printStackTrace();
            } finally {
                try {
                    sc.close();
                } catch (IOException ignore) { }
            }
        }
    }
}
