public final class java_awt_Component extends AbstractTest<Component> {
    public static void main(String[] args) {
        new java_awt_Component().test(true);
    }
    @Override
    protected Component getObject() {
        Component component = new MyComponent();
        component.setBackground(Color.WHITE);
        component.setFont(new Font(null, Font.BOLD, 5));
        return component;
    }
    @Override
    protected Component getAnotherObject() {
        Component component = new MyComponent();
        component.setForeground(Color.BLACK);
        component.setFont(new Font(null, Font.ITALIC, 6));
        return component;
    }
    public static final class MyComponent extends Component {
    }
}
