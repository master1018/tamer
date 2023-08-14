public class BasicPasswordFieldUI extends BasicTextFieldUI {
    public static ComponentUI createUI(JComponent c) {
        return new BasicPasswordFieldUI();
    }
    protected String getPropertyPrefix() {
        return "PasswordField";
    }
    protected void installDefaults() {
        super.installDefaults();
        String prefix = getPropertyPrefix();
        Character echoChar = (Character)UIManager.getDefaults().get(prefix + ".echoChar");
        if(echoChar != null) {
            LookAndFeel.installProperty(getComponent(), "echoChar", echoChar);
        }
    }
    public View create(Element elem) {
        return new PasswordView(elem);
    }
    ActionMap createActionMap() {
        ActionMap map = super.createActionMap();
        if (map.get(DefaultEditorKit.selectWordAction) != null) {
            Action a = map.get(DefaultEditorKit.selectLineAction);
            if (a != null) {
                map.remove(DefaultEditorKit.selectWordAction);
                map.put(DefaultEditorKit.selectWordAction, a);
            }
        }
        return map;
    }
}
