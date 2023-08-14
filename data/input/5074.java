public class MLetCommand {
    public static void main(String[] args) throws Exception {
        if (System.getSecurityManager() == null)
            throw new IllegalStateException("No security manager installed!");
        System.out.println("java.security.policy=" +
                           System.getProperty("java.security.policy"));
        System.out.println("Create the MBean server");
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        System.out.println("Create MLet MBean");
        ObjectName mlet = new ObjectName("MLetTest:name=MLetMBean");
        mbs.createMBean("javax.management.loading.MLet", mlet);
        System.out.println("Bye! Bye!");
    }
}
