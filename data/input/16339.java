public class ListenAddress {
    private static Connector findConnector(String name) {
        List connectors = Bootstrap.virtualMachineManager().allConnectors();
        Iterator iter = connectors.iterator();
        while (iter.hasNext()) {
            Connector connector = (Connector)iter.next();
            if (connector.name().equals(name)) {
                return connector;
            }
        }
        return null;
    }
    static int failures = 0;
    private static void check(ListeningConnector connector, InetAddress addr)
        throws IOException, IllegalConnectorArgumentsException
    {
        Map args = connector.defaultArguments();
        if (addr != null) {
            Connector.StringArgument addr_arg =
              (Connector.StringArgument)args.get("localAddress");
            addr_arg.setValue(addr.getHostAddress());
        }
        String address = connector.startListening(args);
        if (address.indexOf(':') < 0) {
            System.out.println(address + " => Failed - no host component!");
            failures++;
        } else {
            System.out.println(address);
        }
        connector.stopListening(args);
    }
    public static void main(String args[]) throws Exception {
        ListeningConnector connector = (ListeningConnector)findConnector("com.sun.jdi.SocketListen");
        check(connector, (InetAddress)null);
        Enumeration nifs = NetworkInterface.getNetworkInterfaces();
        while (nifs.hasMoreElements()) {
            NetworkInterface ni = (NetworkInterface)nifs.nextElement();
            Enumeration addrs = ni.getInetAddresses();
            while (addrs.hasMoreElements()) {
                InetAddress addr = (InetAddress)addrs.nextElement();
                if (!(addr instanceof Inet4Address)) {
                    continue;
                }
                check(connector, addr);
            }
        }
        if (failures > 0) {
            throw new RuntimeException(failures + " test(s) failed - see output for details.");
        }
    }
}
