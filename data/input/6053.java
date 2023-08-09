public class GetMBeanInfoExceptionTest {
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
            throw new IllegalArgumentException("GetMBeanInfoExceptionTest");
        }
    }
    public static void main(String[] args) throws Exception {
        int error = 0;
        System.out.println("Create the MBean server");
        MBeanServer mbs = MBeanServerFactory.createMBeanServer();
        System.out.println("Create a TestDynamicMBean");
        TestDynamicMBean obj = new TestDynamicMBean();
        ObjectName n = new ObjectName("d:k=v");
        try {
            mbs.registerMBean(obj, n);
            System.out.println("Didn't get expected NotCompliantMBeanException");
            error++;
        } catch (NotCompliantMBeanException e) {
            boolean found = false;
            Throwable t = e.getCause();
            while (t != null) {
                if (t instanceof IllegalArgumentException &&
                    "GetMBeanInfoExceptionTest".equals(t.getMessage())) {
                    found = true;
                }
                t = t.getCause();
            }
            if (found) {
                System.out.println("Found expected IllegalArgumentException");
            } else {
                System.out.println("Didn't find expected IllegalArgumentException");
                error++;
            }
        } catch (Exception e) {
            System.out.println("Got " + e.getClass().getName() +
                "instead of expected NotCompliantMBeanException");
            error++;
        }
        if (error > 0) {
            System.out.println("Test failed");
            throw new IllegalArgumentException("Test failed");
        } else {
            System.out.println("Test passed");
        }
    }
}
