public class B5087907 {
    public static void main(String args[]) {
        InetAddress lh = null;
        InetAddress addrs[] = null;
        try {
            lh = InetAddress.getByName("localhost");
            addrs = InetAddress.getAllByName("localhost");
        } catch (UnknownHostException e) {
            System.out.println ("Cant lookup localhost. cant run test");
            return;
        }
        boolean hasIPv4Address = false;
        boolean hasIPv6Address = false;
        for (InetAddress addr: addrs) {
            if (addr instanceof Inet4Address) {
                hasIPv4Address = true;
            }
            if (addr instanceof Inet6Address) {
                hasIPv6Address = true;
            }
            if (hasIPv4Address && hasIPv6Address) {
                break;
            }
        }
        String prop = System.getProperty("java.net.preferIPv6Addresses");
        boolean preferIPv6Addresses = (prop == null) ? false : prop.equals("true");
        System.out.println("java.net.preferIPv6Addresses: " + preferIPv6Addresses);
        System.out.println("localhost resolves to:");
        for (InetAddress addr: addrs) {
            System.out.println("  " + addr);
        }
        System.out.println("InetAddres.getByName returned: " + lh);
        boolean failed = false;
        if (preferIPv6Addresses && hasIPv6Address) {
            if (!(lh instanceof Inet6Address)) failed = true;
        }
        if (!preferIPv6Addresses && hasIPv4Address) {
            if (!(lh instanceof Inet4Address)) failed = true;
        }
        if (failed) {
            throw new RuntimeException("Test failed!");
        }
    }
}
