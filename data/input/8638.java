public final class javax_swing_DefaultCellEditor extends AbstractTest<DefaultCellEditor> {
    public static void main(String[] args) {
        new javax_swing_DefaultCellEditor().test(true);
    }
    protected DefaultCellEditor getObject() {
        return new DefaultCellEditor(new JTextField("First"));
    }
    protected DefaultCellEditor getAnotherObject() {
        return null; 
    }
    protected void validate(DefaultCellEditor before, DefaultCellEditor after) {
        String text = ((JTextComponent) after.getComponent()).getText();
        if (!text.equals(((JTextComponent) before.getComponent()).getText()))
            throw new Error("Invalid text in component: " + text);
    }
}
