public class BasicFormattedTextFieldUI extends BasicTextFieldUI {
    public static ComponentUI createUI(JComponent c) {
        return new BasicFormattedTextFieldUI();
    }
    protected String getPropertyPrefix() {
        return "FormattedTextField";
    }
}
