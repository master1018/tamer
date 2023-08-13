public class Win32GraphicsConfig extends GraphicsConfiguration
    implements DisplayChangedListener, SurfaceManager.ProxiedGraphicsConfig
{
    protected Win32GraphicsDevice screen;
    protected int visual;  
    protected RenderLoops solidloops;
    private static native void initIDs();
    static {
        initIDs();
    }
    public static Win32GraphicsConfig getConfig(Win32GraphicsDevice device,
                                                int pixFormatID)
    {
        return new Win32GraphicsConfig(device, pixFormatID);
    }
    @Deprecated
    public Win32GraphicsConfig(GraphicsDevice device, int visualnum) {
        this.screen = (Win32GraphicsDevice)device;
        this.visual = visualnum;
        ((Win32GraphicsDevice)device).addDisplayChangedListener(this);
    }
    public GraphicsDevice getDevice() {
        return screen;
    }
    public int getVisual() {
        return visual;
    }
    public Object getProxyKey() {
        return screen;
    }
    private SurfaceType sTypeOrig = null;
    public synchronized RenderLoops getSolidLoops(SurfaceType stype) {
        if (solidloops == null || sTypeOrig != stype) {
            solidloops = SurfaceData.makeRenderLoops(SurfaceType.OpaqueColor,
                                                     CompositeType.SrcNoEa,
                                                     stype);
            sTypeOrig = stype;
        }
        return solidloops;
    }
    public synchronized ColorModel getColorModel() {
        return screen.getColorModel();
    }
    public ColorModel getDeviceColorModel() {
        return screen.getDynamicColorModel();
    }
    public ColorModel getColorModel(int transparency) {
        switch (transparency) {
        case Transparency.OPAQUE:
            return getColorModel();
        case Transparency.BITMASK:
            return new DirectColorModel(25, 0xff0000, 0xff00, 0xff, 0x1000000);
        case Transparency.TRANSLUCENT:
            return ColorModel.getRGBdefault();
        default:
            return null;
        }
    }
    public AffineTransform getDefaultTransform() {
        return new AffineTransform();
    }
    public AffineTransform getNormalizingTransform() {
        Win32GraphicsEnvironment ge = (Win32GraphicsEnvironment)
            GraphicsEnvironment.getLocalGraphicsEnvironment();
        double xscale = ge.getXResolution() / 72.0;
        double yscale = ge.getYResolution() / 72.0;
        return new AffineTransform(xscale, 0.0, 0.0, yscale, 0.0, 0.0);
    }
    public String toString() {
        return (super.toString()+"[dev="+screen+",pixfmt="+visual+"]");
    }
    private native Rectangle getBounds(int screen);
    public Rectangle getBounds() {
        return getBounds(screen.getScreen());
    }
    public synchronized void displayChanged() {
        solidloops = null;
    }
    public void paletteChanged() {}
    public SurfaceData createSurfaceData(WComponentPeer peer,
                                         int numBackBuffers)
    {
        return GDIWindowSurfaceData.createData(peer);
    }
    public Image createAcceleratedImage(Component target,
                                        int width, int height)
    {
        ColorModel model = getColorModel(Transparency.OPAQUE);
        WritableRaster wr =
            model.createCompatibleWritableRaster(width, height);
        return new OffScreenImage(target, model, wr,
                                  model.isAlphaPremultiplied());
    }
    public void assertOperationSupported(Component target,
                                         int numBuffers,
                                         BufferCapabilities caps)
        throws AWTException
    {
        throw new AWTException(
            "The operation requested is not supported");
    }
    public VolatileImage createBackBuffer(WComponentPeer peer) {
        Component target = (Component)peer.getTarget();
        return new SunVolatileImage(target,
                                    target.getWidth(), target.getHeight(),
                                    Boolean.TRUE);
    }
    public void flip(WComponentPeer peer,
                     Component target, VolatileImage backBuffer,
                     int x1, int y1, int x2, int y2,
                     BufferCapabilities.FlipContents flipAction)
    {
        if (flipAction == BufferCapabilities.FlipContents.COPIED ||
            flipAction == BufferCapabilities.FlipContents.UNDEFINED) {
            Graphics g = peer.getGraphics();
            try {
                g.drawImage(backBuffer,
                            x1, y1, x2, y2,
                            x1, y1, x2, y2,
                            null);
            } finally {
                g.dispose();
            }
        } else if (flipAction == BufferCapabilities.FlipContents.BACKGROUND) {
            Graphics g = backBuffer.getGraphics();
            try {
                g.setColor(target.getBackground());
                g.fillRect(0, 0,
                           backBuffer.getWidth(),
                           backBuffer.getHeight());
            } finally {
                g.dispose();
            }
        }
    }
    @Override
    public boolean isTranslucencyCapable() {
        return true;
    }
}
