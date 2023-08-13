public class MetalCheckBoxUI extends MetalRadioButtonUI {
    private static final Object METAL_CHECK_BOX_UI_KEY = new Object();
    private final static String propertyPrefix = "CheckBox" + ".";
    private boolean defaults_initialized = false;
    public static ComponentUI createUI(JComponent b) {
        AppContext appContext = AppContext.getAppContext();
        MetalCheckBoxUI checkboxUI =
                (MetalCheckBoxUI) appContext.get(METAL_CHECK_BOX_UI_KEY);
        if (checkboxUI == null) {
            checkboxUI = new MetalCheckBoxUI();
            appContext.put(METAL_CHECK_BOX_UI_KEY, checkboxUI);
        }
        return checkboxUI;
    }
    public String getPropertyPrefix() {
        return propertyPrefix;
    }
    public void installDefaults(AbstractButton b) {
        super.installDefaults(b);
        if(!defaults_initialized) {
            icon = UIManager.getIcon(getPropertyPrefix() + "icon");
            defaults_initialized = true;
        }
    }
    protected void uninstallDefaults(AbstractButton b) {
        super.uninstallDefaults(b);
        defaults_initialized = false;
    }
}
