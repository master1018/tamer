public class SunVolatileImage extends VolatileImage
    implements DestSurfaceProvider
{
    protected VolatileSurfaceManager volSurfaceManager;
    protected Component comp;
    private GraphicsConfiguration graphicsConfig;
    private Font defaultFont;
    private int width, height;
    private int forcedAccelSurfaceType;
    protected SunVolatileImage(Component comp,
                               GraphicsConfiguration graphicsConfig,
                               int width, int height, Object context,
                               int transparency, ImageCapabilities caps,
                               int accType)
    {
        this.comp = comp;
        this.graphicsConfig = graphicsConfig;
        this.width = width;
        this.height = height;
        this.forcedAccelSurfaceType = accType;
        if (!(transparency == Transparency.OPAQUE ||
            transparency == Transparency.BITMASK ||
            transparency == Transparency.TRANSLUCENT))
        {
            throw new IllegalArgumentException("Unknown transparency type:" +
                                               transparency);
        }
        this.transparency = transparency;
        this.volSurfaceManager = createSurfaceManager(context, caps);
        SurfaceManager.setManager(this, volSurfaceManager);
        volSurfaceManager.initialize();
        volSurfaceManager.initContents();
    }
    private SunVolatileImage(Component comp,
                             GraphicsConfiguration graphicsConfig,
                             int width, int height, Object context,
                             ImageCapabilities caps)
    {
        this(comp, graphicsConfig,
             width, height, context, Transparency.OPAQUE, caps, UNDEFINED);
    }
    public SunVolatileImage(Component comp, int width, int height) {
        this(comp, width, height, null);
    }
    public SunVolatileImage(Component comp,
                            int width, int height, Object context)
    {
        this(comp, comp.getGraphicsConfiguration(),
             width, height, context, null);
    }
    public SunVolatileImage(GraphicsConfiguration graphicsConfig,
                            int width, int height, int transparency,
                            ImageCapabilities caps)
    {
        this(null, graphicsConfig, width, height, null, transparency,
             caps, UNDEFINED);
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
    public GraphicsConfiguration getGraphicsConfig() {
        return graphicsConfig;
    }
    public void updateGraphicsConfig() {
        if (comp != null) {
            GraphicsConfiguration gc = comp.getGraphicsConfiguration();
            if (gc != null) {
                graphicsConfig = gc;
            }
        }
    }
    public Component getComponent() {
        return comp;
    }
    public int getForcedAccelSurfaceType() {
        return forcedAccelSurfaceType;
    }
    protected VolatileSurfaceManager createSurfaceManager(Object context,
                                                          ImageCapabilities caps)
    {
        if (graphicsConfig instanceof BufferedImageGraphicsConfig ||
            graphicsConfig instanceof sun.print.PrinterGraphicsConfig ||
            (caps != null && !caps.isAccelerated()))
        {
            return new BufImgVolatileSurfaceManager(this, context);
        }
        SurfaceManagerFactory smf = SurfaceManagerFactory.getInstance();
        return smf.createVolatileManager(this, context);
    }
    private Color getForeground() {
        if (comp != null) {
            return comp.getForeground();
        } else {
            return Color.black;
        }
    }
    private Color getBackground() {
        if (comp != null) {
            return comp.getBackground();
        } else {
            return Color.white;
        }
    }
    private Font getFont() {
        if (comp != null) {
            return comp.getFont();
        } else {
            if (defaultFont == null) {
                defaultFont = new Font("Dialog", Font.PLAIN, 12);
            }
            return defaultFont;
        }
    }
    public Graphics2D createGraphics() {
        return new SunGraphics2D(volSurfaceManager.getPrimarySurfaceData(),
                                 getForeground(),
                                 getBackground(),
                                 getFont());
    }
    public Object getProperty(String name, ImageObserver observer) {
        if (name == null) {
            throw new NullPointerException("null property name is not allowed");
        }
        return java.awt.Image.UndefinedProperty;
    }
    public int getWidth(ImageObserver observer) {
        return getWidth();
    }
    public int getHeight(ImageObserver observer) {
        return getHeight();
    }
    public BufferedImage getBackupImage() {
        return graphicsConfig.createCompatibleImage(getWidth(), getHeight(),
                                                    getTransparency());
    }
    public BufferedImage getSnapshot() {
        BufferedImage bi = getBackupImage();
        Graphics2D g = bi.createGraphics();
        g.setComposite(AlphaComposite.Src);
        g.drawImage(this, 0, 0, null);
        g.dispose();
        return bi;
    }
    public int validate(GraphicsConfiguration gc) {
        return volSurfaceManager.validate(gc);
    }
    public boolean contentsLost() {
        return volSurfaceManager.contentsLost();
    }
    public ImageCapabilities getCapabilities() {
        return volSurfaceManager.getCapabilities(graphicsConfig);
    }
    @Override
    public Surface getDestSurface() {
        return volSurfaceManager.getPrimarySurfaceData();
    }
}
