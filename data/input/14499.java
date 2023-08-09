public class ReceiveISA {
    public static void main(String args[]) throws Exception {
        DatagramChannel dc1 = DatagramChannel.open();
        DatagramChannel dc2 = DatagramChannel.open();
        DatagramChannel dc3 = DatagramChannel.open();
        dc3.socket().bind((SocketAddress)null);
        InetAddress lh = InetAddress.getLocalHost();
        InetSocketAddress isa
            = new InetSocketAddress( lh, dc3.socket().getLocalPort() );
        ByteBuffer bb = ByteBuffer.allocateDirect(100);
        bb.put("Dia duit!".getBytes());
        bb.flip();
        dc1.send(bb, isa);      
        dc1.send(bb, isa);      
        dc2.send(bb, isa);      
        dc3.socket().setSoTimeout(1000);
        ByteBuffer rb = ByteBuffer.allocateDirect(100);
        SocketAddress sa[] = new SocketAddress[3];
        for (int i=0; i<3; i++) {
            sa[i] = dc3.receive(rb);
            rb.clear();
        }
        dc1.close();
        dc2.close();
        dc3.close();
        if (!sa[0].equals(sa[1])) {
            throw new Exception("Source address for packets 1 & 2 should be equal");
        }
        if (sa[1].equals(sa[2])) {
            throw new Exception("Source address for packets 2 & 3 should be different");
        }
    }
}
