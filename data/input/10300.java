public class B6214234 {
    public static void main (String[] args) throws Exception {
        String osname = System.getProperty ("os.name");
        String version = System.getProperty ("os.version");
        if (!"SunOS".equals (osname)) {
            System.out.println ("Test only runs on Solaris");
            return;
        }
        String[] v = version.split("\\.");
        int verNumber = Integer.parseInt (v[0]) * 100 + Integer.parseInt (v[1]);
        if (verNumber < 510) {
            System.out.println ("Test only runs on Solaris versions 10 or higher");
            return;
        }
        Inet6Address addr = getLocalAddr();
        if (addr == null) {
            System.out.println ("Could not find a link-local address");
            return;
        }
        if (addr.getScopeId() == 0) {
            System.out.println("addr: "+ addr);
            throw new RuntimeException ("Non zero scope_id expected");
        }
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
