public class NetworkUtils {
    public native static int enableInterface(String interfaceName);
    public native static int disableInterface(String interfaceName);
    public native static int addHostRoute(String interfaceName, int hostaddr);
    public native static int setDefaultRoute(String interfaceName, int gwayAddr);
    public native static int getDefaultRoute(String interfaceName);
    public native static int removeHostRoutes(String interfaceName);
    public native static int removeDefaultRoute(String interfaceName);
    public native static int resetConnections(String interfaceName);
    public native static boolean runDhcp(String interfaceName, DhcpInfo ipInfo);
    public native static boolean stopDhcp(String interfaceName);
    public native static boolean releaseDhcpLease(String interfaceName);
    public native static String getDhcpError();
    public static boolean configureInterface(String interfaceName, DhcpInfo ipInfo) {
        return configureNative(interfaceName,
            ipInfo.ipAddress,
            ipInfo.netmask,
            ipInfo.gateway,
            ipInfo.dns1,
            ipInfo.dns2);
    }
    private native static boolean configureNative(
        String interfaceName, int ipAddress, int netmask, int gateway, int dns1, int dns2);
    public static int lookupHost(String hostname) {
        InetAddress inetAddress;
        try {
            inetAddress = InetAddress.getByName(hostname);
        } catch (UnknownHostException e) {
            return -1;
        }
        byte[] addrBytes;
        int addr;
        addrBytes = inetAddress.getAddress();
        addr = ((addrBytes[3] & 0xff) << 24)
                | ((addrBytes[2] & 0xff) << 16)
                | ((addrBytes[1] & 0xff) << 8)
                |  (addrBytes[0] & 0xff);
        return addr;
    }
}
