public class StateTestService {
    static boolean failed = false;
    static int reply_port;
    static void check(boolean okay) {
        if (!okay) {
            failed = true;
        }
    }
    private static void reply(String msg) throws IOException {
        InetSocketAddress isa = new InetSocketAddress(InetAddress.getLocalHost(), reply_port);
        SocketChannel sc = SocketChannel.open(isa);
        byte b[] = msg.getBytes("UTF-8");
        ByteBuffer bb = ByteBuffer.wrap(b);
        sc.write(bb);
        sc.close();
    }
    public static void main(String args[]) throws IOException {
        if (args.length == 0) {
            System.err.println("Usage: StateTestService [reply-port]");
            return;
        }
        reply_port = Integer.parseInt(args[0]);
        Channel c = null;
        try {
            c = System.inheritedChannel();
        } catch (SecurityException se) {
        }
        if (c == null) {
            reply("FAILED");
            return;
        }
        if (c instanceof SocketChannel) {
            SocketChannel sc = (SocketChannel)c;
            check( sc.isBlocking() );
            check( sc.socket().isBound() );
            check( sc.socket().isConnected() );
        }
        if (c instanceof ServerSocketChannel) {
            ServerSocketChannel ssc = (ServerSocketChannel)c;
            check( ssc.isBlocking() );
            check( ssc.socket().isBound() );
        }
        if (c instanceof DatagramChannel) {
            DatagramChannel dc = (DatagramChannel)c;
            check( dc.isBlocking() );
            check( dc.socket().isBound() );
        }
        if (failed) {
            reply("FAILED");
        } else {
            reply("PASSED");
        }
    }
}
