public class SetWrongTypeAttributeTest {
    private static final Object[][] TEST_VALUES = {
        {"Foo", null, 5, "false", Collections.<String,String>emptyMap()},
        {"Name", 5, false, Collections.<String,String>emptyMap()},
        {"Properties", 5, false, Collections.singleton("foo")},
    };
    public static interface BlahMBean {
        public boolean isFoo();
        public void setFoo(boolean foo);
        public String getName();
        public void setName(String name);
        public Map<String,String> getProperties();
        public void setProperties(Map<String,String> map);
    }
    public static interface BlahMXBean {
        public boolean isFoo();
        public void setFoo(boolean foo);
        public String getName();
        public void setName(String name);
        public Map<String,String> getProperties();
        public void setProperties(Map<String,String> map);
    }
    public static class BlahBase {
        public boolean isFoo() {
            return foo;
        }
        public void setFoo(boolean foo) {
            this.foo = foo;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public Map<String,String> getProperties() {
            return properties;
        }
        public void setProperties(Map<String,String> map) {
            this.properties = map;
        }
        private boolean foo;
        private String name;
        private Map<String,String> properties;
    }
    public static class Blah extends BlahBase implements BlahMBean {}
    public static class MXBlah extends BlahBase implements BlahMXBean {}
    public static class StdBlah extends StandardMBean implements BlahMBean {
        public StdBlah() throws NotCompliantMBeanException {
            super(BlahMBean.class);
        }
        public boolean isFoo() {
            return foo;
        }
        public void setFoo(boolean foo) {
            this.foo = foo;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public Map<String,String> getProperties() {
            return properties;
        }
        public void setProperties(Map<String,String> map) {
            this.properties = map;
        }
        private boolean foo;
        private String name;
        private Map<String,String> properties;
    }
    public static class StdMXBlah extends StandardMBean implements BlahMXBean {
        public StdMXBlah() throws NotCompliantMBeanException {
            super(BlahMXBean.class, true);
        }
        public boolean isFoo() {
            return foo;
        }
        public void setFoo(boolean foo) {
            this.foo = foo;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public Map<String,String> getProperties() {
            return properties;
        }
        public void setProperties(Map<String,String> map) {
            this.properties = map;
        }
        private boolean foo;
        private String name;
        private Map<String,String> properties;
    }
    public static void main(String[] args) throws Exception {
        test("Standard Blah", new Blah());
        test("StandardMBean implementing Blah", new StdBlah());
        test("StandardMBean wrapping Blah",
             new StandardMBean(new Blah(), BlahMBean.class));
        test("MXBean Blah", new MXBlah());
        test("StandardMBean implementing MXBean Blah", new StdMXBlah());
        test("StandardMBean wrapping MXBean Blah",
             new StandardMBean(new MXBlah(), BlahMXBean.class, true));
        if (failure == null)
            System.out.println("TEST PASSED");
        else
            throw new Exception("TEST FAILED: " + failure);
    }
    private static void test(String what, Object obj) throws Exception {
        System.out.println(what + "...");
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        ObjectName on = new ObjectName("a:b=c");
        mbs.registerMBean(obj, on);
        for (Object[] testValue : TEST_VALUES) {
            String attrName = (String) testValue[0];
            for (int i = 1; i < testValue.length; i++) {
                Object value = testValue[i];
                final String doing =
                    "setAttribute(" + attrName + ", " + value + ")";
                try {
                    mbs.setAttribute(on, new Attribute("Foo", 5));
                    fail(what, doing + " succeeded but should fail!");
                } catch (InvalidAttributeValueException e) {
                    final String msg =
                        doing + ": OK, got expected " +
                        "InvalidAttributeValueException";
                    System.out.println(msg);
                } catch (Throwable e) {
                    fail(what, doing + " got wrong exception: " + e);
                    e.printStackTrace(System.out);
                }
            }
        }
    }
    private static void fail(String what, String msg) {
        failure = what + ": " + msg;
        System.out.println("FAILED: " + failure);
    }
    private static String failure;
}
