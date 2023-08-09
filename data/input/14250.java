public class CheckJNI {
    static Socket s;
    static ServerSocket server;
    static DatagramSocket dg1, dg2;
    public static void main (String[] args) throws Exception {
        System.out.println ("Testing IPv4 Socket/ServerSocket");
        server = new ServerSocket (0);
        s = new Socket ("127.0.0.1", server.getLocalPort());
        s.close();
        server.close();
        System.out.println ("Testing IPv4 DatagramSocket");
        dg1 = new DatagramSocket (0, InetAddress.getByName ("127.0.0.1"));
        dg2 = new DatagramSocket (0, InetAddress.getByName ("127.0.0.1"));
        testDatagrams (dg1, dg2);
        Enumeration ifs = NetworkInterface.getNetworkInterfaces();
        server = new ServerSocket (0);
        while (ifs.hasMoreElements()) {
            NetworkInterface nif = (NetworkInterface)ifs.nextElement();
            if (!nif.isUp())
                continue;
            Enumeration addrs = nif.getInetAddresses();
            while (addrs.hasMoreElements()) {
                InetAddress addr = (InetAddress) addrs.nextElement();
                if (addr instanceof Inet6Address) {
                    Inet6Address ia6 = (Inet6Address) addr;
                    if (ia6.isLinkLocalAddress()) {
                        System.out.println ("Testing IPv6 Socket");
                        s = new Socket (ia6, server.getLocalPort());
                        s.close();
                        System.out.println ("Testing IPv6 DatagramSocket");
                        dg1 = new DatagramSocket (0, ia6);
                        dg2 = new DatagramSocket (0, ia6);
                        testDatagrams (dg1, dg2);
                    }
                }
            }
        }
        server.close();
        System.out.println ("OK");
    }
    static void testDatagrams (DatagramSocket s1, DatagramSocket s2) throws Exception {
        DatagramPacket p1 = new DatagramPacket (
                "hello world".getBytes(),
                0, "hello world".length(), s2.getLocalAddress(),
                s2.getLocalPort()
        );
        DatagramPacket p2 = new DatagramPacket (new byte[128], 128);
        s1.send (p1);
        s2.receive (p2);
        s1.close ();
        s2.close ();
    }
}
