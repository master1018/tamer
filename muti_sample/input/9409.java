public class UserClassLoaderTest {
    private static final String[] protocols = {"rmi", "iiop", "jmxmp"};
    private static final MBeanServer mbs = MBeanServerFactory.createMBeanServer();
    private static ObjectName timer;
    private final static NotificationListener listener = new NotificationListener() {
            public void handleNotification(Notification notification, Object handback) {
            }
        };
    public static void main(String[] args) throws Exception {
        System.out.println("main: we should not lose client classloader.");
        timer = new ObjectName("test:name=timer");
        mbs.createMBean("javax.management.timer.Timer", timer);
        boolean ok = true;
        for (int i = 0; i < protocols.length; i++) {
            try {
                if (!test(protocols[i])) {
                    System.out.println("main: Test failed for " + protocols[i]);
                    ok = false;
                } else {
                    System.out.println("main: Test successed for " + protocols[i]);
                }
            } catch (Exception e) {
                System.out.println("main: Test failed for " + protocols[i]);
                e.printStackTrace(System.out);
                ok = false;
            }        }
        if (ok) {
            System.out.println("main: Tests passed");
        } else {
            System.out.println("main: Tests FAILED");
            System.exit(1);
        }
    }
    private static boolean test(String proto) throws Exception {
        System.out.println("\ntest: Test for protocol " + proto);
        JMXServiceURL u = null;
        JMXConnectorServer server = null;
        try {
            u = new JMXServiceURL(proto, null, 0);
            server = JMXConnectorServerFactory.newJMXConnectorServer(u, null, mbs);
        } catch (MalformedURLException e) {
            System.out.println("Skipping unsupported URL " + proto);
            return true;
        }
        server.start();
        u = server.getAddress();
        System.out.println("test: create a server on: "+u);
        JMXConnector client = JMXConnectorFactory.connect(u, null);
        MBeanServerConnection conn = client.getMBeanServerConnection();
        final ClassLoader orgCL = Thread.currentThread().getContextClassLoader();
        System.out.println("test: the orginal classloader is "+orgCL);
        final URL url = new URL("file:/xxx");
        final ClassLoader newCL = new URLClassLoader(new URL[]{url}, orgCL);
        try {
            System.out.println("test: set classloader to "+newCL);
            Thread.currentThread().setContextClassLoader(newCL);
            conn.addNotificationListener(timer, listener, null, null);
            client.close();
            server.stop();
            if (Thread.currentThread().getContextClassLoader() != newCL) {
                System.out.println("ERROR: The client class loader is lost.");
                return false;
            } else {
                System.out.println("test: Bye bye.");
                return true;
            }
        } finally {
            Thread.currentThread().setContextClassLoader(orgCL);
        }
    }
}
