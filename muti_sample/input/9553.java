public class NetParamsTest {
    private static void printIF(NetworkInterface netif) throws SocketException {
        System.out.println(netif.getName() + " : ");
        System.out.println("\tStatus: " + (netif.isUp() ? " UP" : "DOWN"));
        byte[] mac = netif.getHardwareAddress();
        if (mac != null) {
            System.out.print("\tHardware Address: ");
            for (byte b : mac) {
                System.out.print(Integer.toHexString(b) + ":");
            }
            System.out.println();
        }
        System.out.println("\tLoopback: " + netif.isLoopback());
        System.out.println("\tPoint to Point: " + netif.isPointToPoint());
        System.out.println("\tVirtual: " + netif.isVirtual());
        if (netif.isVirtual()) {
            System.out.println("\tParent Interface: " + netif.getParent().getName());
        }
        System.out.println("\tMulticast: " + netif.supportsMulticast());
        System.out.println("\tMTU: " + netif.getMTU());
        System.out.println("\tBindings:");
        java.util.List<InterfaceAddress> binds = netif.getInterfaceAddresses();
        for (InterfaceAddress b : binds) {
            System.out.println("\t\t" + b);
        }
        Enumeration<NetworkInterface> ifs = netif.getSubInterfaces();
        while(ifs.hasMoreElements()) {
            NetworkInterface subif = ifs.nextElement();
            printIF(subif);
        }
    }
    public static void main(String[] args) throws Exception {
        Enumeration<NetworkInterface> ifs = NetworkInterface.getNetworkInterfaces();
        while (ifs.hasMoreElements()) {
            NetworkInterface netif = ifs.nextElement();
            printIF(netif);
        }
     }
}
