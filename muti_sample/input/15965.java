public class X11GraphicsConfig extends GraphicsConfiguration
    implements SurfaceManager.ProxiedGraphicsConfig
{
    protected X11GraphicsDevice screen;
    protected int visual;
    int depth;
    int colormap;
    ColorModel colorModel;
    long aData;
    boolean doubleBuffer;
    private Object disposerReferent = new Object();
    private BufferCapabilities bufferCaps;
    private static ImageCapabilities imageCaps =
        new ImageCapabilities(X11SurfaceData.isAccelerationEnabled());
    protected int bitsPerPixel;
    protected SurfaceType surfaceType;
    public RenderLoops solidloops;
    public static X11GraphicsConfig getConfig(X11GraphicsDevice device,
                                              int visualnum, int depth,
                                              int colormap,
                                              boolean doubleBuffer)
    {
        return new X11GraphicsConfig(device, visualnum, depth, colormap, doubleBuffer);
    }
    public static X11GraphicsConfig getConfig(X11GraphicsDevice device,
                                              int visualnum, int depth,
                                              int colormap, int type)
    {
        return new X11GraphicsConfig(device, visualnum, depth, colormap, false);
    }
    private native int getNumColors();
    private native void init(int visualNum, int screen);
    private native ColorModel makeColorModel();
    protected X11GraphicsConfig(X11GraphicsDevice device,
                                int visualnum, int depth,
                                int colormap, boolean doubleBuffer)
    {
        this.screen = device;
        this.visual = visualnum;
        this.doubleBuffer = doubleBuffer;
        this.depth = depth;
        this.colormap = colormap;
        init (visualnum, screen.getScreen());
        long x11CfgData = getAData();
        Disposer.addRecord(disposerReferent,
                           new X11GCDisposerRecord(x11CfgData));
    }
    public GraphicsDevice getDevice() {
        return screen;
    }
    public int getVisual () {
        return visual;
    }
    public int getDepth () {
        return depth;
    }
    public int getColormap () {
        return colormap;
    }
    public int getBitsPerPixel() {
        return bitsPerPixel;
    }
    public synchronized SurfaceType getSurfaceType() {
        if (surfaceType != null) {
            return surfaceType;
        }
        surfaceType = X11SurfaceData.getSurfaceType(this, Transparency.OPAQUE);
        return surfaceType;
    }
    public Object getProxyKey() {
        return screen.getProxyKeyFor(getSurfaceType());
    }
    public synchronized RenderLoops getSolidLoops(SurfaceType stype) {
        if (solidloops == null) {
            solidloops = SurfaceData.makeRenderLoops(SurfaceType.OpaqueColor,
                                                     CompositeType.SrcNoEa,
                                                     stype);
        }
        return solidloops;
    }
    public synchronized ColorModel getColorModel() {
        if (colorModel == null)  {
            java.awt.SystemColor.window.getRGB();
            colorModel = makeColorModel();
            if (colorModel == null)
                colorModel = Toolkit.getDefaultToolkit ().getColorModel ();
        }
        return colorModel;
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
    public static DirectColorModel createDCM32(int rMask, int gMask, int bMask,
                                               int aMask, boolean aPre) {
        return new DirectColorModel(
            ColorSpace.getInstance(ColorSpace.CS_sRGB),
            32, rMask, gMask, bMask, aMask, aPre, DataBuffer.TYPE_INT);
    }
    public static ComponentColorModel createABGRCCM() {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        int[] nBits = {8, 8, 8, 8};
        int[] bOffs = {3, 2, 1, 0};
        return new ComponentColorModel(cs, nBits, true, true,
                                       Transparency.TRANSLUCENT,
                                       DataBuffer.TYPE_BYTE);
    }
    public AffineTransform getDefaultTransform() {
        return new AffineTransform();
    }
    public AffineTransform getNormalizingTransform() {
        double xscale = getXResolution(screen.getScreen()) / 72.0;
        double yscale = getYResolution(screen.getScreen()) / 72.0;
        return new AffineTransform(xscale, 0.0, 0.0, yscale, 0.0, 0.0);
    }
    private native double getXResolution(int screen);
    private native double getYResolution(int screen);
    public long getAData() {
        return aData;
    }
    public String toString() {
        return ("X11GraphicsConfig[dev="+screen+
                ",vis=0x"+Integer.toHexString(visual)+
                "]");
    }
    private static native void initIDs();
    static {
        initIDs ();
    }
    public Rectangle getBounds() {
        return pGetBounds(screen.getScreen());
    }
    public native Rectangle pGetBounds(int screenNum);
    private static class XDBECapabilities extends BufferCapabilities {
        public XDBECapabilities() {
            super(imageCaps, imageCaps, FlipContents.UNDEFINED);
        }
    }
    public BufferCapabilities getBufferCapabilities() {
        if (bufferCaps == null) {
            if (doubleBuffer) {
                bufferCaps = new XDBECapabilities();
            } else {
                bufferCaps = super.getBufferCapabilities();
            }
        }
        return bufferCaps;
    }
    public ImageCapabilities getImageCapabilities() {
        return imageCaps;
    }
    public boolean isDoubleBuffered() {
        return doubleBuffer;
    }
    private static native void dispose(long x11ConfigData);
    private static class X11GCDisposerRecord implements DisposerRecord {
        private long x11ConfigData;
        public X11GCDisposerRecord(long x11CfgData) {
            this.x11ConfigData = x11CfgData;
        }
        public synchronized void dispose() {
            if (x11ConfigData != 0L) {
                X11GraphicsConfig.dispose(x11ConfigData);
                x11ConfigData = 0L;
            }
        }
    }
    public SurfaceData createSurfaceData(X11ComponentPeer peer) {
        return X11SurfaceData.createData(peer);
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
    private native long createBackBuffer(long window, int swapAction);
    private native void swapBuffers(long window, int swapAction);
    public long createBackBuffer(X11ComponentPeer peer,
                                 int numBuffers, BufferCapabilities caps)
        throws AWTException
    {
        if (!X11GraphicsDevice.isDBESupported()) {
            throw new AWTException("Page flipping is not supported");
        }
        if (numBuffers > 2) {
            throw new AWTException(
                "Only double or single buffering is supported");
        }
        BufferCapabilities configCaps = getBufferCapabilities();
        if (!configCaps.isPageFlipping()) {
            throw new AWTException("Page flipping is not supported");
        }
        long window = peer.getContentWindow();
        int swapAction = getSwapAction(caps.getFlipContents());
        return createBackBuffer(window, swapAction);
    }
    public native void destroyBackBuffer(long backBuffer);
    public VolatileImage createBackBufferImage(Component target,
                                               long backBuffer)
    {
        return new SunVolatileImage(target,
                                    target.getWidth(), target.getHeight(),
                                    Long.valueOf(backBuffer));
    }
    public void flip(X11ComponentPeer peer,
                     Component target, VolatileImage xBackBuffer,
                     int x1, int y1, int x2, int y2,
                     BufferCapabilities.FlipContents flipAction)
    {
        long window = peer.getContentWindow();
        int swapAction = getSwapAction(flipAction);
        swapBuffers(window, swapAction);
    }
    private static int getSwapAction(
        BufferCapabilities.FlipContents flipAction) {
        if (flipAction == BufferCapabilities.FlipContents.BACKGROUND) {
            return 0x01;
        } else if (flipAction == BufferCapabilities.FlipContents.PRIOR) {
            return 0x02;
        } else if (flipAction == BufferCapabilities.FlipContents.COPIED) {
            return 0x03;
        } else {
            return 0x00; 
        }
    }
    @Override
    public boolean isTranslucencyCapable() {
        return isTranslucencyCapable(getAData());
    }
    private native boolean isTranslucencyCapable(long x11ConfigData);
}
