public class TargetMBeanTest {
    private static final ObjectName mletName;
    static {
        try {
            mletName = new ObjectName("x:type=mlet");
        } catch (Exception e) {
            e.printStackTrace();
            throw new Error();
        }
    }
    public static void main(String[] args) throws Exception {
        System.out.println("Test that target MBean class loader is used " +
                           "before JMX Remote API class loader");
        ClassLoader jmxRemoteClassLoader =
            JMXServiceURL.class.getClassLoader();
        if (jmxRemoteClassLoader == null) {
            System.out.println("JMX Remote API loaded by bootstrap " +
                               "class loader, this test is irrelevant");
            return;
        }
        if (!(jmxRemoteClassLoader instanceof URLClassLoader)) {
            System.out.println("TEST INVALID: JMX Remote API not loaded by " +
                               "URLClassLoader");
            System.exit(1);
        }
        URLClassLoader jrcl = (URLClassLoader) jmxRemoteClassLoader;
        URL[] urls = jrcl.getURLs();
        PrivateMLet mlet = new PrivateMLet(urls, null, false);
        Class shadowClass = mlet.loadClass(JMXServiceURL.class.getName());
        if (shadowClass == JMXServiceURL.class) {
            System.out.println("TEST INVALID: MLet got original " +
                               "JMXServiceURL not shadow");
            System.exit(1);
        }
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        mbs.registerMBean(mlet, mletName);
        final String[] protos = {"rmi", "iiop", "jmxmp"};
        boolean ok = true;
        for (int i = 0; i < protos.length; i++) {
            try {
                ok &= test(protos[i], mbs);
            } catch (Exception e) {
                System.out.println("TEST FAILED WITH EXCEPTION:");
                e.printStackTrace(System.out);
                ok = false;
            }
        }
        if (ok)
            System.out.println("Test passed");
        else {
            System.out.println("TEST FAILED");
            System.exit(1);
        }
    }
    private static boolean test(String proto, MBeanServer mbs)
            throws Exception {
        System.out.println("Testing for proto " + proto);
        JMXConnectorServer cs;
        JMXServiceURL url = new JMXServiceURL(proto, null, 0);
        try {
            cs = JMXConnectorServerFactory.newJMXConnectorServer(url, null,
                                                                 mbs);
        } catch (MalformedURLException e) {
            System.out.println("System does not recognize URL: " + url +
                               "; ignoring");
            return true;
        }
        cs.start();
        JMXServiceURL addr = cs.getAddress();
        JMXServiceURL rmiurl = new JMXServiceURL("rmi", null, 0);
        JMXConnector client = JMXConnectorFactory.connect(addr);
        MBeanServerConnection mbsc = client.getMBeanServerConnection();
        ObjectName on = new ObjectName("x:proto=" + proto + ",ok=yes");
        mbsc.createMBean(RMIConnectorServer.class.getName(),
                         on,
                         mletName,
                         new Object[] {rmiurl, null},
                         new String[] {JMXServiceURL.class.getName(),
                                       Map.class.getName()});
        System.out.println("Successfully deserialized with " + proto);
        mbsc.unregisterMBean(on);
        client.close();
        cs.stop();
        return true;
    }
}
