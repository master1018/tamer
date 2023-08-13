public class Test4168475 {
    private static final Class CLASS = ComponentBeanInfo.class;
    private static final String[] PATH = {"infos"};
    public static void main(String[] args) throws IntrospectionException {
        Introspector.setBeanInfoSearchPath(PATH);
        BeanInfo info = Introspector.getBeanInfo(Component.class);
        PropertyDescriptor[] pds = info.getPropertyDescriptors();
        if (pds.length != 1) {
            throw new Error("wrong number of properties");
        }
        if (!pds[0].getName().equals("name")) {
            throw new Error("unexpected property name");
        }
    }
}
