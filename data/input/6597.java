public class WindowsPasswordFieldUI extends BasicPasswordFieldUI {
    public static ComponentUI createUI(JComponent c) {
        return new WindowsPasswordFieldUI();
    }
    protected Caret createCaret() {
        return new WindowsTextUI.WindowsCaret();
    }
}
