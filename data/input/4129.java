public class WindowsTextPaneUI extends BasicTextPaneUI
{
    public static ComponentUI createUI(JComponent c) {
        return new WindowsTextPaneUI();
    }
    protected Caret createCaret() {
        return new WindowsTextUI.WindowsCaret();
    }
}
