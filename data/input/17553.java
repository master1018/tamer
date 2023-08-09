public final class Test4936682 extends AbstractTest<Object[]> {
    public static void main(String[] args) {
        new Test4936682().test(true);
    }
    protected Object[] getObject() {
        OuterClass outer = new OuterClass();
        return new Object [] {outer.getInner(), outer};
    }
    protected void initialize(XMLEncoder encoder) {
        encoder.setPersistenceDelegate(
                OuterClass.InnerClass.class,
                new DefaultPersistenceDelegate() {
                    protected Expression instantiate(Object oldInstance, Encoder out) {
                        OuterClass.InnerClass inner = (OuterClass.InnerClass) oldInstance;
                        OuterClass outer = inner.getOuter();
                        return new Expression(inner, outer, "getInner", new Object[0]);
                    }
                }
        );
    }
    protected void validate(Object[] before, Object[] after) {
        validate(before);
        validate(after);
    }
    private static void validate(Object[] array) {
        if (2 != array.length) {
            throw new Error("unexpected array length: " + array.length);
        }
        OuterClass outer = (OuterClass) array[1];
        if (!outer.getInner().equals(array[0])) {
            throw new Error("unexpected array content");
        }
    }
    public static final class OuterClass {
        private final InnerClass inner = new InnerClass();
        public InnerClass getInner() {
            return this.inner;
        }
        public class InnerClass {
            private InnerClass() {
            }
            public OuterClass getOuter() {
                return OuterClass.this;
            }
        }
    }
}
