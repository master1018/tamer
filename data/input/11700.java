public class OffScreenImage extends BufferedImage {
    protected Component c;
    private OffScreenImageSource osis;
    private Font defaultFont;
    public OffScreenImage(Component c, ColorModel cm, WritableRaster raster,
                          boolean isRasterPremultiplied)
    {
        super(cm, raster, isRasterPremultiplied, null);
        this.c = c;
        initSurface(raster.getWidth(), raster.getHeight());
    }
    public Graphics getGraphics() {
        return createGraphics();
    }
    public Graphics2D createGraphics() {
        if (c == null) {
            GraphicsEnvironment env =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
            return env.createGraphics(this);
        }
        Color bg = c.getBackground();
        if (bg == null) {
            bg = SystemColor.window;
        }
        Color fg = c.getForeground();
        if (fg == null) {
            fg = SystemColor.windowText;
        }
        Font font = c.getFont();
        if (font == null) {
            if (defaultFont == null) {
                defaultFont = new Font("Dialog", Font.PLAIN, 12);
            }
            font = defaultFont;
        }
        return new SunGraphics2D(SurfaceData.getPrimarySurfaceData(this),
                                 fg, bg, font);
    }
    private void initSurface(int width, int height) {
        Graphics2D g2 = createGraphics();
        try {
            g2.clearRect(0, 0, width, height);
        } finally {
            g2.dispose();
        }
    }
    public ImageProducer getSource() {
        if (osis == null) {
            osis = new OffScreenImageSource(this);
        }
        return osis;
    }
}
