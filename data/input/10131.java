public final class Test6570354 extends AbstractTest<JLabel> {
    public static void main(String[] args) {
        new Test6570354().test(true);
    }
    protected JLabel getObject() {
        JLabel label = new JLabel("");
        label.removePropertyChangeListener((PropertyChangeListener) label.getUI());
        return label;
    }
    protected JLabel getAnotherObject() {
        return new JLabel("");
    }
}
