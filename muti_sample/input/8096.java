public class BasicCheckBoxUI extends BasicRadioButtonUI {
    private static final Object BASIC_CHECK_BOX_UI_KEY = new Object();
    private final static String propertyPrefix = "CheckBox" + ".";
    public static ComponentUI createUI(JComponent b) {
        AppContext appContext = AppContext.getAppContext();
        BasicCheckBoxUI checkboxUI =
                (BasicCheckBoxUI) appContext.get(BASIC_CHECK_BOX_UI_KEY);
        if (checkboxUI == null) {
            checkboxUI = new BasicCheckBoxUI();
            appContext.put(BASIC_CHECK_BOX_UI_KEY, checkboxUI);
        }
        return checkboxUI;
    }
    public String getPropertyPrefix() {
        return propertyPrefix;
    }
}
