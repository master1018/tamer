public class ParserInfiniteLoopTest {
    public static void main(String[] args) throws Exception {
        boolean error = false;
        System.out.println("Create the MBean server");
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        System.out.println("Create the MLet");
        MLet mlet = new MLet();
        System.out.println("Register the MLet MBean");
        ObjectName mletObjectName = new ObjectName("Test:type=MLet");
        mbs.registerMBean(mlet, mletObjectName);
        System.out.println("Call mlet.getMBeansFromURL(<url>)");
        String testSrc = System.getProperty("test.src");
        System.out.println("test.src = " + testSrc);
        String urlCodebase;
        if (testSrc.startsWith("/")) {
            urlCodebase =
                "file:" + testSrc.replace(File.separatorChar, '/') + "/";
        } else {
            urlCodebase =
                "file:/" + testSrc.replace(File.separatorChar, '/') + "/";
        }
        String mletFile = urlCodebase + args[0];
        System.out.println("MLet File = " + mletFile);
        try {
            mlet.getMBeansFromURL(mletFile);
            System.out.println(
                "TEST FAILED: Expected ServiceNotFoundException not thrown");
            error = true;
        } catch (ServiceNotFoundException e) {
            if (e.getCause() == null) {
                System.out.println("TEST FAILED: Got unexpected null cause " +
                    "in ServiceNotFoundException");
                error = true;
            } else if (!(e.getCause() instanceof IOException)) {
                System.out.println("TEST FAILED: Got unexpected non-null " +
                    "cause in ServiceNotFoundException");
                error = true;
            } else {
                System.out.println("TEST PASSED: Got expected non-null " +
                    "cause in ServiceNotFoundException");
                error = false;
            }
            e.printStackTrace(System.out);
        }
        System.out.println("Unregister the MLet MBean");
        mbs.unregisterMBean(mletObjectName);
        System.out.println("Release the MBean server");
        MBeanServerFactory.releaseMBeanServer(mbs);
        System.out.println("Bye! Bye!");
        if (error) System.exit(1);
    }
}
