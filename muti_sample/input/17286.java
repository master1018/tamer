public class WindowsEditorPaneUI extends BasicEditorPaneUI
{
    public static ComponentUI createUI(JComponent c) {
        return new WindowsEditorPaneUI();
    }
    protected Caret createCaret() {
        return new WindowsTextUI.WindowsCaret();
    }
}
