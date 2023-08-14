public class ScopeTests extends Tests {
    public static void main (String[] args) throws Exception {
        checkDebug (args);
        simpleTests();
        complexTests();
        System.out.println ("Tests passed: OK");
    }
    static void sassert (boolean condition, String msg) throws Exception {
        if (!condition) {
            throw new Exception (msg);
        }
    }
    static void checkAddress (String a, int numeric) throws Exception {
        Inet6Address addr = (Inet6Address) InetAddress.getByName (a);
        if (addr.getScopeId () != numeric) {
            throw new Exception ("wroing numeric scopeid");
        }
    }
    static void checkAddress (String a, String str) throws Exception {
        Inet6Address addr = (Inet6Address) InetAddress.getByName (a);
        if (!addr.getScopedInterface().getName().equals (str)) {
            throw new Exception ("wroing scoped interface name");
        }
    }
    static void simpleTests () throws Exception {
        checkAddress ("fe80::%1", 1);
        checkAddress ("fe80::1%1", 1);
        checkAddress ("fec0::1:a00:20ff:feed:b08d%0", 0);
        checkAddress ("fec0::1:a00:20ff:feed:b08d%1", 1);
        checkAddress ("fec0::1:a00:20ff:feed:b08d%99", 99);
        checkAddress ("fe80::", 0);
        checkAddress ("fec0::1:a00:20ff:feed:b08d", 0);
        checkAddress ("fec0::1:a00:20ff:feed:b08d", 0);
        checkAddress ("fec0::1:a00:20ff:feed:b08d", 0);
    }
    static void complexTests () throws Exception {
        dprintln ("ComplexTests");
        Enumeration e = NetworkInterface.getNetworkInterfaces();
        while (e.hasMoreElements()) {
            NetworkInterface nif = (NetworkInterface)e.nextElement();
            String name = nif.getName();
            Enumeration addrs = nif.getInetAddresses();
            while (addrs.hasMoreElements()) {
                InetAddress addr = (InetAddress) addrs.nextElement();
                dprintln ("ComplexTests: "+addr);
                if (addr instanceof Inet6Address) {
                    Inet6Address ia6 = (Inet6Address) addr;
                    if (ia6.getScopeId() != 0) {
                        System.out.println ("Testing interface: " + name +
                                            " and address: " + ia6);
                        ctest1 (name, ia6);
                        ctest2 (name, ia6);
                    } else {
                        System.out.println ("Interface: " + name +
                                            " Address: "+ ia6 +
                                            " does not support scope");
                    }
                }
            }
        }
    }
    static void ctest1 (String ifname, Inet6Address ia6) throws Exception {
        System.out.println ("ctest1:" + ia6);
        String s = ia6.getScopedInterface().getName();
        int scope = ia6.getScopeId();
        sassert (ifname.equals (s), "ctest1:"+ifname+":"+s);
    }
    static void ctest2 (String ifname, Inet6Address ia6) throws Exception {
        System.out.println ("ctest2:" + ia6);
        String s = ia6.getScopedInterface().getName();
        int scope = ia6.getScopeId();
        String s1 = ia6.getHostAddress();
        if (s1.indexOf('%') != -1) {
            s1 = s1.substring (0, s1.indexOf ('%'));
        }
        Inet6Address add = (Inet6Address) InetAddress.getByName (s1+"%"+ifname);
        sassert (add.getScopeId() == scope, "ctest2:1:" +scope);
    }
}
