public class FullThreadDump {
    private MBeanServerConnection server;
    private JMXConnector jmxc;
    public FullThreadDump(String hostname, int port) {
        System.out.println("Connecting to " + hostname + ":" + port);
        String urlPath = "/jndi/rmi:
        connect(urlPath);
   }
   public void dump() {
        try {
            ThreadMonitor monitor = new ThreadMonitor(server);
            monitor.threadDump();
            if (!monitor.findDeadlock()) {
                System.out.println("No deadlock found.");
            }
        } catch (IOException e) {
            System.err.println("\nCommunication error: " + e.getMessage());
            System.exit(1);
        }
    }
    private void connect(String urlPath) {
        try {
            JMXServiceURL url = new JMXServiceURL("rmi", "", 0, urlPath);
            this.jmxc = JMXConnectorFactory.connect(url);
            this.server = jmxc.getMBeanServerConnection();
        } catch (MalformedURLException e) {
        } catch (IOException e) {
            System.err.println("\nCommunication error: " + e.getMessage());
            System.exit(1);
        }
    }
    public static void main(String[] args) {
        if (args.length != 1) {
            usage();
        }
        String[] arg2 = args[0].split(":");
        if (arg2.length != 2) {
            usage();
        }
        String hostname = arg2[0];
        int port = -1;
        try {
            port = Integer.parseInt(arg2[1]);
        } catch (NumberFormatException x) {
            usage();
        }
        if (port < 0) {
            usage();
        }
        FullThreadDump ftd = new FullThreadDump(hostname, port);
        ftd.dump();
    }
    private static void usage() {
        System.out.println("Usage: java FullThreadDump <hostname>:<port>");
    }
}
