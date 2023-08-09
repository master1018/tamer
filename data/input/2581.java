public class SRTest {
    static PrintStream log = System.err;
    public static void main(String[] args) throws Exception {
        test();
    }
    static void test() throws Exception {
        ClassicReader classicReader;
        NioReader nioReader;
        classicReader = new ClassicReader();
        invoke(classicReader, new ClassicWriter(classicReader.port()));
        log.println("Classic RW: OK");
        classicReader = new ClassicReader();
        invoke(classicReader, new NioWriter(classicReader.port()));
        log.println("Classic R, Nio W: OK");
        nioReader = new NioReader();
        invoke(nioReader, new ClassicWriter(nioReader.port()));
        log.println("Classic W, Nio R: OK");
        nioReader = new NioReader();
        invoke(nioReader, new NioWriter(nioReader.port()));
        log.println("Nio RW: OK");
    }
    static void invoke(Sprintable reader, Sprintable writer) throws Exception {
        Thread readerThread = new Thread(reader);
        readerThread.start();
        Thread.sleep(50);
        Thread writerThread = new Thread(writer);
        writerThread.start();
        writerThread.join();
        readerThread.join();
        reader.throwException();
        writer.throwException();
    }
    public interface Sprintable extends Runnable {
        public void throwException() throws Exception;
    }
    public static class ClassicWriter implements Sprintable {
        final int port;
        Exception e = null;
        ClassicWriter(int port) {
            this.port = port;
        }
        public void throwException() throws Exception {
            if (e != null)
                throw e;
        }
        public void run() {
            try {
                DatagramSocket ds = new DatagramSocket();
                String dataString = "hello";
                byte[] data = dataString.getBytes();
                InetAddress address = InetAddress.getLocalHost();
                DatagramPacket dp = new DatagramPacket(data, data.length,
                                                       address, port);
                ds.send(dp);
                Thread.sleep(50);
                ds.send(dp);
            } catch (Exception ex) {
                e = ex;
            }
        }
    }
    public static class NioWriter implements Sprintable {
        final int port;
        Exception e = null;
        NioWriter(int port) {
            this.port = port;
        }
        public void throwException() throws Exception {
            if (e != null)
                throw e;
        }
        public void run() {
            try {
                DatagramChannel dc = DatagramChannel.open();
                ByteBuffer bb = ByteBuffer.allocateDirect(256);
                bb.put("hello".getBytes());
                bb.flip();
                InetAddress address = InetAddress.getLocalHost();
                InetSocketAddress isa = new InetSocketAddress(address, port);
                dc.send(bb, isa);
                Thread.sleep(50);
                dc.send(bb, isa);
            } catch (Exception ex) {
                e = ex;
            }
        }
    }
    public static class ClassicReader implements Sprintable {
        final DatagramSocket ds;
        Exception e = null;
        ClassicReader() throws IOException {
            this.ds = new DatagramSocket();
        }
        int port() {
            return ds.getLocalPort();
        }
        public void throwException() throws Exception {
            if (e != null)
                throw e;
        }
        public void run() {
            try {
                byte[] buf = new byte[256];
                DatagramPacket dp = new DatagramPacket(buf, buf.length);
                ds.receive(dp);
                String received = new String(dp.getData());
                log.println(received);
                ds.close();
            } catch (Exception ex) {
                e = ex;
            }
        }
    }
    public static class NioReader implements Sprintable {
        final DatagramChannel dc;
        Exception e = null;
        NioReader() throws IOException {
            this.dc = DatagramChannel.open().bind(new InetSocketAddress(0));
        }
        int port() {
            return dc.socket().getLocalPort();
        }
        public void throwException() throws Exception {
            if (e != null)
                throw e;
        }
        public void run() {
            try {
                ByteBuffer bb = ByteBuffer.allocateDirect(100);
                SocketAddress sa = dc.receive(bb);
                bb.flip();
                CharBuffer cb = Charset.forName("US-ASCII").
                    newDecoder().decode(bb);
                log.println("From: "+sa+ " said " +cb);
                dc.close();
            } catch (Exception ex) {
                e = ex;
            }
        }
    }
}
