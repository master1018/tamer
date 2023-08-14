public class NotAnMBeanTest {
    public static void main(String[] args) throws Exception {
        System.out.println("Try to create a java.lang.Integer as an MBean");
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        try {
            mbs.createMBean("java.lang.Integer", null);
            System.out.println("TEST FAILS: createMBean of " +
                               "java.lang.Integer succeeded!");
            System.exit(1);
        } catch (NotCompliantMBeanException e) {
            System.out.println("Got expected exception: " + e);
            System.out.println("Test passed");
        }
    }
}
