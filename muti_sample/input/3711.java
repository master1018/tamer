public class MotifTextFieldUI extends BasicTextFieldUI {
    public static ComponentUI createUI(JComponent c) {
        return new MotifTextFieldUI();
    }
    protected Caret createCaret() {
        return MotifTextUI.createCaret();
    }
}
