public class TestManager {
    private static void startManagementAgent(String pid) throws IOException {
        String home = System.getProperty("java.home");
        String agent = home + File.separator + "jre" + File.separator + "lib"
                + File.separator + "management-agent.jar";
        File f = new File(agent);
        if (!f.exists()) {
            agent = home + File.separator + "lib" + File.separator +
                "management-agent.jar";
            f = new File(agent);
            if (!f.exists()) {
                throw new RuntimeException("management-agent.jar missing");
            }
        }
        agent = f.getCanonicalPath();
        System.out.println("Loading " + agent + " into target VM ...");
        try {
            VirtualMachine.attach(pid).loadAgent(agent);
        } catch (Exception x) {
            throw new IOException(x.getMessage());
        }
    }
    private static void connect(String pid, String address) throws Exception {
        if (address == null) {
            throw new RuntimeException("Local connector address for " +
                                       pid + " is null");
        }
        System.out.println("Connect to process " + pid + " via: " + address);
        JMXServiceURL url = new JMXServiceURL(address);
        JMXConnector c = JMXConnectorFactory.connect(url);
        MBeanServerConnection server = c.getMBeanServerConnection();
        System.out.println("Connected.");
        RuntimeMXBean rt = newPlatformMXBeanProxy(server,
            RUNTIME_MXBEAN_NAME, RuntimeMXBean.class);
        System.out.println(rt.getName());
        c.close();
    }
    private final static String LOCAL_CONNECTOR_ADDRESS_PROP =
        "com.sun.management.jmxremote.localConnectorAddress";
    public static void main(String[] args) throws Exception {
        String pid = args[0]; 
        VirtualMachine vm = VirtualMachine.attach(pid);
        String agentPropLocalConnectorAddress = (String)
            vm.getAgentProperties().get(LOCAL_CONNECTOR_ADDRESS_PROP);
        int vmid = Integer.parseInt(pid);
        String jvmstatLocalConnectorAddress =
            ConnectorAddressLink.importFrom(vmid);
        if (agentPropLocalConnectorAddress == null &&
            jvmstatLocalConnectorAddress == null) {
            startManagementAgent(pid);
            agentPropLocalConnectorAddress = (String)
                vm.getAgentProperties().get(LOCAL_CONNECTOR_ADDRESS_PROP);
            jvmstatLocalConnectorAddress =
                ConnectorAddressLink.importFrom(vmid);
        }
        System.out.println("Testing the connector address from agent properties");
        connect(pid, agentPropLocalConnectorAddress);
        System.out.println("Testing the connector address from jvmstat buffer");
        connect(pid, jvmstatLocalConnectorAddress);
        int port = Integer.parseInt(args[1]);
        System.out.println("Shutdown process via TCP port: " + port);
        Socket s = new Socket();
        s.connect(new InetSocketAddress(port));
        s.close();
    }
}
