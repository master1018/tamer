public class InvalidMXBeanRegistrationTest {
    @MXBean(true)
    public interface Compliant1 {}
    public interface Compliant2MXBean {}
    public static class Compliant implements Compliant1, Compliant2MXBean {}
    public static void main(String[] args) throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        ObjectName on = new ObjectName("a:b=c");
        Compliant mxbean = new Compliant();
        boolean ok;
        try {
            mbs.registerMBean(mxbean, on);
            System.out.println("Didn't get expected NotCompliantMBeanException");
            ok = false;
        } catch (NotCompliantMBeanException e) {
            System.out.println("Got expected exception: " + e);
            ok = true;
        } catch (Exception e) {
            System.out.println("Got unexpected exception: " + e);
            ok = false;
        }
        if (ok)
            System.out.println("Test PASSED");
        else {
            System.out.println("Test FAILED");
        }
    }
}
