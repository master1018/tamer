public class XMenuPeer extends XMenuItemPeer implements MenuPeer {
    private static PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.XMenuPeer");
    XMenuWindow menuWindow;
    private final static Field f_items;
    static {
        f_items = SunToolkit.getField(Menu.class, "items");
    }
    XMenuPeer(Menu target) {
        super(target);
    }
    void setContainer(XBaseMenuWindow container) {
        super.setContainer(container);
        menuWindow = new XMenuWindow(this);
    }
    public void dispose() {
        if (menuWindow != null) {
            menuWindow.dispose();
        }
        super.dispose();
    }
    public void setFont(Font font) {
        resetTextMetrics();
        XMenuWindow menuWindow = getMenuWindow();
        if (menuWindow != null) {
            menuWindow.setItemsFont(font);
        }
        repaintIfShowing();
    }
    public void addSeparator() {
        if (log.isLoggable(PlatformLogger.FINER)) log.finer("addSeparator is not implemented");
    }
    public void addItem(MenuItem item) {
        XMenuWindow menuWindow = getMenuWindow();
        if (menuWindow != null) {
            menuWindow.addItem(item);
        } else {
            if (log.isLoggable(PlatformLogger.FINE)) {
                log.fine("Attempt to use XMenuWindowPeer without window");
            }
        }
    }
    public void delItem(int index) {
        XMenuWindow menuWindow = getMenuWindow();
        if (menuWindow != null) {
            menuWindow.delItem(index);
        } else {
            if (log.isLoggable(PlatformLogger.FINE)) {
                log.fine("Attempt to use XMenuWindowPeer without window");
            }
        }
    }
    Vector getTargetItems() {
        try {
            return (Vector)f_items.get(getTarget());
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
            return null;
        }
    }
    boolean isSeparator() {
        return false;
    }
    String getShortcutText() {
        return null;
    }
    XMenuWindow getMenuWindow() {
        return menuWindow;
    }
}
