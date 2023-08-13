public class MetalMenuBarUI extends BasicMenuBarUI  {
    public static ComponentUI createUI(JComponent x) {
        if (x == null) {
            throw new NullPointerException("Must pass in a non-null component");
        }
        return new MetalMenuBarUI();
    }
    public void installUI(JComponent c) {
        super.installUI(c);
        MetalToolBarUI.register(c);
    }
    public void uninstallUI(JComponent c) {
        super.uninstallUI(c);
        MetalToolBarUI.unregister(c);
    }
    public void update(Graphics g, JComponent c) {
        boolean isOpaque = c.isOpaque();
        if (g == null) {
            throw new NullPointerException("Graphics must be non-null");
        }
        if (isOpaque && (c.getBackground() instanceof UIResource) &&
                        UIManager.get("MenuBar.gradient") != null) {
            if (MetalToolBarUI.doesMenuBarBorderToolBar((JMenuBar)c)) {
                JToolBar tb = (JToolBar)MetalToolBarUI.
                     findRegisteredComponentOfType(c, JToolBar.class);
                if (tb.isOpaque() &&tb.getBackground() instanceof UIResource) {
                    MetalUtils.drawGradient(c, g, "MenuBar.gradient", 0, 0,
                                            c.getWidth(), c.getHeight() +
                                            tb.getHeight(), true);
                    paint(g, c);
                    return;
                }
            }
            MetalUtils.drawGradient(c, g, "MenuBar.gradient", 0, 0,
                                    c.getWidth(), c.getHeight(),true);
            paint(g, c);
        }
        else {
            super.update(g, c);
        }
    }
}
