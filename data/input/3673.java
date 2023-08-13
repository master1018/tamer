public class IsBound {
    public static void main(String argv[]) throws Exception {
        InetSocketAddress isa = new InetSocketAddress(
            InetAddress.getByName(TestUtil.HOST), 13);
        ByteBuffer bb = ByteBuffer.allocateDirect(256);
        bb.put("hello".getBytes());
        bb.flip();
        DatagramChannel dc = DatagramChannel.open();
        dc.send(bb, isa);
        if(!dc.socket().isBound())
            throw new Exception("Test failed");
        dc.close();
        dc = DatagramChannel.open();
        if(dc.socket().isBound())
            throw new Exception("Test failed");
        dc.close();
    }
}
