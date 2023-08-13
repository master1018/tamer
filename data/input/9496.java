public class TestIPv6Addresses {
    public static void main(String[] args) {
        try {
        InetAddress ia1 = InetAddress.getByName("fe80::a00:20ff:feae:45c9");
        InetAddress ia2 = InetAddress.getByName("[fe80::a00:20ff:feae:45c9]");
        System.out.println("InetAddress: "+ia1+" , "+ia2);
        if (!ia1.equals(ia2)) {
            throw new RuntimeException("InetAddress.getByName failed for"+
                                       "literal IPv6 addresses");
        }
        URL u1 = new URL("http", "fe80::a00:20ff:feae:45c9", 80, "/index.html");
        URL u2 = new URL("http", "[fe80::a00:20ff:feae:45c9]", 80, "/index.html");
        if (!u1.equals(u2)) {
            throw new RuntimeException("URL constructor failed for"+
                                       "literal IPv6 addresses");
        }
        if (!u1.getHost().equals(u2.getHost()) ||
            !u1.getHost().equals("[fe80::a00:20ff:feae:45c9]")) {
            throw new RuntimeException("URL.getHost() failed for"+
                                       "literal IPv6 addresses");
        }
        if (!u1.getAuthority().equals("[fe80::a00:20ff:feae:45c9]:80")) {
            throw new RuntimeException("URL.getAuthority() failed for"+
                                       "literal IPv6 addresses");
        }
        SocketPermission sp1 =
            new SocketPermission(u1.getHost()+":80-", "resolve");
        SocketPermission sp2 =
            new SocketPermission(ia1.getHostAddress()+":8080", "resolve");
        if (!sp1.implies(sp2)) {
            throw new RuntimeException("SocketPermission implies doesn't work"+
                                       " for literal IPv6 addresses");
        }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
        SecurityManager sm = new SecurityManager();
        String strAddr = "::FFFF:127.0.0.1.2";
        try {
            InetAddress addr = InetAddress.getByName(strAddr);
        } catch (UnknownHostException e) {
        }
        System.setSecurityManager(sm);
        try {
            InetAddress addr = InetAddress.getByName(strAddr);
        } catch (java.security.AccessControlException e) {
        } catch (UnknownHostException e) {
        }
    }
}
