public class Test4679556 extends AbstractTest {
    public static void main(String[] args) {
        new Test4679556().test(true);
    }
    protected Object getObject() {
        A a = new A();
        B b = (B) a.getX();
        b.setX(a.createC());
        return a;
    }
    protected Object getAnotherObject() {
        return new A();
    }
    protected void initialize(XMLEncoder encoder) {
        encoder.setExceptionListener(this);
        encoder.setPersistenceDelegate(C.class, new DefaultPersistenceDelegate() {
            protected Expression instantiate(Object oldInstance, Encoder out) {
                C c = (C) oldInstance;
                return new Expression(c, c.getX(), "createC", new Object[] {});
            }
        });
    }
    public static class Base {
        private Object x;
        public Object getX() {
            return this.x;
        }
        public void setX(Object x) {
            this.x = x;
        }
    }
    public static class A extends Base {
        public A() {
            setX(new B());
        }
        public C createC() {
            return new C(this);
        }
    }
    public static class B extends Base {
    }
    public static class C extends Base {
        private C(Object x) {
            setX(x);
        }
    }
}
