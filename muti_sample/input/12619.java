public class Test5023557 extends AbstractTest {
    public static void main(String[] args) {
        new Test5023557().test(true);
    }
    @Override
    protected void initialize(XMLEncoder encoder) {
        encoder.setPersistenceDelegate(B.class, new BDelegate());
        encoder.setPersistenceDelegate(C.class, new CDelegate());
    }
    protected Object getObject() {
        A a = new A();
        return a.newC(a.newB());
    }
    public static class A {
        public B newB() {
            return new B(this);
        }
        public C newC(B b) {
            return new C(b);
        }
    }
    public static class B {
        private final A a;
        private B(A a) {
            this.a = a;
        }
        public A getA() {
            return this.a;
        }
    }
    public static class C {
        private final B b;
        private C(B b) {
            this.b = b;
        }
        public B getB() {
            return this.b;
        }
    }
    public static class BDelegate extends DefaultPersistenceDelegate {
        protected Expression instantiate(Object old, Encoder out) {
            B b = (B) old;
            return new Expression(b, b.getA(), "newB", new Object[0]);
        }
    }
    public static class CDelegate extends DefaultPersistenceDelegate {
        protected Expression instantiate(Object old, Encoder out) {
            C c = (C) old;
            return new Expression(c, c.getB().getA(), "newC", new Object[] { c.getB() });
        }
    }
}
