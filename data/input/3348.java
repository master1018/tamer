public class Test6528714 {
    public static void main(String[] args) {
        test(Concrete.class, "id", Integer.class);
    }
    private static void test(Class type, String name, Class expected) {
        PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(type, name);
        if (name.equals(pd.getName()))
            if (!expected.equals(pd.getPropertyType()))
                throw new Error("expected " + expected + " but " + pd.getPropertyType() + " is resolved");
    }
    private static interface Abstract {
        Number getId();
    }
    private static class Concrete implements Abstract {
        private Integer id;
        public Integer getId() {
            return this.id;
        }
    }
}
