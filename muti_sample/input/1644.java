public class ComparatorExceptionTest {
    public static interface TestMXBean {
        public SortedSet<String> getSortedSet();
        public SortedMap<String, String> getSortedMap();
    }
    public static class TestImpl implements TestMXBean {
        public SortedSet<String> getSortedSet() {
            return new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        }
        public SortedMap<String, String> getSortedMap() {
            return new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        }
    }
    private static String failure;
    private static void fail(String why) {
        failure = "FAILED: " + why;
        System.out.println(failure);
    }
    public static void main(String[] args) throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        ObjectName name = new ObjectName("a:b=c");
        mbs.registerMBean(new TestImpl(), name);
        for (String attr : new String[] {"SortedSet", "SortedMap"}) {
            try {
                Object value = mbs.getAttribute(name, attr);
                fail("get " + attr + " did not throw exception");
            } catch (Exception e) {
                Throwable t = e;
                while (!(t instanceof IllegalArgumentException)) {
                    if (t == null)
                        break;
                    t = t.getCause();
                }
                if (t != null)
                    System.out.println("Correct exception for " + attr);
                else {
                    fail("get " + attr + " got wrong exception");
                    e.printStackTrace(System.out);
                }
            }
        }
        if (failure != null)
            throw new Exception(failure);
    }
}
