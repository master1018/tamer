public final class javax_swing_KeyStroke extends AbstractTest<KeyStroke> {
    public static void main(String[] args) {
        new javax_swing_KeyStroke().test(true);
    }
    protected KeyStroke getObject() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.SHIFT_DOWN_MASK, true);
    }
    protected KeyStroke getAnotherObject() {
        return KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK);
    }
}
