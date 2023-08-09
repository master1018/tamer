public abstract class GraphicsEnvironment {
    protected GraphicsEnvironment() {
    }
    public static GraphicsEnvironment getLocalGraphicsEnvironment() {
        synchronized (ContextStorage.getContextLock()) {
            if (ContextStorage.getGraphicsEnvironment() == null) {
                if (isHeadless()) {
                    ContextStorage.setGraphicsEnvironment(new HeadlessGraphicsEnvironment());
                } else {
                    CommonGraphics2DFactory g2df = (CommonGraphics2DFactory)Toolkit
                            .getDefaultToolkit().getGraphicsFactory();
                    ContextStorage.setGraphicsEnvironment(g2df
                            .createGraphicsEnvironment(ContextStorage.getWindowFactory()));
                }
            }
            return ContextStorage.getGraphicsEnvironment();
        }
    }
    public boolean isHeadlessInstance() {
        return false;
    }
    public static boolean isHeadless() {
        return "true".equals(System.getProperty("java.awt.headless"));
    }
    public Rectangle getMaximumWindowBounds() throws HeadlessException {
        return getDefaultScreenDevice().getDefaultConfiguration().getBounds();
    }
    public Point getCenterPoint() throws HeadlessException {
        Rectangle mwb = getMaximumWindowBounds();
        return new Point(mwb.width >> 1, mwb.height >> 1);
    }
    public void preferLocaleFonts() {
    }
    public void preferProportionalFonts() {
    }
    public abstract Graphics2D createGraphics(BufferedImage bufferedImage);
    public abstract Font[] getAllFonts();
    public abstract String[] getAvailableFontFamilyNames();
    public abstract String[] getAvailableFontFamilyNames(Locale locale);
    public abstract GraphicsDevice getDefaultScreenDevice() throws HeadlessException;
    public abstract GraphicsDevice[] getScreenDevices() throws HeadlessException;
}
