public class MulticastAddresses {
    public static void main(String args[]) throws Exception {
        boolean ipv6_available = false;
        NetworkInterface ni = null;
        Enumeration nifs = NetworkInterface.getNetworkInterfaces();
        while (nifs.hasMoreElements()) {
            NetworkInterface this_ni = (NetworkInterface)nifs.nextElement();
            Enumeration addrs = this_ni.getInetAddresses();
            while (addrs.hasMoreElements()) {
                InetAddress addr = (InetAddress)addrs.nextElement();
                if (addr instanceof Inet6Address) {
                    ipv6_available = true;
                }
                if (!addr.isLoopbackAddress() && ni == null) {
                    ni = this_ni;
                }
            }
            if (ipv6_available) {
                break;
            }
        }
        int failures = 0;
        String multicasts[] = {
                "224.80.80.80",
                "ff01::1",
                "ff02::1234",
                "ff05::a",
                "ff0e::1234:a" };
        String non_multicasts[] = {
                "129.1.1.1",
                "::1",
                "::129.1.1.1",
                "fe80::a00:20ff:fee5:bc02" };
        MulticastSocket s = new MulticastSocket();
        for (int i=0; i<multicasts.length; i++) {
            InetAddress ia = InetAddress.getByName(multicasts[i]);
            if (ia instanceof Inet6Address && !ipv6_available) {
                continue;
            }
            System.out.println("Test: " + ia);
            try {
                System.out.print("    joinGroup(InetAddress) ");
                s.joinGroup(ia);
                s.leaveGroup(ia);
                System.out.println("    Passed.");
                System.out.print("    joinGroup(InetAddress,NetworkInterface) ");
                s.joinGroup(new InetSocketAddress(ia,0), ni);
                s.leaveGroup(new InetSocketAddress(ia,0), ni);
                System.out.println("    Passed.");
            } catch (IOException e) {
                failures++;
                System.out.println("Failed: " + e.getMessage());
            }
        }
        for (int i=0; i<non_multicasts.length; i++) {
            InetAddress ia = InetAddress.getByName(non_multicasts[i]);
            if (ia instanceof Inet6Address && !ipv6_available) {
                continue;
            }
            boolean failed = false;
            System.out.println("Test: " + ia + " ");
            try {
                System.out.println("    joinGroup(InetAddress) ");
                s.joinGroup(ia);
                System.out.println("Failed!! -- incorrectly joined group");
                failed = true;
            } catch (IOException e) {
                System.out.println("    Passed: " + e.getMessage());
            }
            if (failed) {
                s.leaveGroup(ia);
                failures++;
            }
        }
        s.close();
        if (failures > 0) {
            throw new Exception(failures + " test(s) failed - see log file.");
        }
    }
}
