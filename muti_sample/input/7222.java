public final class Test5023559 extends AbstractTest {
    public static void main(String[] args) {
        new Test5023559().test(true);
    }
    protected Object getObject() {
        return new GrandParent().create().create();
    }
    protected void initialize(XMLEncoder encoder) {
        encoder.setPersistenceDelegate(Parent.class, new PersistenceDelegate() {
            protected Expression instantiate(Object old, Encoder out) {
                Parent parent = (Parent) old;
                return new Expression(old, parent.getParent(), "create", new Object[] {});
            }
        });
        encoder.setPersistenceDelegate(Child.class, new PersistenceDelegate() {
            protected Expression instantiate(Object old, Encoder out) {
                Child child = (Child) old;
                return new Expression(old, child.getParent(), "create", new Object[] {});
            }
        });
    }
    public static final class GrandParent {
        public Parent create() {
            return new Parent(this);
        }
    }
    public static final class Parent {
        private final GrandParent parent;
        private Parent(GrandParent parent) {
            this.parent = parent;
        }
        public GrandParent getParent() {
            return this.parent;
        }
        public Child create() {
            return new Child(this);
        }
    }
    public static final class Child {
        private final Parent parent;
        private Child(Parent parent) {
            this.parent = parent;
        }
        public Parent getParent() {
            return this.parent;
        }
    }
}
