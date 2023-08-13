public final class javax_swing_BoxLayout extends AbstractTest<BoxLayout> {
    public static void main(String[] args) {
        new javax_swing_BoxLayout().test(true);
    }
    protected BoxLayout getObject() {
        return new BoxLayout(new JLabel("TEST"), BoxLayout.LINE_AXIS);
    }
    protected BoxLayout getAnotherObject() {
        return null; 
    }
}
