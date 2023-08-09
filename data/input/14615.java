public class DuplicateGetterTest {
    public static interface FooMBean {
        public MBeanNotificationInfo[] getNotificationInfo();
    }
    public static interface BazMBean {
        public MBeanNotificationInfo[] getNotificationInfo();
    }
    public static interface BarMBean extends FooMBean, BazMBean {
    }
    public static class Bar implements BarMBean {
        public MBeanNotificationInfo[] getNotificationInfo() {
            return null;
        }
    }
    public static void main(String[] args) throws Exception {
        System.out.println("Testing that inheriting the same getter from " +
                           "more than one interface does not cause problems");
        DynamicMBean mbean =
            new StandardMBean(new Bar(), BarMBean.class);
        MBeanAttributeInfo[] attrs = mbean.getMBeanInfo().getAttributes();
        System.out.println("Attributes: " + Arrays.toString(attrs));
        if (attrs.length != 1)
            throw new Exception("Wrong number of attributes: " + attrs.length);
        if (!attrs[0].getName().equals("NotificationInfo"))
            throw new Exception("Wrong attribute name: " + attrs[0].getName());
        System.out.println("Test passed");
    }
}
