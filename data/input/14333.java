public class Test4168833 {
    public static void main(String[] args) throws Exception {
        IndexedPropertyDescriptor ipd = BeanUtils.getIndexedPropertyDescriptor(Base.class, "prop");
        if (!ipd.getIndexedPropertyType().equals(Dimension.class)) {
            error(ipd, "Base.prop property should a Dimension");
        }
        test(Sub.class);
        test(Sub2.class);
    }
    private static void test(Class type) {
        PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(type, "prop");
        if (pd instanceof IndexedPropertyDescriptor) {
            error(pd, type.getSimpleName() + ".prop should not be an indexed property");
        }
        if (!pd.getPropertyType().equals(Color.class)) {
            error(pd, type.getSimpleName() + ".prop type should be a Color");
        }
        if (null == pd.getReadMethod()) {
            error(pd, type.getSimpleName() + ".prop should have classic read method");
        }
        if (null == pd.getWriteMethod()) {
            error(pd, type.getSimpleName() + ".prop should have classic write method");
        }
    }
    private static void error(PropertyDescriptor pd, String message) {
        BeanUtils.reportPropertyDescriptor(pd);
        throw new Error(message);
    }
    public static class Base {
        public Color getProp() {
            return null;
        }
        public Dimension getProp(int i) {
            return null;
        }
    }
    public static class Sub extends Base {
        public void setProp(Color c) {
        }
    }
    public static class Base2 {
        public void setProp(Color c) {
        }
        public void setProp(int i, Dimension d) {
        }
    }
    public static class Sub2 extends Base2 {
        public Color getProp() {
            return null;
        }
    }
}
