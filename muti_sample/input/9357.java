public final class javax_swing_JButton extends AbstractTest<JButton> {
    public static void main(String[] args) {
        new javax_swing_JButton().test(true);
    }
    protected JButton getObject() {
        return new JButton("First");
    }
    protected JButton getAnotherObject() {
        return new JButton("Second");
    }
}
