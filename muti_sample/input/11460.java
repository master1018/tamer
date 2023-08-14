public class BasicViewportUI extends ViewportUI {
    private static ViewportUI viewportUI;
    public static ComponentUI createUI(JComponent c) {
        if(viewportUI == null) {
            viewportUI = new BasicViewportUI();
        }
        return viewportUI;
    }
    public void installUI(JComponent c) {
        super.installUI(c);
        installDefaults(c);
    }
    public void uninstallUI(JComponent c) {
        uninstallDefaults(c);
        super.uninstallUI(c);
    }
    protected void installDefaults(JComponent c) {
        LookAndFeel.installColorsAndFont(c,
                                         "Viewport.background",
                                         "Viewport.foreground",
                                         "Viewport.font");
        LookAndFeel.installProperty(c, "opaque", Boolean.TRUE);
    }
    protected void uninstallDefaults(JComponent c) {
    }
}
