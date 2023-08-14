public final class java_awt_AWTKeyStroke extends AbstractTest<AWTKeyStroke> {
    public static void main(String[] args) {
        new java_awt_AWTKeyStroke().test(true);
    }
    protected AWTKeyStroke getObject() {
        return AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_A, InputEvent.SHIFT_DOWN_MASK, true);
    }
    protected AWTKeyStroke getAnotherObject() {
        return AWTKeyStroke.getAWTKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK);
    }
}
