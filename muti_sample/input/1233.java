public class SendToUnresolved {
    public static void main(String [] argv) throws Exception {
        String host = TestUtil.UNRESOLVABLE_HOST;
        DatagramChannel dc = DatagramChannel.open();
        ByteBuffer bb = ByteBuffer.allocate(4);
        InetSocketAddress sa = new InetSocketAddress (host, 37);
        InetAddress inetaddr = sa.getAddress();
        try {
            dc.send(bb, sa);
            throw new RuntimeException("Expected exception not thrown");
        } catch (IOException e) {
        }
        dc.close();
    }
}
