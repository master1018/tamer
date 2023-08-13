public class Test6422403 {
    public static void main(String[] args) {
        test(Grand.class, "array", new Object[0].getClass());
        test(Parent.class, "array", new Number[0].getClass());
        test(Parent.class, "number", Number.class);
        test(Child.class, "array", new Long[0].getClass());
        test(Child.class, "number", Long.class);
        test(Child.class, "value", Long.class);
    }
    private static void test(Class type, String name, Class expected) {
        PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(type, name);
        if (name.equals(pd.getName()))
            if (!expected.equals(pd.getPropertyType()))
                throw new Error("expected " + expected + " but " + pd.getPropertyType() + " is resolved");
    }
    private static class Grand<A> {
        private A[] array;
        public A[] getArray() {
            return this.array;
        }
        public void setArray(A...array) {
            this.array = array;
        }
    }
    private static class Parent<N extends Number> extends Grand<N> {
        private N number;
        public N getNumber() {
            return this.number;
        }
        public void setNumber(N number) {
            this.number = number;
        }
    }
    private static class Child extends Parent<Long> {
        private Long value;
        public Long getValue() {
            return this.value;
        }
        public void setValue(Long value) {
            this.value = value;
        }
    }
}
