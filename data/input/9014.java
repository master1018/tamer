public class ScanDirClient {
    private ScanDirClient() { }
    public static final String USAGE = ScanDirClient.class.getSimpleName() +
            " <server-host> <rmi-port-number>";
    public static void main(String[] args) {
        try {
            if (args==null || args.length!=2) {
                System.err.println("Bad number of arguments: usage is: \n\t" +
                        USAGE);
                System.exit(1);
            }
            try {
                InetAddress.getByName(args[0]);
            } catch (UnknownHostException x) {
                System.err.println("No such host: " + args[0]+
                            "\n usage is: \n\t" + USAGE);
                System.exit(2);
            } catch (Exception x) {
                System.err.println("Bad address: " + args[0]+
                            "\n usage is: \n\t" + USAGE);
                System.exit(2);
            }
            try {
                if (Integer.parseInt(args[1]) <= 0) {
                    System.err.println("Bad port value: " + args[1]+
                            "\n usage is: \n\t" + USAGE);
                    System.exit(2);
                }
            } catch (Exception x) {
                System.err.println("Bad argument: " + args[1]+
                        "\n usage is: \n\t" + USAGE);
                System.exit(2);
            }
            System.out.println("\nInitialize the environment map");
            final Map<String,Object> env = new HashMap<String,Object>();
            final String[] credentials = new String[] { "guest" , "guestpasswd" };
            env.put("jmx.remote.credentials", credentials);
            env.put("com.sun.jndi.rmi.factory.socket",
                    new SslRMIClientSocketFactory());
            System.out.println("\nCreate the RMI connector client and " +
                    "connect it to the RMI connector server");
            final JMXServiceURL url = new JMXServiceURL(
                    "service:jmx:rmi:
                    "/jmxrmi");
            System.out.println("Connecting to: "+url);
            final JMXConnector jmxc = JMXConnectorFactory.connect(url, env);
            System.out.println("\nGet the MBeanServerConnection");
            final MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();
            final ScanManagerMXBean proxy =
                    ScanManager.newSingletonProxy(mbsc);
            System.out.println(
                    "\nGet ScanDirConfigMXBean from ScanManagerMXBean");
            final ScanDirConfigMXBean configMBean =
                    proxy.getConfigurationMBean();
            System.out.println(
                    "\nGet 'Configuration' attribute on ScanDirConfigMXBean");
            System.out.println("\nConfiguration:\n" +
                    configMBean.getConfiguration());
            System.out.println("\nInvoke 'close' on ScanManagerMXBean");
            try {
                proxy.close();
            } catch (SecurityException e) {
                System.out.println("\nGot expected security exception: " + e);
            }
            System.out.println("\nClose the connection to the server");
            jmxc.close();
            System.out.println("\nBye! Bye!");
        } catch (Exception e) {
            System.out.println("\nGot unexpected exception: " + e);
            e.printStackTrace();
            System.exit(3);
        }
    }
}
