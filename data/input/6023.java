public class WindowsRadioButtonUI extends BasicRadioButtonUI
{
    private static final Object WINDOWS_RADIO_BUTTON_UI_KEY = new Object();
    protected int dashedRectGapX;
    protected int dashedRectGapY;
    protected int dashedRectGapWidth;
    protected int dashedRectGapHeight;
    protected Color focusColor;
    private boolean initialized = false;
    public static ComponentUI createUI(JComponent c) {
        AppContext appContext = AppContext.getAppContext();
        WindowsRadioButtonUI windowsRadioButtonUI =
                (WindowsRadioButtonUI) appContext.get(WINDOWS_RADIO_BUTTON_UI_KEY);
        if (windowsRadioButtonUI == null) {
            windowsRadioButtonUI = new WindowsRadioButtonUI();
            appContext.put(WINDOWS_RADIO_BUTTON_UI_KEY, windowsRadioButtonUI);
        }
        return windowsRadioButtonUI;
    }
    public void installDefaults(AbstractButton b) {
        super.installDefaults(b);
        if(!initialized) {
            dashedRectGapX = ((Integer)UIManager.get("Button.dashedRectGapX")).intValue();
            dashedRectGapY = ((Integer)UIManager.get("Button.dashedRectGapY")).intValue();
            dashedRectGapWidth = ((Integer)UIManager.get("Button.dashedRectGapWidth")).intValue();
            dashedRectGapHeight = ((Integer)UIManager.get("Button.dashedRectGapHeight")).intValue();
            focusColor = UIManager.getColor(getPropertyPrefix() + "focus");
            initialized = true;
        }
        if (XPStyle.getXP() != null) {
            LookAndFeel.installProperty(b, "rolloverEnabled", Boolean.TRUE);
        }
    }
    protected Color getFocusColor() {
        return focusColor;
    }
    protected void paintText(Graphics g, AbstractButton b, Rectangle textRect, String text) {
        WindowsGraphicsUtils.paintText(g, b, textRect, text, getTextShiftOffset());
    }
    protected void paintFocus(Graphics g, Rectangle textRect, Dimension d){
        g.setColor(getFocusColor());
        BasicGraphicsUtils.drawDashedRect(g, textRect.x, textRect.y, textRect.width, textRect.height);
    }
    public Dimension getPreferredSize(JComponent c) {
        Dimension d = super.getPreferredSize(c);
        AbstractButton b = (AbstractButton)c;
        if (d != null && b.isFocusPainted()) {
            if(d.width % 2 == 0) { d.width += 1; }
            if(d.height % 2 == 0) { d.height += 1; }
        }
        return d;
    }
}
