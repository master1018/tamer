public class IsConnected {
    public static void main(String argv[]) throws Exception {
        InetSocketAddress isa = new InetSocketAddress(
            InetAddress.getByName(TestUtil.HOST), 13);
        DatagramChannel dc = DatagramChannel.open();
        dc.configureBlocking(true);
        dc.connect(isa);
        if  (!dc.isConnected())
            throw new RuntimeException("channel.isConnected inconsistent");
        if (!dc.socket().isConnected())
            throw new RuntimeException("socket.isConnected inconsistent");
        dc.close();
    }
}
