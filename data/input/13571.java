public class WindowsTextAreaUI extends BasicTextAreaUI {
    protected Caret createCaret() {
        return new WindowsTextUI.WindowsCaret();
    }
    public static ComponentUI createUI(JComponent c) {
        return new WindowsTextAreaUI();
    }
}
