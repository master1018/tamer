public class B6206527 {
    public static void main (String[] args) throws Exception {
        Inet6Address addr = getLocalAddr();
        if (addr == null) {
            System.out.println ("Could not find a link-local address");
            return;
        }
        ServerSocket ss = new ServerSocket();
        System.out.println ("trying LL addr: " + addr);
        ss.bind(new InetSocketAddress(addr, 0));
        addr = (Inet6Address)InetAddress.getByAddress (
            addr.getAddress()
        );
        System.out.println ("trying LL addr: " + addr);
        ss = new ServerSocket();
        ss.bind(new InetSocketAddress(addr, 0));
    }
    public static Inet6Address getLocalAddr () throws Exception {
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        while (e.hasMoreElements()) {
            NetworkInterface ifc = (NetworkInterface) e.nextElement();
            Enumeration addrs = ifc.getInetAddresses();
            while (addrs.hasMoreElements()) {
                InetAddress a = (InetAddress)addrs.nextElement();
                if (a instanceof Inet6Address) {
                    Inet6Address ia6 = (Inet6Address) a;
                    if (ia6.isLinkLocalAddress()) {
                        return ia6;
                    }
                }
            }
        }
        return null;
    }
}
