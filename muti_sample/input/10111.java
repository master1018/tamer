public class TypeNameTest {
    public static interface TestMXBean {
        public int getInt();
        public String IntName = "int";
        public Map<String, Integer> getMapSI();
        public String MapSIName = "java.util.Map<java.lang.String, java.lang.Integer>";
        public Map<String, int[]> getMapSInts();
        public String MapSIntsName = "java.util.Map<java.lang.String, int[]>";
        public List<List<int[]>> getListListInts();
        public String ListListIntsName = "java.util.List<java.util.List<int[]>>";
    }
    private static InvocationHandler nullIH = new InvocationHandler() {
        public Object invoke(Object proxy, Method method, Object[] args)
                throws Throwable {
            return null;
        }
    };
    static volatile String failure;
    public static void main(String[] args) throws Exception {
        TestMXBean testImpl = (TestMXBean) Proxy.newProxyInstance(
                TestMXBean.class.getClassLoader(), new Class<?>[] {TestMXBean.class}, nullIH);
        Object mxbean = new StandardMBean(testImpl, TestMXBean.class, true);
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        ObjectName name = new ObjectName("a:b=c");
        mbs.registerMBean(mxbean, name);
        MBeanInfo mbi = mbs.getMBeanInfo(name);
        MBeanAttributeInfo[] mbais = mbi.getAttributes();
        boolean sawTabular = false;
        for (MBeanAttributeInfo mbai : mbais) {
            String attrName = mbai.getName();
            String attrTypeName = (String) mbai.getDescriptor().getFieldValue("originalType");
            String fieldName = attrName + "Name";
            Field nameField = TestMXBean.class.getField(fieldName);
            String expectedTypeName = (String) nameField.get(null);
            if (expectedTypeName.equals(attrTypeName)) {
                System.out.println("OK: " + attrName + ": " + attrTypeName);
            } else {
                fail("For attribute " + attrName + " expected type name \"" +
                        expectedTypeName + "\", found type name \"" + attrTypeName +
                        "\"");
            }
            if (mbai.getType().equals(TabularData.class.getName())) {
                sawTabular = true;
                TabularType tt = (TabularType) mbai.getDescriptor().getFieldValue("openType");
                if (tt.getTypeName().equals(attrTypeName)) {
                    System.out.println("OK: TabularType name for " + attrName);
                } else {
                    fail("For attribute " + attrName + " expected TabularType " +
                            "name \"" + attrTypeName + "\", found \"" +
                            tt.getTypeName());
                }
            }
        }
        if (!sawTabular)
            fail("Test bug: did not test TabularType name");
        if (failure == null)
            System.out.println("TEST PASSED");
        else
            throw new Exception("TEST FAILED: " + failure);
    }
    private static void fail(String why) {
        System.out.println("FAIL: " + why);
        failure = why;
    }
}
