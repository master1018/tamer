public final class Test6437265 extends AbstractTest<JPanel> {
    private static final String[] NAMES = {
            BorderLayout.EAST,
            BorderLayout.WEST,
            BorderLayout.NORTH,
            BorderLayout.SOUTH,
            BorderLayout.CENTER,
            BorderLayout.LINE_END,
            BorderLayout.PAGE_END,
            BorderLayout.LINE_START,
            BorderLayout.PAGE_START};
    public static void main(String[] args) {
        new Test6437265().test(true);
    }
    protected JPanel getObject() {
        JPanel panel = new MyPanel();
        for (String name : NAMES) {
            panel.add(name, new JLabel(name));
        }
        return panel;
    }
    protected void validate(JPanel before, JPanel after) {
        validate(before);
        validate(after);
        super.validate(before, after);
    }
    private static void validate(JPanel panel) {
        BorderLayout layout = (BorderLayout) panel.getLayout();
        for (Component component : panel.getComponents()) {
            String name = (String) layout.getConstraints(component);
            if (name == null)
                throw new Error("The component is not layed out: " + component);
            JLabel label = (JLabel) component;
            if (!name.equals(label.getText()))
                throw new Error("The component is layed out on " + name + ": " + component);
        }
    }
    public static final class MyPanel extends JPanel {
        public MyPanel() {
            super(new BorderLayout(3, 3));
        }
    }
}
