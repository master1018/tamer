public class MotifCheckBoxUI extends MotifRadioButtonUI {
    private static final Object MOTIF_CHECK_BOX_UI_KEY = new Object();
    private final static String propertyPrefix = "CheckBox" + ".";
    private boolean defaults_initialized = false;
    public static ComponentUI createUI(JComponent c) {
        AppContext appContext = AppContext.getAppContext();
        MotifCheckBoxUI motifCheckBoxUI =
                (MotifCheckBoxUI) appContext.get(MOTIF_CHECK_BOX_UI_KEY);
        if (motifCheckBoxUI == null) {
            motifCheckBoxUI = new MotifCheckBoxUI();
            appContext.put(MOTIF_CHECK_BOX_UI_KEY, motifCheckBoxUI);
        }
        return motifCheckBoxUI;
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
