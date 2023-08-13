public class FeatureOrderTest {
    private static boolean failed;
    public static interface OrderMXBean {
        public int getMercury();
        public String getVenus();
        public void setVenus(String x);
        public BigInteger getEarth();
        public void setEarth(BigInteger x);
        public boolean isMars();
        public double getJupiter();
        public byte getSaturn();
        public short getUranus();
        public void setUranus(short x);
        public long getNeptune();
        public void neptune();
        public void uranus(int x);
        public int saturn(int x, int y);
        public short jupiter(int x, long y, double z);
        public void mars(boolean x);
        public BigInteger earth();
        public double earth(double x);  
        public String venus();
        public int mercury();
    }
    public static interface OrderMBean extends OrderMXBean {}
    public static class OrderImpl implements OrderMXBean {
        public int getMercury() {
            return 0;
        }
        public String getVenus() {
            return null;
        }
        public void setVenus(String x) {
        }
        public BigInteger getEarth() {
            return null;
        }
        public void setEarth(BigInteger x) {
        }
        public boolean isMars() {
            return true;
        }
        public double getJupiter() {
            return 0;
        }
        public byte getSaturn() {
            return 0;
        }
        public short getUranus() {
            return 0;
        }
        public void setUranus(short x) {
        }
        public long getNeptune() {
            return 0;
        }
        public void neptune() {
        }
        public void uranus(int x) {
        }
        public int saturn(int x, int y) {
            return 0;
        }
        public short jupiter(int x, long y, double z) {
            return 0;
        }
        public void mars(boolean x) {
        }
        public BigInteger earth() {
            return null;
        }
        public double earth(double x) {
            return 0;
        }
        public String venus() {
            return null;
        }
        public int mercury() {
            return 0;
        }
    }
    public static class Order extends OrderImpl implements OrderMBean {}
    private static final boolean[] booleans = {false, true};
    public static void main(String[] args) throws Exception {
        List<String> expectedAttributeNames = new ArrayList<String>();
        List<String> expectedOperationNames = new ArrayList<String>();
        for (Method m : OrderMXBean.class.getMethods()) {
            String name = m.getName();
            String attrName = null;
            if (name.startsWith("get") && !name.equals("get") &&
                    m.getParameterTypes().length == 0 &&
                    m.getReturnType() != void.class)
                attrName = name.substring(3);
            else if (name.startsWith("is") && !name.equals("is") &&
                    m.getParameterTypes().length == 0 &&
                    m.getReturnType() == boolean.class)
                attrName = name.substring(2);
            else if (name.startsWith("set") && !name.equals("set") &&
                    m.getReturnType() == void.class &&
                    m.getParameterTypes().length == 1)
                attrName = name.substring(3);
            if (attrName != null) {
                if (!expectedAttributeNames.contains(attrName))
                    expectedAttributeNames.add(attrName);
            } else
                expectedOperationNames.add(name);
        }
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        for (boolean mxbean : booleans) {
            for (boolean withStandardMBean : booleans) {
                String testName = "MXBean: " + mxbean + "; " +
                        "using javax.management.StandardMBean: " +
                        withStandardMBean;
                System.out.println("Test case: " + testName);
                Object mbean;
                if (mxbean) {
                    if (withStandardMBean) {
                        mbean = new StandardMBean(
                                new OrderImpl(), OrderMXBean.class, true);
                    } else
                        mbean = new OrderImpl();
                } else {
                    if (withStandardMBean)
                        mbean = new StandardMBean(new Order(), OrderMBean.class);
                    else
                        mbean = new Order();
                }
                ObjectName name = new ObjectName(
                        ":mxbean=" + mxbean + "," + "withStandardMBean=" +
                        withStandardMBean);
                mbs.registerMBean(mbean, name);
                MBeanInfo mbi = mbs.getMBeanInfo(name);
                boolean isWithStandardMBean =
                        mbs.isInstanceOf(name, StandardMBean.class.getName());
                System.out.println("classname " +mbi.getClassName());
                String mxbeanField =
                        (String) mbi.getDescriptor().getFieldValue("mxbean");
                boolean isMXBean = "true".equalsIgnoreCase(mxbeanField);
                if (mxbean != isMXBean)
                    throw new Exception("Test error: MXBean mismatch");
                if (withStandardMBean != isWithStandardMBean)
                    throw new Exception("Test error: StandardMBean mismatch");
                MBeanAttributeInfo[] mbais = mbi.getAttributes();
                checkEqual(expectedAttributeNames.size(), mbais.length,
                        "number of attributes");
                List<String> attributeNames = new ArrayList<String>();
                for (MBeanAttributeInfo mbai : mbais)
                    attributeNames.add(mbai.getName());
                checkEqual(expectedAttributeNames, attributeNames,
                        "order of attributes");
                MBeanOperationInfo[] mbois = mbi.getOperations();
                checkEqual(expectedOperationNames.size(), mbois.length,
                        "number of operations");
                List<String> operationNames = new ArrayList<String>();
                for (MBeanOperationInfo mboi : mbois)
                    operationNames.add(mboi.getName());
                checkEqual(expectedOperationNames, operationNames,
                        "order of operations");
                System.out.println();
            }
        }
        if (failed)
            throw new Exception("TEST FAILED");
        System.out.println("TEST PASSED");
    }
    private static void checkEqual(Object expected, Object actual, String what) {
        if (expected.equals(actual))
            System.out.println("OK: " + what + " matches");
        else {
            System.out.println("FAILED: " + what + " differs:");
            System.out.println("  expected: " + expected);
            System.out.println("  actual:   " + actual);
            failed = true;
        }
    }
}
