public class UnregisterMBeanExceptionTest {
    public static class TestDynamicMBean implements DynamicMBean {
        public Object getAttribute(String attribute) throws
            AttributeNotFoundException,
            MBeanException,
            ReflectionException {
            return null;
        }
        public void setAttribute(Attribute attribute) throws
            AttributeNotFoundException,
            InvalidAttributeValueException,
            MBeanException,
            ReflectionException {
        }
        public AttributeList getAttributes(String[] attributes) {
            return null;
        }
        public AttributeList setAttributes(AttributeList attributes) {
            return null;
        }
        public Object invoke(String op, Object params[], String sign[]) throws
            MBeanException,
            ReflectionException {
            return null;
        }
        public MBeanInfo getMBeanInfo() {
            if (throwException)
                throw new RuntimeException("UnregisterMBeanExceptionTest");
            else
                return new MBeanInfo(this.getClass().getName(), "Test",
                                     null, null, null, null);
        }
        public boolean throwException;
    }
    public static void main(String[] args) throws Exception {
        System.out.println("Create the MBean server");
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        System.out.println("Create a TestDynamicMBean");
        TestDynamicMBean obj = new TestDynamicMBean();
        ObjectName n = new ObjectName("d:k=v");
        System.out.println("Register a TestDynamicMBean");
        mbs.registerMBean(obj, n);
        obj.throwException = true;
        System.out.println("Unregister a TestDynamicMBean");
        try {
            mbs.unregisterMBean(n);
        } catch (Exception e) {
            throw new IllegalArgumentException("Test failed", e);
        }
        boolean isRegistered = mbs.isRegistered(n);
        System.out.println("Is MBean Registered? " + isRegistered);
        if (isRegistered) {
            throw new IllegalArgumentException(
                "Test failed: the MBean is still registered");
        } else {
            System.out.println("Test passed");
        }
    }
}
