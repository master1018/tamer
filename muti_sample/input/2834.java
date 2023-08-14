public class GetMBeansFromURLTest {
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
        try {
            mlet.getMBeansFromURL("bogus:
            System.out.println("TEST FAILED: Expected " +
                               ServiceNotFoundException.class +
                               " exception not thrown.");
            error = true;
        } catch (ServiceNotFoundException e) {
            if (e.getCause() == null) {
                System.out.println("TEST FAILED: Got null cause in " +
                                   ServiceNotFoundException.class +
                                   " exception.");
                error = true;
            } else {
                System.out.println("TEST PASSED: Got non-null cause in " +
                                   ServiceNotFoundException.class +
                                   " exception.");
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
