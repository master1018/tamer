public class AvoidGetMBeanInfoCallsTest {
    public static class Test implements DynamicMBean {
        public Object getAttribute(String attribute)
            throws AttributeNotFoundException,
                   MBeanException,
                   ReflectionException {
            return null;
        }
        public void setAttribute(Attribute attribute)
            throws AttributeNotFoundException,
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
        public Object invoke(String actionName,
                             Object params[],
                             String signature[])
            throws MBeanException,
                   ReflectionException {
            return null;
        }
        public MBeanInfo getMBeanInfo() {
            entered = true;
            return new MBeanInfo(Test.class.getName(),
                                 "Test description",
                                 null, null, null, null);
        }
        public boolean entered;
    }
    private static void echo(String message) {
        System.out.println(message);
    }
    public static void main(String args[]) throws Exception {
        echo(">>> Create MBeanServer");
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        echo(">>> Default Domain: " + server.getDefaultDomain());
        echo(">>> Create and register Test MBean");
        Test mbean = new Test();
        ObjectName name = ObjectName.getInstance(":type=Test");
        server.registerMBean(mbean, name);
        echo(">>> Set entered flag to false in Test MBean");
        mbean.entered = false;
        echo(">>> Query Names:");
        Set<ObjectName> names = server.queryNames(null, null);
        for (ObjectName on : names) {
            echo("\t" + on.toString());
        }
        echo(">>> Entered flag = " + mbean.entered);
        if (mbean.entered) {
            echo(">>> Test FAILED!");
            throw new IllegalArgumentException("getMBeanInfo got called");
        } else {
            echo(">>> Test PASSED!");
        }
    }
}
