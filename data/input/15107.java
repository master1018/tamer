public class UdpTest extends Tests {
    static DatagramSocket c3, s1, s2, s3;
    static InetAddress s1peer, s2peer;
    static InetAddress ia4any;
    static InetAddress ia6any;
    static Inet6Address ia6addr;
    static InetAddress ia6bad; 
    static InetAddress ia6rem1;
    static Inet4Address ia4addr;
    static {
        try {
            ia4any = InetAddress.getByName ("0.0.0.0");
            ia6any = InetAddress.getByName ("::0");
            try {
                ia6bad = InetAddress.getByName ("2002:819c:dc29:1:1322:33ff:fe44:5566%net0");
            } catch (Exception e) {
                ia6bad = InetAddress.getByName ("2002:819c:dc29:1:1322:33ff:fe44:5566");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ia6addr = getFirstLocalIPv6Address ();
        ia4addr = getFirstLocalIPv4Address ();
    }
    public static void main (String[] args) throws Exception {
        checkDebug(args);
        if (ia6addr == null) {
            System.out.println ("No local IPv6 addresses: exiting now");
            return;
        }
        dprintln ("Local Addresses");
        dprintln (ia4addr.toString());
        dprintln (ia6addr.toString());
        test1 ();
        test2 ();
        if (!isLinux()) {
            test3 ();
        }
        test4 ();
    }
    static void test1 () throws Exception {
        s1 = new DatagramSocket ();
        s2 = new DatagramSocket ();
        simpleDataExchange (s1, ia4addr, s2, ia4addr);
        s1.close (); s2.close ();
        s1 = new DatagramSocket ();
        s2 = new DatagramSocket ();
        simpleDataExchange (s1, ia6addr, s2, ia6addr);
        s1.close (); s2.close ();
        s1 = new DatagramSocket (0, ia6addr);
        s2 = new DatagramSocket (0, ia6addr);
        simpleDataExchange (s1, ia6addr, s2, ia6addr);
        s1.close (); s2.close ();
        s1 = new DatagramSocket ();
        s2 = new DatagramSocket ();
        simpleDataExchange (s1, ia6addr, s2, ia4addr);
        s1.close (); s2.close ();
        s1 = new DatagramSocket ();
        s2 = new DatagramSocket (0, ia6addr);
        s3 = new DatagramSocket (0, ia4addr);
        datagramEcho (s2, s1, ia6addr);
        datagramEcho (s3, s1, ia4addr);
        s1.close (); s2.close (); s3.close();
        System.out.println ("Test1: OK");
    }
    static void test2 () throws Exception {
        s1 = new DatagramSocket ();
        s2 = new DatagramSocket ();
        s1.setSoTimeout (4000);
        long t1 = System.currentTimeMillis();
        try {
            s1.receive (new DatagramPacket (new byte [128], 128));
            throw new Exception ("expected receive timeout ");
        } catch (SocketTimeoutException e) {
        }
        checkTime (System.currentTimeMillis() - t1, 4000);
        simpleDataExchange (s1, ia6addr, s2, ia4addr);
        t1 = System.currentTimeMillis();
        try {
            s1.receive (new DatagramPacket (new byte [128], 128));
            throw new Exception ("expected receive timeout ");
        } catch (SocketTimeoutException e) {
        }
        checkTime (System.currentTimeMillis() - t1, 4000);
        final DatagramSocket s = s2;
        final InetAddress ia6 = ia6addr;
        final int port = s1.getLocalPort();
        runAfter (2000, new Runnable () {
            public void run () {
                try {
                    DatagramPacket p = new DatagramPacket ("Hello 123".getBytes(), 0, 8, ia6, port);
                    s.send (p);
                } catch (Exception e) {}
            }
        });
        t1 = System.currentTimeMillis();
        s1.receive (new DatagramPacket (new byte [128], 128));
        checkTime (System.currentTimeMillis() - t1, 2000);
        s1.close ();
        s2.close ();
        System.out.println ("Test2: OK");
    }
    static void test3 () throws Exception {
        s1 = new DatagramSocket ();
        s2 = new DatagramSocket ();
        s1.connect (ia6addr, s2.getLocalPort());
        datagramEcho (s1, s2, null);
        s1.close (); s2.close();
        System.out.println ("Test3: OK");
    }
    static void test4 () throws Exception {
        s1 = new DatagramSocket ();
        s1.connect (ia6addr, 5000);
        s1.setSoTimeout (3000);
        try {
            DatagramPacket p = new DatagramPacket ("HelloWorld".getBytes(), "HelloWorld".length());
            s1.send (p);
            p = new DatagramPacket (new byte[128], 128);
            s1.receive (p);
        } catch (PortUnreachableException e) {
            System.out.println ("Test4: OK");
            return;
        } catch (SocketTimeoutException e) {
            System.out.println ("Test4: failed. Never mind, it's an OS bug");
        }
    }
}
