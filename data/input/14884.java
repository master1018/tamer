public class MotifTextPaneUI extends BasicTextPaneUI {
    public static ComponentUI createUI(JComponent c) {
        return new MotifTextPaneUI();
    }
    protected Caret createCaret() {
        return MotifTextUI.createCaret();
    }
}
