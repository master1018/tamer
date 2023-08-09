public abstract class X11SurfaceData extends XSurfaceData {
    X11ComponentPeer peer;
    X11GraphicsConfig graphicsConfig;
    private RenderLoops solidloops;
    protected int depth;
    private static native void initIDs(Class xorComp, boolean tryDGA);
    protected native void initSurface(int depth, int width, int height,
                                      long drawable);
    public static final String
        DESC_INT_BGR_X11        = "Integer BGR Pixmap";
    public static final String
        DESC_INT_RGB_X11        = "Integer RGB Pixmap";
    public static final String
        DESC_4BYTE_ABGR_PRE_X11 = "4 byte ABGR Pixmap with pre-multplied alpha";
    public static final String
        DESC_INT_ARGB_PRE_X11   = "Integer ARGB Pixmap with pre-multiplied " +
                                  "alpha";
    public static final String
        DESC_BYTE_IND_OPQ_X11   = "Byte Indexed Opaque Pixmap";
    public static final String
        DESC_INT_BGR_X11_BM     = "Integer BGR Pixmap with 1-bit transp";
    public static final String
        DESC_INT_RGB_X11_BM     = "Integer RGB Pixmap with 1-bit transp";
    public static final String
        DESC_BYTE_IND_X11_BM    = "Byte Indexed Pixmap with 1-bit transp";
    public static final String
        DESC_BYTE_GRAY_X11      = "Byte Gray Opaque Pixmap";
    public static final String
        DESC_INDEX8_GRAY_X11    = "Index8 Gray Opaque Pixmap";
    public static final String
        DESC_BYTE_GRAY_X11_BM   = "Byte Gray Opaque Pixmap with 1-bit transp";
    public static final String
        DESC_INDEX8_GRAY_X11_BM = "Index8 Gray Opaque Pixmap with 1-bit transp";
    public static final String
        DESC_3BYTE_RGB_X11      = "3 Byte RGB Pixmap";
    public static final String
        DESC_3BYTE_BGR_X11      = "3 Byte BGR Pixmap";
    public static final String
        DESC_3BYTE_RGB_X11_BM   = "3 Byte RGB Pixmap with 1-bit transp";
    public static final String
        DESC_3BYTE_BGR_X11_BM   = "3 Byte BGR Pixmap with 1-bit transp";
    public static final String
        DESC_USHORT_555_RGB_X11 = "Ushort 555 RGB Pixmap";
    public static final String
        DESC_USHORT_565_RGB_X11 = "Ushort 565 RGB Pixmap";
    public static final String
        DESC_USHORT_555_RGB_X11_BM
                                = "Ushort 555 RGB Pixmap with 1-bit transp";
    public static final String
        DESC_USHORT_565_RGB_X11_BM
                                = "Ushort 565 RGB Pixmap with 1-bit transp";
    public static final String
        DESC_USHORT_INDEXED_X11 = "Ushort Indexed Pixmap";
    public static final String
        DESC_USHORT_INDEXED_X11_BM = "Ushort Indexed Pixmap with 1-bit transp";
    public static final SurfaceType IntBgrX11 =
        SurfaceType.IntBgr.deriveSubType(DESC_INT_BGR_X11);
    public static final SurfaceType IntRgbX11 =
        SurfaceType.IntRgb.deriveSubType(DESC_INT_RGB_X11);
    public static final SurfaceType FourByteAbgrPreX11 =
        SurfaceType.FourByteAbgrPre.deriveSubType(DESC_4BYTE_ABGR_PRE_X11);
    public static final SurfaceType IntArgbPreX11 =
        SurfaceType.IntArgbPre.deriveSubType(DESC_INT_ARGB_PRE_X11);
    public static final SurfaceType ThreeByteRgbX11 =
        SurfaceType.ThreeByteRgb.deriveSubType(DESC_3BYTE_RGB_X11);
    public static final SurfaceType ThreeByteBgrX11 =
        SurfaceType.ThreeByteBgr.deriveSubType(DESC_3BYTE_BGR_X11);
    public static final SurfaceType UShort555RgbX11 =
        SurfaceType.Ushort555Rgb.deriveSubType(DESC_USHORT_555_RGB_X11);
    public static final SurfaceType UShort565RgbX11 =
        SurfaceType.Ushort565Rgb.deriveSubType(DESC_USHORT_565_RGB_X11);
    public static final SurfaceType UShortIndexedX11 =
        SurfaceType.UshortIndexed.deriveSubType(DESC_USHORT_INDEXED_X11);
    public static final SurfaceType ByteIndexedOpaqueX11 =
        SurfaceType.ByteIndexedOpaque.deriveSubType(DESC_BYTE_IND_OPQ_X11);
    public static final SurfaceType ByteGrayX11 =
        SurfaceType.ByteGray.deriveSubType(DESC_BYTE_GRAY_X11);
    public static final SurfaceType Index8GrayX11 =
        SurfaceType.Index8Gray.deriveSubType(DESC_INDEX8_GRAY_X11);
    public static final SurfaceType IntBgrX11_BM =
        SurfaceType.Custom.deriveSubType(DESC_INT_BGR_X11_BM,
                                         PixelConverter.Xbgr.instance);
    public static final SurfaceType IntRgbX11_BM =
        SurfaceType.Custom.deriveSubType(DESC_INT_RGB_X11_BM,
                                         PixelConverter.Xrgb.instance);
    public static final SurfaceType ThreeByteRgbX11_BM =
        SurfaceType.Custom.deriveSubType(DESC_3BYTE_RGB_X11_BM,
                                         PixelConverter.Xbgr.instance);
    public static final SurfaceType ThreeByteBgrX11_BM =
        SurfaceType.Custom.deriveSubType(DESC_3BYTE_BGR_X11_BM,
                                         PixelConverter.Xrgb.instance);
    public static final SurfaceType UShort555RgbX11_BM =
        SurfaceType.Custom.deriveSubType(DESC_USHORT_555_RGB_X11_BM,
                                         PixelConverter.Ushort555Rgb.instance);
    public static final SurfaceType UShort565RgbX11_BM =
        SurfaceType.Custom.deriveSubType(DESC_USHORT_565_RGB_X11_BM,
                                         PixelConverter.Ushort565Rgb.instance);
    public static final SurfaceType UShortIndexedX11_BM =
        SurfaceType.Custom.deriveSubType(DESC_USHORT_INDEXED_X11_BM);
    public static final SurfaceType ByteIndexedX11_BM =
        SurfaceType.Custom.deriveSubType(DESC_BYTE_IND_X11_BM);
    public static final SurfaceType ByteGrayX11_BM =
        SurfaceType.Custom.deriveSubType(DESC_BYTE_GRAY_X11_BM);
    public static final SurfaceType Index8GrayX11_BM =
        SurfaceType.Custom.deriveSubType(DESC_INDEX8_GRAY_X11_BM);
    private static Boolean accelerationEnabled = null;
    public Raster getRaster(int x, int y, int w, int h) {
        throw new InternalError("not implemented yet");
    }
    protected X11Renderer x11pipe;
    protected PixelToShapeConverter x11txpipe;
    protected static TextPipe x11textpipe;
    protected static boolean dgaAvailable;
    static {
       if (!isX11SurfaceDataInitialized() &&
           !GraphicsEnvironment.isHeadless()) {
            String magPresent = (String) java.security.AccessController.doPrivileged
                (new sun.security.action.GetPropertyAction("javax.accessibility.screen_magnifier_present"));
            boolean tryDGA = magPresent == null || !"true".equals(magPresent);
            initIDs(XORComposite.class, tryDGA);
            String xtextpipe = (String) java.security.AccessController.doPrivileged
                (new sun.security.action.GetPropertyAction("sun.java2d.xtextpipe"));
            if (xtextpipe == null || "true".startsWith(xtextpipe)) {
                if ("true".equals(xtextpipe)) {
                    System.out.println("using X11 text renderer");
                }
                x11textpipe = new X11TextRenderer();
                if (GraphicsPrimitive.tracingEnabled()) {
                    x11textpipe = ((X11TextRenderer) x11textpipe).traceWrap();
                }
            } else {
                if ("false".equals(xtextpipe)) {
                    System.out.println("using DGA text renderer");
                }
                x11textpipe = solidTextRenderer;
            }
            dgaAvailable = isDgaAvailable();
            if (isAccelerationEnabled()) {
                X11PMBlitLoops.register();
                X11PMBlitBgLoops.register();
            }
       }
    }
    public static native boolean isDgaAvailable();
    private static native boolean isShmPMAvailable();
    public static boolean isAccelerationEnabled() {
        if (accelerationEnabled == null) {
            if (GraphicsEnvironment.isHeadless()) {
                accelerationEnabled = Boolean.FALSE;
            } else {
                String prop =
                    (String) java.security.AccessController.doPrivileged(
                        new sun.security.action.GetPropertyAction("sun.java2d.pmoffscreen"));
                if (prop != null) {
                    accelerationEnabled = Boolean.valueOf(prop);
                } else {
                    boolean isDisplayLocal = false;
                    GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                    if (ge instanceof SunGraphicsEnvironment) {
                        isDisplayLocal = ((SunGraphicsEnvironment) ge).isDisplayLocal();
                     }
                    accelerationEnabled =
                        !(isDgaAvailable() || (isDisplayLocal && !isShmPMAvailable()));
                }
            }
        }
        return accelerationEnabled.booleanValue();
    }
    @Override
    public SurfaceDataProxy makeProxyFor(SurfaceData srcData) {
        return X11SurfaceDataProxy.createProxy(srcData, graphicsConfig);
    }
    public void validatePipe(SunGraphics2D sg2d) {
        if (sg2d.antialiasHint != SunHints.INTVAL_ANTIALIAS_ON &&
            sg2d.paintState <= sg2d.PAINT_ALPHACOLOR &&
            (sg2d.compositeState <= sg2d.COMP_ISCOPY ||
             sg2d.compositeState == sg2d.COMP_XOR))
        {
            if (x11txpipe == null) {
                sg2d.drawpipe = lazypipe;
                sg2d.fillpipe = lazypipe;
                sg2d.shapepipe = lazypipe;
                sg2d.imagepipe = lazypipe;
                sg2d.textpipe = lazypipe;
                return;
            }
            if (sg2d.clipState == sg2d.CLIP_SHAPE) {
                super.validatePipe(sg2d);
            } else {
                switch (sg2d.textAntialiasHint) {
                case SunHints.INTVAL_TEXT_ANTIALIAS_DEFAULT:
                case SunHints.INTVAL_TEXT_ANTIALIAS_OFF:
                    if (sg2d.compositeState == sg2d.COMP_ISCOPY) {
                        sg2d.textpipe = x11textpipe;
                    } else {
                        sg2d.textpipe = solidTextRenderer;
                    }
                    break;
                case SunHints.INTVAL_TEXT_ANTIALIAS_ON:
                    sg2d.textpipe = aaTextRenderer;
                    break;
                default:
                    switch (sg2d.getFontInfo().aaHint) {
                    case SunHints.INTVAL_TEXT_ANTIALIAS_LCD_HRGB:
                    case SunHints.INTVAL_TEXT_ANTIALIAS_LCD_VRGB:
                        sg2d.textpipe = lcdTextRenderer;
                        break;
                    case SunHints.INTVAL_TEXT_ANTIALIAS_OFF:
                    if (sg2d.compositeState == sg2d.COMP_ISCOPY) {
                        sg2d.textpipe = x11textpipe;
                    } else {
                        sg2d.textpipe = solidTextRenderer;
                    }
                    break;
                    case SunHints.INTVAL_TEXT_ANTIALIAS_ON:
                        sg2d.textpipe = aaTextRenderer;
                        break;
                    default:
                        sg2d.textpipe = solidTextRenderer;
                    }
                }
            }
            if (sg2d.transformState >= sg2d.TRANSFORM_TRANSLATESCALE) {
                sg2d.drawpipe = x11txpipe;
                sg2d.fillpipe = x11txpipe;
            } else if (sg2d.strokeState != sg2d.STROKE_THIN){
                sg2d.drawpipe = x11txpipe;
                sg2d.fillpipe = x11pipe;
            } else {
                sg2d.drawpipe = x11pipe;
                sg2d.fillpipe = x11pipe;
            }
            sg2d.shapepipe = x11pipe;
            sg2d.imagepipe = imagepipe;
            if (sg2d.loops == null) {
                sg2d.loops = getRenderLoops(sg2d);
            }
        } else {
            super.validatePipe(sg2d);
        }
    }
    public RenderLoops getRenderLoops(SunGraphics2D sg2d) {
        if (sg2d.paintState <= sg2d.PAINT_ALPHACOLOR &&
            sg2d.compositeState <= sg2d.COMP_ISCOPY)
        {
            return solidloops;
        }
        return super.getRenderLoops(sg2d);
    }
    public GraphicsConfiguration getDeviceConfiguration() {
        return graphicsConfig;
    }
    public static X11WindowSurfaceData createData(X11ComponentPeer peer) {
       X11GraphicsConfig gc = getGC(peer);
       return new X11WindowSurfaceData(peer, gc, gc.getSurfaceType());
    }
    public static X11PixmapSurfaceData createData(X11GraphicsConfig gc,
                                                  int width, int height,
                                                  ColorModel cm, Image image,
                                                  long drawable,
                                                  int transparency)
    {
        return new X11PixmapSurfaceData(gc, width, height, image,
                                        getSurfaceType(gc, transparency, true),
                                        cm, drawable, transparency);
    }
    protected X11SurfaceData(X11ComponentPeer peer,
                             X11GraphicsConfig gc,
                             SurfaceType sType,
                             ColorModel cm) {
        super(sType, cm);
        this.peer = peer;
        this.graphicsConfig = gc;
        this.solidloops = graphicsConfig.getSolidLoops(sType);
        this.depth = cm.getPixelSize();
        initOps(peer, graphicsConfig, depth);
        if (isAccelerationEnabled()) {
            setBlitProxyKey(gc.getProxyKey());
        }
    }
    public static X11GraphicsConfig getGC(X11ComponentPeer peer) {
        if (peer != null) {
            return (X11GraphicsConfig) peer.getGraphicsConfiguration();
        } else {
            GraphicsEnvironment env =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = env.getDefaultScreenDevice();
            return (X11GraphicsConfig)gd.getDefaultConfiguration();
        }
    }
    public abstract boolean canSourceSendExposures(int x, int y, int w, int h);
    public boolean copyArea(SunGraphics2D sg2d,
                            int x, int y, int w, int h, int dx, int dy)
    {
        if (x11pipe == null) {
            if (!isDrawableValid()) {
                return true;
            }
            makePipes();
        }
        CompositeType comptype = sg2d.imageComp;
        if (sg2d.transformState < sg2d.TRANSFORM_TRANSLATESCALE &&
            (CompositeType.SrcOverNoEa.equals(comptype) ||
             CompositeType.SrcNoEa.equals(comptype)))
        {
            x += sg2d.transX;
            y += sg2d.transY;
            SunToolkit.awtLock();
            try {
                boolean needExposures = canSourceSendExposures(x, y, w, h);
                long xgc = getBlitGC(sg2d.getCompClip(), needExposures);
                x11pipe.devCopyArea(getNativeOps(), xgc,
                                    x, y,
                                    x + dx, y + dy,
                                    w, h);
            } finally {
                SunToolkit.awtUnlock();
            }
            return true;
        }
        return false;
    }
    public static SurfaceType getSurfaceType(X11GraphicsConfig gc,
                                             int transparency)
    {
        return getSurfaceType(gc, transparency, false);
    }
    public static SurfaceType getSurfaceType(X11GraphicsConfig gc,
                                             int transparency,
                                             boolean pixmapSurface)
    {
        boolean transparent = (transparency == Transparency.BITMASK);
        SurfaceType sType;
        ColorModel cm = gc.getColorModel();
        switch (cm.getPixelSize()) {
        case 24:
            if (gc.getBitsPerPixel() == 24) {
                if (cm instanceof DirectColorModel) {
                    sType = transparent ? X11SurfaceData.ThreeByteBgrX11_BM : X11SurfaceData.ThreeByteBgrX11;
                } else {
                    throw new sun.java2d.InvalidPipeException("Unsupported bit " +
                                                              "depth/cm combo: " +
                                                              cm.getPixelSize()  +
                                                              ", " + cm);
                }
                break;
            }
        case 32:
            if (cm instanceof DirectColorModel) {
                if (((SunToolkit)java.awt.Toolkit.getDefaultToolkit()
                     ).isTranslucencyCapable(gc) && !pixmapSurface)
                {
                    sType = X11SurfaceData.IntArgbPreX11;
                } else {
                    if (((DirectColorModel)cm).getRedMask() == 0xff0000) {
                        sType = transparent ? X11SurfaceData.IntRgbX11_BM :
                                              X11SurfaceData.IntRgbX11;
                    } else {
                        sType = transparent ? X11SurfaceData.IntBgrX11_BM :
                                              X11SurfaceData.IntBgrX11;
                    }
                }
            } else if (cm instanceof ComponentColorModel) {
                   sType = X11SurfaceData.FourByteAbgrPreX11;
            } else {
                throw new sun.java2d.InvalidPipeException("Unsupported bit " +
                                                          "depth/cm combo: " +
                                                          cm.getPixelSize()  +
                                                          ", " + cm);
            }
            break;
        case 15:
            sType = transparent ? X11SurfaceData.UShort555RgbX11_BM : X11SurfaceData.UShort555RgbX11;
            break;
        case 16:
            if ((cm instanceof DirectColorModel) &&
                (((DirectColorModel)cm).getGreenMask() == 0x3e0))
            {
                sType = transparent ? X11SurfaceData.UShort555RgbX11_BM : X11SurfaceData.UShort555RgbX11;
            } else {
                sType = transparent ? X11SurfaceData.UShort565RgbX11_BM : X11SurfaceData.UShort565RgbX11;
            }
            break;
        case  12:
            if (cm instanceof IndexColorModel) {
                sType = transparent ?
                    X11SurfaceData.UShortIndexedX11_BM :
                    X11SurfaceData.UShortIndexedX11;
            } else {
                throw new sun.java2d.InvalidPipeException("Unsupported bit " +
                                                          "depth: " +
                                                          cm.getPixelSize() +
                                                          " cm="+cm);
            }
            break;
        case 8:
            if (cm.getColorSpace().getType() == ColorSpace.TYPE_GRAY &&
                cm instanceof ComponentColorModel) {
                sType = transparent ? X11SurfaceData.ByteGrayX11_BM : X11SurfaceData.ByteGrayX11;
            } else if (cm instanceof IndexColorModel &&
                       isOpaqueGray((IndexColorModel)cm)) {
                sType = transparent ? X11SurfaceData.Index8GrayX11_BM : X11SurfaceData.Index8GrayX11;
            } else {
                sType = transparent ? X11SurfaceData.ByteIndexedX11_BM : X11SurfaceData.ByteIndexedOpaqueX11;
            }
            break;
        default:
            throw new sun.java2d.InvalidPipeException("Unsupported bit " +
                                                      "depth: " +
                                                      cm.getPixelSize());
        }
        return sType;
    }
    public void invalidate() {
        if (isValid()) {
            setInvalid();
            super.invalidate();
        }
    }
    private static native void XSetCopyMode(long xgc);
    private static native void XSetXorMode(long xgc);
    private static native void XSetForeground(long xgc, int pixel);
    private long xgc;
    private Region validatedClip;
    private XORComposite validatedXorComp;
    private int xorpixelmod;
    private int validatedPixel;
    private boolean validatedExposures = true;
    public final long getRenderGC(Region clip,
                                  int compState, Composite comp,
                                  int pixel)
    {
        return getGC(clip, compState, comp, pixel, validatedExposures);
    }
    public final long getBlitGC(Region clip, boolean needExposures) {
        return getGC(clip, SunGraphics2D.COMP_ISCOPY, null,
                     validatedPixel, needExposures);
    }
    final long getGC(Region clip,
                     int compState, Composite comp,
                     int pixel, boolean needExposures)
    {
        if (!isValid()) {
            throw new InvalidPipeException("bounds changed");
        }
        if (clip != validatedClip) {
            validatedClip = clip;
            if (clip != null) {
                XSetClip(xgc,
                         clip.getLoX(), clip.getLoY(),
                         clip.getHiX(), clip.getHiY(),
                         (clip.isRectangular() ? null : clip));
            } else {
                XResetClip(xgc);
            }
        }
        if (compState == SunGraphics2D.COMP_ISCOPY) {
            if (validatedXorComp != null) {
                validatedXorComp = null;
                xorpixelmod = 0;
                XSetCopyMode(xgc);
            }
        } else {
            if (validatedXorComp != comp) {
                validatedXorComp = (XORComposite)comp;
                xorpixelmod = validatedXorComp.getXorPixel();
                XSetXorMode(xgc);
            }
        }
        pixel ^= xorpixelmod;
        if (pixel != validatedPixel) {
            validatedPixel = pixel;
            XSetForeground(xgc, pixel);
        }
        if (validatedExposures != needExposures) {
            validatedExposures = needExposures;
            XSetGraphicsExposures(xgc, needExposures);
        }
        return xgc;
    }
    public synchronized void makePipes() {
        if (x11pipe == null) {
            SunToolkit.awtLock();
            try {
                xgc = XCreateGC(getNativeOps());
            } finally {
                SunToolkit.awtUnlock();
            }
            x11pipe = X11Renderer.getInstance();
            x11txpipe = new PixelToShapeConverter(x11pipe);
        }
    }
    public static class X11WindowSurfaceData extends X11SurfaceData {
        public X11WindowSurfaceData(X11ComponentPeer peer,
                                    X11GraphicsConfig gc,
                                    SurfaceType sType) {
            super(peer, gc, sType, peer.getColorModel());
            if (isDrawableValid()) {
                makePipes();
            }
        }
        public SurfaceData getReplacement() {
            return peer.getSurfaceData();
        }
        public Rectangle getBounds() {
            Rectangle r = peer.getBounds();
            r.x = r.y = 0;
            return r;
        }
        @Override
        public boolean canSourceSendExposures(int x, int y, int w, int h) {
            return true;
        }
        public Object getDestination() {
            return peer.getTarget();
        }
    }
    public static class X11PixmapSurfaceData extends X11SurfaceData {
        Image                   offscreenImage;
        int                     width;
        int                     height;
        int                     transparency;
        public X11PixmapSurfaceData(X11GraphicsConfig gc,
                                    int width, int height,
                                    Image image,
                                    SurfaceType sType, ColorModel cm,
                                    long drawable, int transparency)
        {
            super(null, gc, sType, cm);
            this.width = width;
            this.height = height;
            offscreenImage = image;
            this.transparency = transparency;
            initSurface(depth, width, height, drawable);
            makePipes();
        }
        public SurfaceData getReplacement() {
            return restoreContents(offscreenImage);
        }
        public int getTransparency() {
            return transparency;
        }
        public Rectangle getBounds() {
            return new Rectangle(width, height);
        }
        @Override
        public boolean canSourceSendExposures(int x, int y, int w, int h) {
            return (x < 0 || y < 0 || (x+w) > width || (y+h) > height);
        }
        public void flush() {
            invalidate();
            flushNativeSurface();
        }
        public Object getDestination() {
            return offscreenImage;
        }
    }
    private static LazyPipe lazypipe = new LazyPipe();
    public static class LazyPipe extends ValidatePipe {
        public boolean validate(SunGraphics2D sg2d) {
            X11SurfaceData xsd = (X11SurfaceData) sg2d.surfaceData;
            if (!xsd.isDrawableValid()) {
                return false;
            }
            xsd.makePipes();
            return super.validate(sg2d);
        }
    }
}
