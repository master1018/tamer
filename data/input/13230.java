public class MotifEditorPaneUI extends BasicEditorPaneUI {
    public static ComponentUI createUI(JComponent c) {
        return new MotifEditorPaneUI();
    }
    protected Caret createCaret() {
        return MotifTextUI.createCaret();
    }
}
