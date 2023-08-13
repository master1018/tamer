public class MXBeanPreRegisterTest {
    public static interface EmptyMBean {}
    public static interface EmptyMXBean extends EmptyMBean {}
    public static class Base implements MBeanRegistration {
        public ObjectName preRegister(MBeanServer mbs, ObjectName n) {
            count++;
            return n;
        }
        public void postRegister(Boolean done) {
            count++;
        }
        public void preDeregister() {
            count++;
        }
        public void postDeregister() {
            count++;
        }
        int count;
    }
    public static class Empty extends Base implements EmptyMBean {}
    public static class EmptyMX extends Base implements EmptyMXBean {}
    public static void main(String[] args) throws Exception {
        for (boolean mx : new boolean[] {false, true})
            for (boolean wrapped : new boolean[] {false, true})
                test(mx, wrapped);
        if (failure != null)
            throw new Exception("TEST FAILED: " + failure);
        System.out.println("TEST PASSED");
    }
    private static void test(boolean mx, boolean wrapped) throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        ObjectName on = new ObjectName("a:b=c");
        Base mbean = mx ? new EmptyMX() : new Empty();
        Object reg = wrapped ? new StandardMBean(mbean, null, mx) : mbean;
        mbs.registerMBean(reg, on);
        mbs.unregisterMBean(on);
        String testDescr =
            (mx ? "MXBean" : "Standard MBean") +
            (wrapped ? " wrapped in StandardMBean class should not" :
             " should") +
            " call MBeanRegistration methods";
        boolean ok = mbean.count == (wrapped ? 0 : 4);
        if (ok)
            System.out.println("OK: " + testDescr);
        else {
            failure = testDescr;
            System.out.println("FAILED: " + failure);
        }
    }
    private static String failure;
}
