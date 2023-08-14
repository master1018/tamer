public class MetalLabelUI extends BasicLabelUI
{
    protected static MetalLabelUI metalLabelUI = new MetalLabelUI();
    private static final Object METAL_LABEL_UI_KEY = new Object();
    public static ComponentUI createUI(JComponent c) {
        if (System.getSecurityManager() != null) {
            AppContext appContext = AppContext.getAppContext();
            MetalLabelUI safeMetalLabelUI =
                    (MetalLabelUI) appContext.get(METAL_LABEL_UI_KEY);
            if (safeMetalLabelUI == null) {
                safeMetalLabelUI = new MetalLabelUI();
                appContext.put(METAL_LABEL_UI_KEY, safeMetalLabelUI);
            }
            return safeMetalLabelUI;
        }
        return metalLabelUI;
    }
    protected void paintDisabledText(JLabel l, Graphics g, String s, int textX, int textY)
    {
        int mnemIndex = l.getDisplayedMnemonicIndex();
        g.setColor(UIManager.getColor("Label.disabledForeground"));
        SwingUtilities2.drawStringUnderlineCharAt(l, g, s, mnemIndex,
                                                   textX, textY);
    }
}
