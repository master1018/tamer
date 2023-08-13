public class NoSender {
    public static void main(String argv[]) throws Exception {
        DatagramChannel dc = DatagramChannel.open();
        dc.socket().bind(new InetSocketAddress(0));
        dc.configureBlocking(false);
        ByteBuffer buf1 = ByteBuffer.allocateDirect(256);
        SocketAddress sa1 = dc.receive(buf1);
        if (sa1 != null)
            throw new RuntimeException("Test failed");
        dc.close();
    }
}
