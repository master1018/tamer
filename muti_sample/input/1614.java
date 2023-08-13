public final class Test5023552 extends AbstractTest {
    public static void main(String[] args) {
        new Test5023552().test(true);
    }
    protected Object getObject() {
        Component component = new Component();
        return component.create(component);
    }
    protected void initialize(XMLEncoder encoder) {
        encoder.setPersistenceDelegate(Container.class, new PersistenceDelegate() {
            protected Expression instantiate(Object oldInstance, Encoder out) {
                Container container = (Container) oldInstance;
                Component component = container.getComponent();
                return new Expression(container, component, "create", new Object[] {component});
            }
        });
    }
    public static final class Component {
        public Container create(Component component) {
            return new Container(component);
        }
    }
    public static final class Container {
        private final Component component;
        public Container(Component component) {
            this.component = component;
        }
        public Component getComponent() {
            return this.component;
        }
    }
}
