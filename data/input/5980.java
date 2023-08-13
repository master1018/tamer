public class ReleaseMBeanServerTest {
    private static final String DOMAIN = "TestDomain";
    public static void main(String[] args) throws Exception {
        System.out.println("--------------------------------------" +
                           "-----------------------------------------");
        System.out.println("- Testing IllegalArgumentException in " +
                           "MBeanServerFactory.releaseMBeanServer() -");
        System.out.println("--------------------------------------" +
                           "-----------------------------------------");
        System.out.println("TEST_0: Call releaseMBeanServer() with " +
                           "a null MBeanServer reference.");
        try {
            MBeanServerFactory.releaseMBeanServer(null);
            System.err.println("Didn't get expected IllegalArgumentException!");
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.out.println("Got expected IllegalArgumentException!");
        }
        System.out.println("TEST_1: Call releaseMBeanServer() with an " +
                           "MBeanServer reference corresponding to an " +
                           "MBeanServer created using newMBeanServer().");
        MBeanServer mbs1 = MBeanServerFactory.newMBeanServer();
        try {
            MBeanServerFactory.releaseMBeanServer(mbs1);
            System.err.println("Didn't get expected IllegalArgumentException!");
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.out.println("Got expected IllegalArgumentException!");
        }
        System.out.println("TEST_2: Call releaseMBeanServer() with an " +
                           "MBeanServer reference corresponding to an " +
                           "MBeanServer created using newMBeanServer(String).");
        MBeanServer mbs2 = MBeanServerFactory.newMBeanServer(DOMAIN);
        try {
            MBeanServerFactory.releaseMBeanServer(mbs2);
            System.err.println("Didn't get expected IllegalArgumentException!");
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.out.println("Got expected IllegalArgumentException!");
        }
        System.out.println("TEST_3: Call releaseMBeanServer() twice with an " +
                           "MBeanServer reference corresponding to an MBean" +
                           "Server created using createMBeanServer().");
        MBeanServer mbs3 = MBeanServerFactory.createMBeanServer();
        MBeanServerFactory.releaseMBeanServer(mbs3);
        try {
            MBeanServerFactory.releaseMBeanServer(mbs3);
            System.err.println("Didn't get expected IllegalArgumentException!");
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.out.println("Got expected IllegalArgumentException!");
        }
        System.out.println("TEST_4: Call releaseMBeanServer() twice with an " +
                           "MBeanServer reference corresponding to an MBean" +
                           "Server created using createMBeanServer(String).");
        MBeanServer mbs4 = MBeanServerFactory.createMBeanServer(DOMAIN);
        MBeanServerFactory.releaseMBeanServer(mbs4);
        try {
            MBeanServerFactory.releaseMBeanServer(mbs4);
            System.err.println("Didn't get expected IllegalArgumentException!");
            System.exit(1);
        } catch (IllegalArgumentException e) {
            System.out.println("Got expected IllegalArgumentException!");
        }
    }
}
