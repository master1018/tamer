public class LinkLocal {
    static int testCount = 0;
    static int failed = 0;
    static void TcpTest(InetAddress ia) throws Exception {
        System.out.println("**************************************");
        testCount++;
        System.out.println("Test " + testCount + ": TCP connect to " + ia);
        ServerSocket ss = new ServerSocket(0);
        Socket s = new Socket();
        try {
            s.connect(new InetSocketAddress(ia, ss.getLocalPort()));
            System.out.println("Test passed - connection established.");
            Socket s2 = ss.accept();
            s2.close();
        } catch (SocketException e) {
            failed++;
            System.out.println("Test failed: " + e);
        } finally {
            s.close();
            ss.close();
        }
    }
    static void UdpTest(InetAddress ia, boolean connected) throws Exception {
        System.out.println("**************************************");
        testCount++;
        if (connected) {
            System.out.println("Test " + testCount + ": UDP connect to " + ia);
        } else {
            System.out.println("Test " + testCount + ": UDP send to " + ia);
        }
        DatagramSocket ds1 = new DatagramSocket();
        DatagramSocket ds2 = new DatagramSocket();
        try {
            byte b[] = "Hello".getBytes();
            DatagramPacket p = new DatagramPacket(b, b.length);
            if (connected) {
                ds1.connect( new InetSocketAddress(ia, ds2.getLocalPort()) );
                System.out.println("DatagramSocket connected.");
            } else {
                p.setAddress(ia);
                p.setPort(ds2.getLocalPort());
            }
            ds1.send(p);
            System.out.println("Packet has been sent.");
            ds2.setSoTimeout(5000);
            ds2.receive(p);
            System.out.println("Test passed - packet received.");
        } catch (SocketException e) {
            failed++;
            System.out.println("Test failed: " + e);
        } finally {
            ds1.close();
            ds2.close();
        }
    }
    static void TestAddress(InetAddress ia) throws Exception {
        TcpTest(ia);
        UdpTest(ia, true);      
        UdpTest(ia, false);     
    }
    public static void main(String args[]) throws Exception {
        if (args.length > 0) {
            InetAddress ia = InetAddress.getByName(args[0]);
            if ( !(ia instanceof Inet6Address) ||
                !ia.isLinkLocalAddress()) {
                throw new Exception(ia +
                        " is not a link-local IPv6 address");
            }
            TestAddress(ia);
        }
        if (args.length == 0) {
            Enumeration nifs = NetworkInterface.getNetworkInterfaces();
            while (nifs.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface)nifs.nextElement();
                if (!ni.isUp())
                    continue;
                Enumeration addrs = ni.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = (InetAddress)addrs.nextElement();
                    if (addr instanceof Inet6Address &&
                        addr.isLinkLocalAddress()) {
                        TestAddress(addr);
                    }
                }
            }
        }
        if (testCount == 0) {
            System.out.println("No link-local IPv6 addresses - test skipped!");
        } else {
            System.out.println("**************************************");
            System.out.println(testCount + " test(s) executed, " +
                failed + " failed.");
            if (failed > 0) {
                throw new Exception( failed + " test(s) failed.");
            }
        }
    }
}
