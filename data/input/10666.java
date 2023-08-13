public class B6521014 {
    static InetAddress sin;
    static Inet6Address getLocalAddr () throws Exception {
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        while (e.hasMoreElements()) {
            NetworkInterface ifc = (NetworkInterface) e.nextElement();
            if (!ifc.isUp())
                continue;
            Enumeration addrs = ifc.getInetAddresses();
            while (addrs.hasMoreElements()) {
                InetAddress a = (InetAddress)addrs.nextElement();
                if (a instanceof Inet6Address) {
                    Inet6Address ia6 = (Inet6Address) a;
                    if (ia6.isLinkLocalAddress()) {
                        return (Inet6Address)InetAddress.getByAddress(ia6.getAddress());
                    }
                }
            }
        }
        return null;
    }
    static void test1() throws Exception {
        ServerSocket ssock;
        Socket sock;
        int port;
        ssock = new ServerSocket(0);
        port = ssock.getLocalPort();
        sock = new Socket();
        try {
            sock.connect(new InetSocketAddress(sin, port), 100);
        } catch (SocketTimeoutException e) {
            System.out.println("timed out when connecting.");
        }
    }
    static void test2() throws Exception {
        Socket sock;
        ServerSocket ssock;
        int port;
        int localport;
        ssock = new ServerSocket(0);
        ssock.setSoTimeout(100);
        port = ssock.getLocalPort();
        localport = port + 1;
        sock = new Socket();
        sock.bind(new InetSocketAddress(sin, localport));
        try {
            sock.connect(new InetSocketAddress(sin, port), 100);
        } catch (SocketTimeoutException e) {
            System.out.println("timed out when connecting.");
        }
    }
    public static void main(String[] args) throws Exception {
        sin = getLocalAddr();
        if (sin == null) {
            System.out.println("Cannot find a link-local address.");
            return;
        }
        try {
            test1();
            test2();
        } catch (IOException e) {
            throw new RuntimeException("Test failed: cannot create socket.", e);
        }
    }
}
