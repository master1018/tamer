public class SystemClassLoaderTest {
    public static void main(String[] args) throws Exception {
        System.out.println("Create the MBean server");
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        ClassLoader mbsClassLoader = mbs.getClass().getClassLoader();
        String testClassName = Test.class.getName();
        try {
            Class.forName(testClassName, true, mbsClassLoader);
            System.out.println("TEST IS INVALID: MBEANSERVER'S CLASS LOADER " +
                               "KNOWS OUR TEST CLASS");
            System.exit(1);
        } catch (ClassNotFoundException e) {
        }
        System.out.println("Create MBean from this class");
        ObjectName objectName = new ObjectName("whatever:type=whatever");
        mbs.createMBean(testClassName, objectName);
        System.out.println("Bye! Bye!");
    }
    public static class Test implements TestMBean {
        public Test() {}
    }
    public static interface TestMBean {}
}
