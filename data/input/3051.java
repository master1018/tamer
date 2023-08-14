public class WindowsDesktopIconUI extends BasicDesktopIconUI {
    private int width;
    public static ComponentUI createUI(JComponent c) {
        return new WindowsDesktopIconUI();
    }
    public void installDefaults() {
        super.installDefaults();
        width = UIManager.getInt("DesktopIcon.width");
    }
    public void installUI(JComponent c)   {
        super.installUI(c);
        c.setOpaque(XPStyle.getXP() == null);
    }
    public void uninstallUI(JComponent c) {
        WindowsInternalFrameTitlePane thePane =
                                        (WindowsInternalFrameTitlePane)iconPane;
        super.uninstallUI(c);
        thePane.uninstallListeners();
    }
    protected void installComponents() {
        iconPane = new WindowsInternalFrameTitlePane(frame);
        desktopIcon.setLayout(new BorderLayout());
        desktopIcon.add(iconPane, BorderLayout.CENTER);
        if (XPStyle.getXP() != null) {
            desktopIcon.setBorder(null);
        }
    }
    public Dimension getPreferredSize(JComponent c) {
        return getMinimumSize(c);
    }
    public Dimension getMinimumSize(JComponent c) {
        Dimension dim = super.getMinimumSize(c);
        dim.width = width;
        return dim;
    }
}
