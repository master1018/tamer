public class OverloadTest {
    public static void main(String[] args) throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName on = new ObjectName("a:b=c");
        OverloadMXBean overloadImpl = new OverloadImpl();
        mbs.registerMBean(overloadImpl, on);
        OverloadMXBean p = JMX.newMXBeanProxy(mbs, on, OverloadMXBean.class);
        check(p.notOverloaded(5), "notOverloaded");
        check(p.overloaded(), "overloaded()");
        check(p.overloaded(5), "overloaded(int)");
        check(p.overloaded("x"), "overloaded(String)");
        check(p.overloaded(36, 64), "overloaded(int, int)");
        if (failure != null)
            throw new Exception(failure);
    }
    private static void check(String got, String expect) {
        if (!expect.equals(got)) {
            failure = "FAILED: got \"" + got + "\", expected \"" + expect + "\"";
            System.out.println(failure);
        }
    }
    public static interface OverloadMXBean {
        String notOverloaded(int x);
        String overloaded();
        String overloaded(int x);
        String overloaded(String x);
        String overloaded(int x, int y);
    }
    public static class OverloadImpl implements OverloadMXBean {
        public String notOverloaded(int x) {
            return "notOverloaded";
        }
        public String overloaded() {
            return "overloaded()";
        }
        public String overloaded(int x) {
            return "overloaded(int)";
        }
        public String overloaded(String x) {
            return "overloaded(String)";
        }
        public String overloaded(int x, int y) {
            return "overloaded(int, int)";
        }
    }
    private static String failure;
}
