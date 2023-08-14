public class SameObjectTwoNamesTest {
    public static void main(String[] args) throws Exception {
        boolean expectException =
                (System.getProperty("jmx.mxbean.multiname") == null);
        try {
            ObjectName objectName1 = new ObjectName("test:index=1");
            ObjectName objectName2 = new ObjectName("test:index=2");
            MBeanServer mbs = MBeanServerFactory.createMBeanServer();
            MXBC_SimpleClass01 mxBeanObject = new MXBC_SimpleClass01();
            mbs.registerMBean(mxBeanObject, objectName1);
            mbs.registerMBean(mxBeanObject, objectName2);
            if (expectException) {
                throw new Exception("TEST FAILED: " +
                        "InstanceAlreadyExistsException was not thrown");
            } else
                System.out.println("Correctly got no exception with compat property");
        } catch (InstanceAlreadyExistsException e) {
            if (expectException) {
                System.out.println("Got expected InstanceAlreadyExistsException:");
                e.printStackTrace(System.out);
            } else {
                throw new Exception(
                        "TEST FAILED: Got exception even though compat property set", e);
            }
        }
        System.out.println("TEST PASSED");
    }
    public interface MXBC_Simple01MXBean {}
    public static class MXBC_SimpleClass01 implements MXBC_Simple01MXBean {}
}
