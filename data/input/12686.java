public class Test4750368 {
    public static void main(String[] args) throws IntrospectionException {
        if (getLength(com.foo.test.Component.class) == getLength(Component.class)) {
            throw new Error("test failed for Component");
        }
        if (getLength(java.util.List.class) == getLength(List.class)) {
            throw new Error("test failed for List");
        }
    }
    private static int getLength(Class type) throws IntrospectionException {
        PropertyDescriptor[]  pds = Introspector.getBeanInfo(type).getPropertyDescriptors();
        System.out.println(type + ": " + pds.length);
        for (PropertyDescriptor pd : pds) {
            System.out.println(" - " + pd.getName());
        }
        return pds.length;
    }
}
