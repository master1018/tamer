public class HeadlessGraphicsEnvironment extends GraphicsEnvironment {
    private GraphicsEnvironment ge;
    public HeadlessGraphicsEnvironment(GraphicsEnvironment ge) {
        this.ge = ge;
    }
    public GraphicsDevice[] getScreenDevices()
        throws HeadlessException {
        throw new HeadlessException();
    }
    public GraphicsDevice getDefaultScreenDevice()
        throws HeadlessException {
        throw new HeadlessException();
    }
    public Point getCenterPoint() throws HeadlessException {
        throw new HeadlessException();
    }
    public Rectangle getMaximumWindowBounds() throws HeadlessException {
        throw new HeadlessException();
    }
    public Graphics2D createGraphics(BufferedImage img) {
        return ge.createGraphics(img); }
    public Font[] getAllFonts() { return ge.getAllFonts(); }
    public String[] getAvailableFontFamilyNames() {
        return ge.getAvailableFontFamilyNames(); }
    public String[] getAvailableFontFamilyNames(Locale l) {
        return ge.getAvailableFontFamilyNames(l); }
    public GraphicsEnvironment getSunGraphicsEnvironment() {
        return ge;
    }
}
