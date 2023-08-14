public class MotifRadioButtonUI extends BasicRadioButtonUI {
    private static final Object MOTIF_RADIO_BUTTON_UI_KEY = new Object();
    protected Color focusColor;
    private boolean defaults_initialized = false;
    public static ComponentUI createUI(JComponent c) {
        AppContext appContext = AppContext.getAppContext();
        MotifRadioButtonUI motifRadioButtonUI =
                (MotifRadioButtonUI) appContext.get(MOTIF_RADIO_BUTTON_UI_KEY);
        if (motifRadioButtonUI == null) {
            motifRadioButtonUI = new MotifRadioButtonUI();
            appContext.put(MOTIF_RADIO_BUTTON_UI_KEY, motifRadioButtonUI);
        }
        return motifRadioButtonUI;
    }
    public void installDefaults(AbstractButton b) {
        super.installDefaults(b);
        if(!defaults_initialized) {
            focusColor = UIManager.getColor(getPropertyPrefix() + "focus");
            defaults_initialized = true;
        }
    }
    protected void uninstallDefaults(AbstractButton b) {
        super.uninstallDefaults(b);
        defaults_initialized = false;
    }
    protected Color getFocusColor() {
        return focusColor;
    }
    protected void paintFocus(Graphics g, Rectangle t, Dimension d){
        g.setColor(getFocusColor());
        g.drawRect(0,0,d.width-1,d.height-1);
    }
}
