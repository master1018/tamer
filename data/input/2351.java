public class BasicTextPaneUI extends BasicEditorPaneUI {
    public static ComponentUI createUI(JComponent c) {
        return new BasicTextPaneUI();
    }
    public BasicTextPaneUI() {
        super();
    }
    protected String getPropertyPrefix() {
        return "TextPane";
    }
    public void installUI(JComponent c) {
        super.installUI(c);
    }
    protected void propertyChange(PropertyChangeEvent evt) {
        super.propertyChange(evt);
    }
}
