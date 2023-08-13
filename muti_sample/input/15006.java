public class MotifPasswordFieldUI extends BasicPasswordFieldUI {
    public static ComponentUI createUI(JComponent c) {
        return new MotifPasswordFieldUI();
    }
    protected Caret createCaret() {
        return MotifTextUI.createCaret();
    }
}
