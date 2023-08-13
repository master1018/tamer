public class B4923906 {
    public static void main (String[] args) throws Exception {
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        while (e.hasMoreElements()) {
            NetworkInterface ifc = (NetworkInterface) e.nextElement();
            Enumeration addrs = ifc.getInetAddresses();
            System.out.println (ifc.getName() +": " + ifc.getDisplayName());
            while (addrs.hasMoreElements()) {
                InetAddress a = (InetAddress)addrs.nextElement();
                System.out.println ("\t"+a);
                if (a instanceof Inet6Address) {
                    Inet6Address ia6 = (Inet6Address) a;
                    Object o = ia6.getScopedInterface();
                    if (o instanceof String) {
                        throw new RuntimeException (
                           "Oops! This should be a NetworkInterface"
                        );
                    }
                }
            }
        }
    }
}
