public class Test6868189 {
    private static final String PROPERTY = "$?"; 
    private static final String GETTER = "name"; 
    private static final String SETTER = null;
    public static void main(String[] args) throws IntrospectionException {
        PropertyDescriptor[] pds = Introspector.getBeanInfo(Enumeration.class).getPropertyDescriptors();
        if ((pds.length != 1)|| !PROPERTY.equals(pds[0].getName())){
            throw new Error("unexpected property");
        }
    }
    public enum Enumeration {
        FIRST, SECOND
    }
    public static class EnumerationBeanInfo extends SimpleBeanInfo {
        @Override
        public PropertyDescriptor[] getPropertyDescriptors() {
            try {
                return new PropertyDescriptor[] {
                        new PropertyDescriptor(PROPERTY, Enumeration.class, GETTER, SETTER)
                };
            }
            catch (IntrospectionException exception) {
                throw new Error("unexpected exception", exception);
            }
        }
    }
}
