public class Test4520754 {
    private static final Class[] COMPILE = {
            ComponentBeanInfo.class,
            FooBarBeanInfo.class,
            WombatBeanInfo.class,
    };
    public static void main(String[] args) {
        test4168475(Component.class);
        test(null, Button.class, Component.class, List.class, Menu.class, Panel.class);
        test(null, JApplet.class, JButton.class, JCheckBox.class);
        test(Boolean.TRUE, Wombat.class, Foo.class, FooBar.class);
    }
    private static void test(Boolean mark, Class... types) {
        for (Class type : types) {
            BeanInfo info = getBeanInfo(mark, type);
            if (info == null) {
                throw new Error("could not find BeanInfo for " + type);
            }
            if (mark != info.getBeanDescriptor().getValue("test")) {
                throw new Error("could not find marked BeanInfo for " + type);
            }
        }
        Introspector.flushCaches();
    }
    private static BeanInfo getBeanInfo(Boolean mark, Class type) {
        System.out.println("test=" + mark + " for " + type);
        BeanInfo info;
        try {
            info = Introspector.getBeanInfo(type);
        } catch (IntrospectionException exception) {
            throw new Error("unexpected exception", exception);
        }
        if (info == null) {
            throw new Error("could not find BeanInfo for " + type);
        }
        if (mark != info.getBeanDescriptor().getValue("test")) {
            throw new Error("could not find marked BeanInfo for " + type);
        }
        return info;
    }
    private static void test4168475(Class type) {
        String[] newPath = {"infos"};
        String[] oldPath = Introspector.getBeanInfoSearchPath();
        Introspector.setBeanInfoSearchPath(newPath);
        BeanInfo info = getBeanInfo(Boolean.TRUE, type);
        Introspector.setBeanInfoSearchPath(oldPath);
        PropertyDescriptor[] pds = info.getPropertyDescriptors();
        if (pds.length != 1) {
            throw new Error("could not find custom BeanInfo for " + type);
        }
        Introspector.flushCaches();
    }
}
