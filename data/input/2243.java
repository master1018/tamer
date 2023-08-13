public class MotifTextAreaUI extends BasicTextAreaUI {
    public static ComponentUI createUI(JComponent ta) {
        return new MotifTextAreaUI();
    }
    protected Caret createCaret() {
        return MotifTextUI.createCaret();
    }
}
