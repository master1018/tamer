public class EchoTest {
    private static int failures = 0;
    private static String ECHO_SERVICE = "EchoService";
    private static void TCPEchoTest() throws IOException {
        SocketChannel sc = Launcher.launchWithSocketChannel(ECHO_SERVICE, null);
        String msg = "Where's that damn torpedo?";
        int repeat = 100;
        int size = msg.length() * repeat;
        ByteBuffer bb1 = ByteBuffer.allocate(size);
        Random gen = new Random();
        for (int i=0; i<repeat; i++) {
            bb1.put(msg.getBytes("UTF-8"));
        }
        bb1.flip();
        sc.write(bb1);
        ByteBuffer bb2 = ByteBuffer.allocate(size+100);
        sc.configureBlocking(false);
        Selector sel = sc.provider().openSelector();
        SelectionKey sk = sc.register(sel, SelectionKey.OP_READ);
        int nread = 0;
        long to = 5000;
        while (nread < size) {
            long st = System.currentTimeMillis();
            sel.select(to);
            if (sk.isReadable()) {
                int n = sc.read(bb2);
                if (n > 0) {
                    nread += n;
                }
                if (n < 0) {
                    break;              
                }
            }
            sel.selectedKeys().remove(sk);
            to -= System.currentTimeMillis() - st;
            if (to <= 0) {
                break;
            }
        }
        sc.close();
        boolean err = false;
        if (nread != size) {
            err = true;
        } else {
            bb1.flip();
            bb2.flip();
            while (bb1.hasRemaining()) {
                if (bb1.get() != bb2.get()) {
                    err = true;
                }
            }
        }
        if (err) {
            System.err.println("Bad response or premature EOF, bytes read: ");
            bb2.flip();
            while (bb2.hasRemaining()) {
                char c = (char)bb2.get();
                System.out.print(c);
            }
            throw new RuntimeException("Bad response or premature EOF from service");
        }
    }
    private static void UDPEchoTest() throws IOException {
        DatagramChannel dc = Launcher.launchWithDatagramChannel(ECHO_SERVICE, null);
        String msg = "I was out saving the galaxy when your grandfather was in diapers";
        ByteBuffer bb = ByteBuffer.wrap(msg.getBytes("UTF-8"));
        dc.write(bb);
        byte b[] = new byte[msg.length() + 100];
        DatagramPacket pkt2 = new DatagramPacket(b, b.length);
        dc.socket().setSoTimeout(5000);
        dc.socket().receive(pkt2);
        if (pkt2.getLength() != msg.length()) {
            throw new RuntimeException("Received packet of incorrect length");
        }
        dc.close();
    }
    public static void main(String args[]) throws IOException {
        try {
            TCPEchoTest();
            System.out.println("TCP echo test passed.");
        } catch (Exception x) {
            System.err.println(x);
            failures++;
        }
        try {
            UDPEchoTest();
            System.out.println("UDP echo test passed.");
        } catch (Exception x) {
            x.printStackTrace();
            System.err.println(x);
            failures++;
        }
        if (failures > 0) {
            throw new RuntimeException("Test failed - see log for details");
        }
    }
}
