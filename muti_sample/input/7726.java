public class Refused {
    static ByteBuffer outBuf = ByteBuffer.allocateDirect(100);
    static ByteBuffer inBuf  = ByteBuffer.allocateDirect(100);
    static DatagramChannel client;
    static DatagramChannel server;
    static InetSocketAddress isa;
    public static void main(String[] args) throws Exception {
        outBuf.put("Blah Blah".getBytes());
        outBuf.flip();
        test1();
        if ((args.length > 0) && (args[0].equals("test2"))) {
            outBuf.rewind();
            test2();
        }
    }
    public static void setup() throws Exception {
        client = DatagramChannel.open();
        server = DatagramChannel.open();
        client.socket().bind((SocketAddress)null);
        server.socket().bind((SocketAddress)null);
        client.configureBlocking(false);
        server.configureBlocking(false);
        InetAddress address = InetAddress.getLocalHost();
        int port = client.socket().getLocalPort();
        isa = new InetSocketAddress(address, port);
    }
    public static void test1() throws Exception {
        setup();
        server.send(outBuf, isa);
        server.receive(inBuf);
        client.close();
        outBuf.rewind();
        server.send(outBuf, isa);
        server.receive(inBuf);
        server.close();
    }
    public static void test2() throws Exception {
        setup();
        server.configureBlocking(true);
        server.connect(isa);
        server.configureBlocking(false);
        outBuf.rewind();
        server.write(outBuf);
        server.receive(inBuf);
        client.close();
        Thread.sleep(2000);
        outBuf.rewind();
        try {
            server.write(outBuf);
            Thread.sleep(2000);
            inBuf.clear();
            server.read(inBuf);
            if (onSolarisOrLinux())
                throw new Exception("Expected PUE not thrown");
        } catch (PortUnreachableException pue) {
            System.err.println("received PUE");
        }
        server.close();
    }
    static boolean onSolarisOrLinux() {
        String osName = System.getProperty("os.name");
        return osName.startsWith("SunOS") || osName.startsWith("Linux");
    }
}
