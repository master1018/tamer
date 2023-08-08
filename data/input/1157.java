public class WindowCustomizer implements IWindowCustomizer {
    private String title = "";
    private Rectangle bounds = new Rectangle();
    private int extendedState;
    private AppPreferences prefs;
    private String windowsLabel = "Window";
    public WindowCustomizer(AppPreferences prefs) {
        this.prefs = prefs;
    }
    public int getExtendedState() {
        return extendedState;
    }
    public void setExtendedState(int extendedState) {
        this.extendedState = extendedState;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public void useStoredFrameBounds(int defWidth, int defHeight) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int centerX = screenSize.width / 2;
        int centerY = screenSize.height / 2;
        int defaultX = centerX - defWidth / 2;
        int defaultY = centerY - defHeight / 2;
        bounds = prefs.getApplicationBounds((int) Math.max(0, defaultX), (int) Math.max(0, defaultY), defWidth, defHeight);
    }
    public Rectangle getInitialBounds() {
        return bounds;
    }
    public void setInitialBounds(int width, int height) {
        bounds = new Rectangle(-1, -1, width, height);
    }
    public void setInitialBounds(int x, int y, int width, int height) {
        bounds = new Rectangle(x, y, width, height);
    }
    public void useSavedLayout() {
        useSavedLayout("");
    }
    public void useSavedLayout(String path) {
        prefs.useSavedViewLayout(path);
    }
    public String getWindowsMenuLabel() {
        return windowsLabel;
    }
    public void setWindowMenuLabel(String label) {
        windowsLabel = label;
    }
}
