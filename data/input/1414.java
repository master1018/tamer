public class ConnectedSend {
    public static void main(String[] args) throws Exception {
        test1();
        test2();
    }
    private static void test1() throws Exception {
        DatagramChannel sndChannel = DatagramChannel.open();
        sndChannel.socket().bind(null);
        InetSocketAddress sender = new InetSocketAddress(
            InetAddress.getLocalHost(),
            sndChannel.socket().getLocalPort());
        DatagramChannel rcvChannel = DatagramChannel.open();
        rcvChannel.socket().bind(null);
        InetSocketAddress receiver = new InetSocketAddress(
            InetAddress.getLocalHost(),
            rcvChannel.socket().getLocalPort());
        rcvChannel.connect(sender);
        sndChannel.connect(receiver);
        ByteBuffer bb = ByteBuffer.allocate(256);
        bb.put("hello".getBytes());
        bb.flip();
        int sent = sndChannel.send(bb, receiver);
        bb.clear();
        rcvChannel.receive(bb);
        bb.flip();
        CharBuffer cb = Charset.forName("US-ASCII").newDecoder().decode(bb);
        if (!cb.toString().startsWith("h"))
            throw new RuntimeException("Test failed");
        rcvChannel.close();
        sndChannel.close();
    }
    private static void test2() throws Exception {
        DatagramChannel sndChannel = DatagramChannel.open();
        sndChannel.socket().bind(null);
        InetSocketAddress sender = new InetSocketAddress(
            InetAddress.getLocalHost(),
            sndChannel.socket().getLocalPort());
        DatagramChannel rcvChannel = DatagramChannel.open();
        rcvChannel.socket().bind(null);
        InetSocketAddress receiver = new InetSocketAddress(
            InetAddress.getLocalHost(),
            rcvChannel.socket().getLocalPort());
        rcvChannel.connect(sender);
        sndChannel.connect(receiver);
        byte b[] = "hello".getBytes("UTF-8");
        DatagramPacket pkt = new DatagramPacket(b, b.length);
        sndChannel.socket().send(pkt);
        ByteBuffer bb = ByteBuffer.allocate(256);
        rcvChannel.receive(bb);
        bb.flip();
        CharBuffer cb = Charset.forName("US-ASCII").newDecoder().decode(bb);
        if (!cb.toString().startsWith("h"))
            throw new RuntimeException("Test failed");
        if (!pkt.getSocketAddress().equals(receiver))
            throw new RuntimeException("Test failed");
        rcvChannel.close();
        sndChannel.close();
    }
}
