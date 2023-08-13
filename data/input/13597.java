public class ImplicitBind {
    public static void main(String[] args) throws Exception {
        DatagramSocket s = new DatagramSocket((SocketAddress)null);
        byte b[] = "hello".getBytes();
        InetAddress ia = InetAddress.getByName("localhost");
        DatagramPacket pac = new DatagramPacket(b, b.length, ia, 53);
        s.send(pac);
        if (!s.isBound())
            throw new RuntimeException("Socket should be implictly bound!");
        s.close();
        s = new DatagramSocket((SocketAddress)null);
        s.connect(new InetSocketAddress("localhost", 1234));
        if (!s.isBound())
            throw new RuntimeException("Socket should be implictly bound!");
        s.close();
    }
}
