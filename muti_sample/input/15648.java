public class BasicPanelUI extends PanelUI {
    private static PanelUI panelUI;
    public static ComponentUI createUI(JComponent c) {
        if(panelUI == null) {
            panelUI = new BasicPanelUI();
        }
        return panelUI;
    }
    public void installUI(JComponent c) {
        JPanel p = (JPanel)c;
        super.installUI(p);
        installDefaults(p);
    }
    public void uninstallUI(JComponent c) {
        JPanel p = (JPanel)c;
        uninstallDefaults(p);
        super.uninstallUI(c);
    }
    protected void installDefaults(JPanel p) {
        LookAndFeel.installColorsAndFont(p,
                                         "Panel.background",
                                         "Panel.foreground",
                                         "Panel.font");
        LookAndFeel.installBorder(p,"Panel.border");
        LookAndFeel.installProperty(p, "opaque", Boolean.TRUE);
    }
    protected void uninstallDefaults(JPanel p) {
        LookAndFeel.uninstallBorder(p);
    }
    public int getBaseline(JComponent c, int width, int height) {
        super.getBaseline(c, width, height);
        Border border = c.getBorder();
        if (border instanceof AbstractBorder) {
            return ((AbstractBorder)border).getBaseline(c, width, height);
        }
        return -1;
    }
    public Component.BaselineResizeBehavior getBaselineResizeBehavior(
            JComponent c) {
        super.getBaselineResizeBehavior(c);
        Border border = c.getBorder();
        if (border instanceof AbstractBorder) {
            return ((AbstractBorder)border).getBaselineResizeBehavior(c);
        }
        return Component.BaselineResizeBehavior.OTHER;
    }
}
