public class MXBeanFlagTest {
    public interface Compliant1MXBean {}
    public static class Compliant1 implements Compliant1MXBean {}
    public interface Compliant2MXBean {}
    public static class Compliant2 implements Compliant2MXBean {}
    public interface Compliant3MBean {}
    public static class Compliant3 implements Compliant3MBean {}
    public interface Compliant4MBean {}
    public static class Compliant4 implements Compliant4MBean {}
    public static void main(String[] args) throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        ObjectName on1 = new ObjectName(":type=Compliant1");
        ObjectName on2 = new ObjectName(":type=Compliant2");
        ObjectName on3 = new ObjectName(":type=Compliant3");
        ObjectName on4 = new ObjectName(":type=Compliant4");
        Compliant1 compliant1 = new Compliant1();
        StandardMBean compliant2 =
            new StandardMBean(new Compliant2(), Compliant2MXBean.class, true);
        Compliant3 compliant3 = new Compliant3();
        StandardMBean compliant4 =
            new StandardMBean(new Compliant4(), Compliant4MBean.class, false);
        mbs.registerMBean(compliant1, on1);
        mbs.registerMBean(compliant2, on2);
        mbs.registerMBean(compliant3, on3);
        mbs.registerMBean(compliant4, on4);
        String flag1 = (String)
            mbs.getMBeanInfo(on1).getDescriptor().getFieldValue("mxbean");
        String flag2 = (String)
            mbs.getMBeanInfo(on2).getDescriptor().getFieldValue("mxbean");
        String flag3 = (String)
            mbs.getMBeanInfo(on3).getDescriptor().getFieldValue("mxbean");
        String flag4 = (String)
            mbs.getMBeanInfo(on4).getDescriptor().getFieldValue("mxbean");
        System.out.println("MXBean compliant1:\n" +
            "\t[Expected: mxbean=true]  [Got: mxbean=" + flag1 + "]");
        System.out.println("MXBean compliant2:\n" +
            "\t[Expected: mxbean=true]  [Got: mxbean=" + flag2 + "]");
        System.out.println("Standard MBean compliant3:\n" +
            "\t[Expected: mxbean=false] [Got: mxbean=" + flag3 + "]");
        System.out.println("Standard MBean compliant4:\n" +
            "\t[Expected: mxbean=false] [Got: mxbean=" + flag4 + "]");
        if (flag1 != null && flag1.equals("true") &&
            flag2 != null && flag2.equals("true") &&
            flag3 != null && flag3.equals("false") &&
            flag4 != null && flag4.equals("false"))
            System.out.println("Test PASSED");
        else {
            System.out.println("Test FAILED");
            throw new Exception("invalid flags");
        }
    }
}
