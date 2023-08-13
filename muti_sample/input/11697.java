public class Test4918902 {
    public static void main(String[] args) {
        testPropertyDescriptor(Child1.class, Child1.class, Parent.class);
        testPropertyDescriptor(Child2.class, Parent.class, Child2.class);
        testPropertyDescriptor(Child3.class, Child3.class, Child3.class);
        testPropertyDescriptor(Child4.class, Parent.class, Parent.class);
        testPropertyDescriptor(Grandchild.class, Child3.class, Child3.class);
        testIndexedPropertyDescriptor(IChild1.class, IChild1.class, IChild1.class);
        testIndexedPropertyDescriptor(IChild2.class, IChild2.class, IParent.class);
        testIndexedPropertyDescriptor(IChild3.class, IParent.class, IChild3.class);
        testIndexedPropertyDescriptor(IChild4.class, IParent.class, IParent.class);
    }
    private static void testPropertyDescriptor(Class type, Class read, Class write) {
        PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(type, "foo");
        if (!read.equals(pd.getReadMethod().getDeclaringClass())) {
            throw new Error("unexpected read method: " + pd.getReadMethod());
        }
        if (!write.equals(pd.getWriteMethod().getDeclaringClass())) {
            throw new Error("unexpected write method: " + pd.getWriteMethod());
        }
    }
    private static void testIndexedPropertyDescriptor(Class type, Class read, Class write) {
        IndexedPropertyDescriptor ipd = BeanUtils.getIndexedPropertyDescriptor(type, "foo");
        if (!read.equals(ipd.getIndexedReadMethod().getDeclaringClass())) {
            throw new Error("unexpected read method: " + ipd.getIndexedReadMethod());
        }
        if (!write.equals(ipd.getIndexedWriteMethod().getDeclaringClass())) {
            throw new Error("unexpected write method: " + ipd.getIndexedWriteMethod());
        }
    }
    public static class Parent {
        public String getFoo() {
            return null;
        }
        public void setFoo(String str) {
        }
    }
    public static class Child1 extends Parent {
        public String getFoo() {
            return null;
        }
    }
    public static class Child2 extends Parent {
        public void setFoo(String str) {
        }
    }
    public static class Child3 extends Parent {
        public void setFoo(String str) {
        }
        public String getFoo() {
            return null;
        }
    }
    public static class Child4 extends Parent {
    }
    public static class Grandchild extends Child3 {
    }
    public static class IParent {
        public String getFoo(int i) {
            return null;
        }
        public void setFoo(int i, String str) {
        }
    }
    public static class IChild1 extends IParent {
        public void setFoo(int i, String str) {
        }
        public String getFoo(int i) {
            return null;
        }
    }
    public static class IChild2 extends IParent {
        public String getFoo(int i) {
            return null;
        }
    }
    public static class IChild3 extends IParent {
        public void setFoo(int i, String str) {
        }
    }
    public static class IChild4 extends IParent {
    }
}
