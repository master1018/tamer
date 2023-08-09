public class IIOPURLTest {
    public static void main(String[] args) throws Exception {
        JMXServiceURL inputAddr =
            new JMXServiceURL("service:jmx:iiop:
        JMXConnectorServer s =
            JMXConnectorServerFactory.newJMXConnectorServer(inputAddr, null,
                                                            null);
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        mbs.registerMBean(s, new ObjectName("a:b=c"));
        s.start();
        JMXServiceURL outputAddr = s.getAddress();
        if (!outputAddr.getURLPath().startsWith("/ior/IOR:")) {
            System.out.println("URL path should start with \"/ior/IOR:\": " +
                               outputAddr);
            System.exit(1);
        }
        System.out.println("IIOP URL path looks OK: " + outputAddr);
        JMXConnector c = JMXConnectorFactory.connect(outputAddr);
        System.out.println("Successfully got default domain: " +
                           c.getMBeanServerConnection().getDefaultDomain());
        c.close();
        s.stop();
    }
}
