public class NetworkPrefixLength {
    static boolean passed = true;
    public static void main(String[] args) throws Exception {
        Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
        while (nics.hasMoreElements()) {
            NetworkInterface nic = nics.nextElement();
            for (InterfaceAddress iaddr : nic.getInterfaceAddresses()) {
                boolean valid = checkPrefix(iaddr);
                if (!valid) {
                    passed = false;
                    debug(nic.getName(), iaddr);
                }
            }
        }
        if (!passed)
            throw new RuntimeException("Failed: some interfaces have invalid prefix lengths");
    }
    static boolean checkPrefix(InterfaceAddress iaddr) {
        InetAddress addr = iaddr.getAddress();
        if (addr instanceof Inet4Address)
            return checkIPv4PrefixLength(iaddr.getNetworkPrefixLength());
        else
            return checkIPv6PrefixLength(iaddr.getNetworkPrefixLength());
    }
    static boolean checkIPv4PrefixLength(int prefix) {
        if (prefix >=0 && prefix <= 32)
            return true;
        return false;
    }
    static boolean checkIPv6PrefixLength(int prefix) {
        if (prefix >=0 && prefix <= 128)
            return true;
        return false;
    }
    static void debug(String nicName, InterfaceAddress iaddr) {
        out.println("NIC " + nicName + " has an address with an invalid prefix length:\n" + iaddr);
    }
}
