public class DeserializeEncodedURLTest {
    private static final ClassLoader mutantLoader =
        new SingleClassLoader("SubMutantRMIServerStub",
                              MutantRMIServerStub.class,
                              MutantRMIServerStub.class.getClassLoader());
    private static final Class subMutantRMIServerStubClass;
    static {
        try {
            subMutantRMIServerStubClass =
                mutantLoader.loadClass("SubMutantRMIServerStub");
        } catch (ClassNotFoundException e) {
            throw new Error(e);
        }
    }
    public static void main(String[] args) throws Exception {
        System.out.println("Check that we can deserialize a mutant stub " +
                           "from an RMI connector URL even when the stub's " +
                           "class is known to the user's default loader " +
                           "but not the caller's loader");
        System.out.println("Create RMI connector server as an MBean");
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        ObjectName csName = new ObjectName("test:type=RMIConnectorServer");
        JMXServiceURL url = new JMXServiceURL("rmi", null, 0);
        RMIServerImpl impl = new MutantRMIServerImpl();
        mbs.createMBean("javax.management.remote.rmi.RMIConnectorServer",
                        csName,
                        new Object[] {url, null, impl, null},
                        new String[] {JMXServiceURL.class.getName(),
                                      Map.class.getName(),
                                      RMIServerImpl.class.getName(),
                                      MBeanServer.class.getName()});
        mbs.invoke(csName, "start", new Object[0], new String[0]);
        JMXServiceURL address =
            (JMXServiceURL) mbs.getAttribute(csName, "Address");
        System.out.println("Address with mutant stub: " + address);
        Map env = new HashMap();
        env.put(JMXConnectorFactory.DEFAULT_CLASS_LOADER, mutantLoader);
        JMXConnector conn = JMXConnectorFactory.newJMXConnector(address, env);
        System.out.println("Client successfully created with this address");
        System.out.println("Try to connect newly-created client");
        try {
            conn.connect();
            System.out.println("TEST FAILS: Connect worked but should not " +
                               "have");
            System.exit(1);
        } catch (MutantException e) {
            System.out.println("Caught MutantException as expected");
        } catch (Exception e) {
            System.out.println("TEST FAILS: Caught unexpected exception:");
            e.printStackTrace(System.out);
            System.exit(1);
        }
        mbs.invoke(csName, "stop", new Object[0], new String[0]);
        System.out.println("Test passed");
    }
    private static class MutantException extends IOException {}
    public static class MutantRMIServerStub
            implements RMIServer, Serializable {
        public MutantRMIServerStub() {}
        public String getVersion() {
            return "1.0 BOGUS";
        }
        public RMIConnection newClient(Object credentials) throws IOException {
            throw new MutantException();
        }
    }
    private static class MutantRMIServerImpl extends RMIJRMPServerImpl {
        public MutantRMIServerImpl() throws IOException {
            super(0, null, null, null);
        }
        public Remote toStub() throws IOException {
            try {
                return (Remote) subMutantRMIServerStubClass.newInstance();
            } catch (Exception e) {
                IOException ioe =
                    new IOException("Couldn't make submutant stub");
                ioe.initCause(e);
                throw ioe;
            }
        }
    }
}
