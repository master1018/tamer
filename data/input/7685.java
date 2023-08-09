public class AdaptDatagramSocket {
    static java.io.PrintStream out = System.out;
    static Random rand = new Random();
    static final int ECHO_PORT = 7;
    static final int DISCARD_PORT = 9;
    static final String REMOTE_HOST = TestUtil.HOST;
    static final InetSocketAddress echoAddress
        = new InetSocketAddress(REMOTE_HOST, ECHO_PORT);
    static final InetSocketAddress discardAddress
        = new InetSocketAddress(REMOTE_HOST, DISCARD_PORT);
    static String toString(DatagramPacket dp) {
        return ("DatagramPacket[off=" + dp.getOffset()
                + ", len=" + dp.getLength()
                + "]");
    }
    static void test(DatagramSocket ds, InetSocketAddress dst,
                     boolean shouldTimeout)
        throws Exception
    {
        DatagramPacket op = new DatagramPacket(new byte[100], 13, 42, dst);
        rand.nextBytes(op.getData());
        DatagramPacket ip = new DatagramPacket(new byte[100], 19, 100 - 19);
        out.println("pre  op: " + toString(op) + "  ip: " + toString(ip));
        long start = System.currentTimeMillis();
        ds.send(op);
        for (;;) {
            try {
                ds.receive(ip);
                if (ip.getLength() == 0) { 
                    ip.setLength(100 - 19);
                    continue;
                }
            } catch (SocketTimeoutException x) {
                if (shouldTimeout) {
                    out.println("Receive timed out, as expected");
                    return;
                }
                throw x;
            }
            break;
        }
        out.println("rtt: " + (System.currentTimeMillis() - start));
        out.println("post op: " + toString(op) + "  ip: " + toString(ip));
        for (int i = 0; i < ip.getLength(); i++)
            if (ip.getData()[ip.getOffset() + i]
                != op.getData()[op.getOffset() + i])
                throw new Exception("Incorrect data received");
        if (!(ip.getSocketAddress().equals(dst))) {
            throw new Exception("Incorrect sender address, expected: " + dst
                + " actual: " + ip.getSocketAddress());
        }
    }
    static void test(InetSocketAddress dst,
                     int timeout, boolean shouldTimeout,
                     boolean connect)
        throws Exception
    {
        out.println();
        out.println("dst: " + dst);
        DatagramSocket ds;
        if (false) {
            ds = new DatagramSocket();
        } else {
            DatagramChannel dc = DatagramChannel.open();
            ds = dc.socket();
            ds.bind(new InetSocketAddress(0));
        }
        out.println("socket: " + ds);
        if (connect) {
            ds.connect(dst);
            out.println("connect: " + ds);
        }
        InetSocketAddress src = new InetSocketAddress(ds.getLocalAddress(),
                                                      ds.getLocalPort());
        out.println("src: " + src);
        if (timeout > 0)
            ds.setSoTimeout(timeout);
        out.println("timeout: " + ds.getSoTimeout());
        for (int i = 0; i < 5; i++)
            test(ds, dst, shouldTimeout);
    }
    public static void main(String[] args) throws Exception {
        test(echoAddress, 0, false, false);
        test(echoAddress, 0, false, true);
        test(echoAddress, 5000, false, false);
        test(discardAddress, 10, true, false);
    }
}
