public final class WGlobalCursorManager extends GlobalCursorManager {
    private static WGlobalCursorManager manager;
    public static GlobalCursorManager getCursorManager() {
        if (manager == null) {
            manager = new WGlobalCursorManager();
        }
        return manager;
    }
    public static void nativeUpdateCursor(Component heavy) {
        WGlobalCursorManager.getCursorManager().updateCursorLater(heavy);
    }
    protected native void setCursor(Component comp, Cursor cursor, boolean u);
    protected native void getCursorPos(Point p);
    protected native Component findComponentAt(Container con, int x, int y);
    protected native Component findHeavyweightUnderCursor(boolean useCache);
    protected native Point getLocationOnScreen(Component com);
}
