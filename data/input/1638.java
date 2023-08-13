public class MXBeanProxyTest {
    private static MBeanServer server = getPlatformMBeanServer();
    public static void main(String[] argv) throws Exception {
        boolean iae = false;
        try {
            newPlatformMXBeanProxy(server,
                                   "Invalid ObjectName",
                                   RuntimeMXBean.class);
        } catch (IllegalArgumentException e) {
            System.out.println("EXPECTED: " + e);
            iae = true;
        }
        if (!iae) {
            throw new RuntimeException("Invalid ObjectName " +
                " was not detected");
        }
        try {
            newPlatformMXBeanProxy(server,
                                   "java.lang:type=Foo",
                                   RuntimeMXBean.class);
            iae = false;
        } catch (IllegalArgumentException e) {
            System.out.println("EXPECTED: " + e);
            iae = true;
        }
        if (!iae) {
            throw new RuntimeException("Non existent MXBean " +
                " was not detected");
        }
        try {
            newPlatformMXBeanProxy(server,
                                   RUNTIME_MXBEAN_NAME,
                                   ClassLoadingMXBean.class);
            iae = false;
        } catch (IllegalArgumentException e) {
            System.out.println("EXPECTED: " + e);
            iae = true;
        }
        if (!iae) {
            throw new RuntimeException("Mismatched MXBean interface " +
                " was not detected");
        }
        final FooMBean foo = new Foo();
        final ObjectName objName = new ObjectName("java.lang:type=Foo");
        server.registerMBean(foo, objName);
        try {
            newPlatformMXBeanProxy(server,
                                   "java.lang:type=Foo",
                                   FooMBean.class);
            iae = false;
        } catch (IllegalArgumentException e) {
            System.out.println("EXPECTED: " + e);
            iae = true;
        }
        if (!iae) {
            throw new RuntimeException("Non-platform MXBean " +
                " was not detected");
        }
        RuntimeMXBean rm = newPlatformMXBeanProxy(server,
                                                  RUNTIME_MXBEAN_NAME,
                                                  RuntimeMXBean.class);
        System.out.println("VM uptime = " + rm.getUptime());
        System.out.println("Test passed.");
    }
    public interface FooMBean {
        public boolean isFoo();
    }
    static class Foo implements FooMBean {
        public boolean isFoo() {
            return true;
        }
    }
}
