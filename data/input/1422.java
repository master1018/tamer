public abstract class XRSurfaceData extends XSurfaceData {
    X11ComponentPeer peer;
    XRGraphicsConfig graphicsConfig;
    XRBackend renderQueue;
    private RenderLoops solidloops;
    protected int depth;
    private static native void initIDs();
    protected native void XRInitSurface(int depth, int width, int height,
                                        long drawable, int pictFormat);
    native void initXRPicture(long xsdo, int pictForm);
    native void freeXSDOPicture(long xsdo);
    public static final String DESC_BYTE_A8_X11 = "Byte A8 Pixmap";
    public static final String DESC_INT_RGB_X11 = "Integer RGB Pixmap";
    public static final String DESC_INT_ARGB_X11 = "Integer ARGB-Pre Pixmap";
    public static final SurfaceType
        ByteA8X11 = SurfaceType.ByteGray.deriveSubType(DESC_BYTE_A8_X11);
    public static final SurfaceType
        IntRgbX11 = SurfaceType.IntRgb.deriveSubType(DESC_INT_RGB_X11);
    public static final SurfaceType
        IntArgbPreX11 = SurfaceType.IntArgbPre.deriveSubType(DESC_INT_ARGB_X11);
    public Raster getRaster(int x, int y, int w, int h) {
        throw new InternalError("not implemented yet");
    }
    protected XRRenderer xrpipe;
    protected PixelToShapeConverter xrtxpipe;
    protected TextPipe xrtextpipe;
    protected XRDrawImage xrDrawImage;
    protected ShapeDrawPipe aaShapePipe;
    protected PixelToShapeConverter aaPixelToShapeConv;
    public static void initXRSurfaceData() {
        if (!isX11SurfaceDataInitialized()) {
            FontManagerNativeLibrary.load();
            initIDs();
            XRPMBlitLoops.register();
            XRMaskFill.register();
            XRMaskBlit.register();
            setX11SurfaceDataInitialized();
        }
    }
    protected boolean isXRDrawableValid() {
        try {
            SunToolkit.awtLock();
            return isDrawableValid();
        } finally {
            SunToolkit.awtUnlock();
        }
    }
    @Override
    public SurfaceDataProxy makeProxyFor(SurfaceData srcData) {
        return XRSurfaceDataProxy.createProxy(srcData, graphicsConfig);
    }
    public void validatePipe(SunGraphics2D sg2d) {
        TextPipe textpipe;
        boolean validated = false;
        if (sg2d.compositeState < SunGraphics2D.COMP_XOR &&
            (sg2d.paintState < SunGraphics2D.PAINT_TEXTURE ||
             sg2d.composite == null ||
             !(sg2d.composite instanceof AlphaComposite) ||
             ((AlphaComposite) sg2d.composite).getAlpha() == 1.0f))
        {
            textpipe = xrtextpipe;
        } else {
            super.validatePipe(sg2d);
            textpipe = sg2d.textpipe;
            validated = true;
        }
        PixelToShapeConverter txPipe = null;
        XRRenderer nonTxPipe = null;
        if (sg2d.antialiasHint != SunHints.INTVAL_ANTIALIAS_ON) {
            if (sg2d.paintState <= SunGraphics2D.PAINT_ALPHACOLOR) {
                if (sg2d.compositeState <= SunGraphics2D.COMP_XOR) {
                    txPipe = xrtxpipe;
                    nonTxPipe = xrpipe;
                }
            } else if (sg2d.compositeState <= SunGraphics2D.COMP_ALPHA) {
                if (XRPaints.isValid(sg2d)) {
                    txPipe = xrtxpipe;
                    nonTxPipe = xrpipe;
                }
            }
        }
        if (sg2d.antialiasHint == SunHints.INTVAL_ANTIALIAS_ON &&
            JulesPathBuf.isCairoAvailable())
        {
            sg2d.shapepipe = aaShapePipe;
            sg2d.drawpipe = aaPixelToShapeConv;
            sg2d.fillpipe = aaPixelToShapeConv;
        } else {
            if (txPipe != null) {
                if (sg2d.transformState >= SunGraphics2D.TRANSFORM_TRANSLATESCALE) {
                    sg2d.drawpipe = txPipe;
                    sg2d.fillpipe = txPipe;
                } else if (sg2d.strokeState != SunGraphics2D.STROKE_THIN) {
                    sg2d.drawpipe = txPipe;
                    sg2d.fillpipe = nonTxPipe;
                } else {
                    sg2d.drawpipe = nonTxPipe;
                    sg2d.fillpipe = nonTxPipe;
                }
                sg2d.shapepipe = nonTxPipe;
            } else {
                if (!validated) {
                    super.validatePipe(sg2d);
                }
            }
        }
        sg2d.textpipe = textpipe;
        sg2d.imagepipe = xrDrawImage;
    }
    protected MaskFill getMaskFill(SunGraphics2D sg2d) {
        if (sg2d.paintState > SunGraphics2D.PAINT_ALPHACOLOR &&
            !XRPaints.isValid(sg2d))
        {
            return null;
        }
        return super.getMaskFill(sg2d);
    }
    public RenderLoops getRenderLoops(SunGraphics2D sg2d) {
        if (sg2d.paintState <= SunGraphics2D.PAINT_ALPHACOLOR &&
            sg2d.compositeState <= SunGraphics2D.COMP_ALPHA)
        {
            return solidloops;
        }
        return super.getRenderLoops(sg2d);
    }
    public GraphicsConfiguration getDeviceConfiguration() {
        return graphicsConfig;
    }
    public static XRWindowSurfaceData createData(X11ComponentPeer peer) {
        XRGraphicsConfig gc = getGC(peer);
        return new XRWindowSurfaceData(peer, gc, gc.getSurfaceType());
    }
    public static XRPixmapSurfaceData createData(XRGraphicsConfig gc,
                                                 int width, int height,
                                                 ColorModel cm, Image image,
                                                 long drawable,
                                                 int transparency) {
        int depth = transparency > Transparency.OPAQUE ? 32 : 24;
        if (depth == 24) {
            cm = new DirectColorModel(depth,
                                      0x00FF0000, 0x0000FF00, 0x000000FF);
        } else {
            cm = new DirectColorModel(depth, 0x00FF0000, 0x0000FF00,
                                      0x000000FF, 0xFF000000);
        }
        return new XRPixmapSurfaceData
            (gc, width, height, image, getSurfaceType(gc, transparency),
             cm, drawable, transparency,
             XRUtils.getPictureFormatForTransparency(transparency), depth);
    }
    protected XRSurfaceData(X11ComponentPeer peer, XRGraphicsConfig gc,
        SurfaceType sType, ColorModel cm, int depth, int transparency)
    {
        super(sType, cm);
        this.peer = peer;
        this.graphicsConfig = gc;
        this.solidloops = graphicsConfig.getSolidLoops(sType);
        this.depth = depth;
        initOps(peer, graphicsConfig, depth);
        setBlitProxyKey(gc.getProxyKey());
    }
    protected XRSurfaceData(XRBackend renderQueue) {
        super(XRSurfaceData.IntRgbX11,
              new DirectColorModel(24, 0x00FF0000, 0x0000FF00, 0x000000FF));
        this.renderQueue = renderQueue;
    }
    public void initXRender(int pictureFormat) {
        try {
            SunToolkit.awtLock();
            initXRPicture(getNativeOps(), pictureFormat);
            renderQueue = XRCompositeManager.getInstance(this).getBackend();
            maskBuffer = XRCompositeManager.getInstance(this);
        } catch (Throwable ex) {
            ex.printStackTrace();
        } finally {
            SunToolkit.awtUnlock();
        }
    }
    public static XRGraphicsConfig getGC(X11ComponentPeer peer) {
        if (peer != null) {
            return (XRGraphicsConfig) peer.getGraphicsConfiguration();
        } else {
            GraphicsEnvironment env =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = env.getDefaultScreenDevice();
            return (XRGraphicsConfig) gd.getDefaultConfiguration();
        }
    }
    public abstract boolean canSourceSendExposures(int x, int y, int w, int h);
    public void validateCopyAreaGC(Region gcClip, boolean needExposures) {
        if (validatedGCClip != gcClip) {
            if (gcClip != null)
                renderQueue.setGCClipRectangles(xgc, gcClip);
            validatedGCClip = gcClip;
        }
        if (validatedExposures != needExposures) {
            validatedExposures = needExposures;
            renderQueue.setGCExposures(xgc, needExposures);
        }
        if (validatedXorComp != null) {
            renderQueue.setGCMode(xgc, true);
            renderQueue.setGCForeground(xgc, validatedGCForegroundPixel);
            validatedXorComp = null;
        }
    }
    public boolean copyArea(SunGraphics2D sg2d, int x, int y, int w, int h,
                            int dx, int dy) {
        if (xrpipe == null) {
            if (!isXRDrawableValid()) {
                return true;
            }
            makePipes();
        }
        CompositeType comptype = sg2d.imageComp;
        if (sg2d.transformState < SunGraphics2D.TRANSFORM_TRANSLATESCALE &&
            (CompositeType.SrcOverNoEa.equals(comptype) ||
             CompositeType.SrcNoEa.equals(comptype)))
        {
            x += sg2d.transX;
            y += sg2d.transY;
            try {
                SunToolkit.awtLock();
                boolean needExposures = canSourceSendExposures(x, y, w, h);
                validateCopyAreaGC(sg2d.getCompClip(), needExposures);
                renderQueue.copyArea(xid, xid, xgc, x, y, w, h, x + dx, y + dy);
            } finally {
                SunToolkit.awtUnlock();
            }
            return true;
        }
        return false;
    }
    public static SurfaceType getSurfaceType(XRGraphicsConfig gc,
                                             int transparency) {
        SurfaceType sType = null;
        switch (transparency) {
        case Transparency.OPAQUE:
            sType = XRSurfaceData.IntRgbX11;
            break;
        case Transparency.BITMASK:
        case Transparency.TRANSLUCENT:
            sType = XRSurfaceData.IntArgbPreX11;
            break;
        }
        return sType;
    }
    public void invalidate() {
        if (isValid()) {
            setInvalid();
            super.invalidate();
        }
    }
    private long xgc; 
    private int validatedGCForegroundPixel = 0;
    private XORComposite validatedXorComp;
    private int xid;
    public int picture;
    public XRCompositeManager maskBuffer;
    private Region validatedClip;
    private Region validatedGCClip;
    private boolean validatedExposures = true;
    boolean transformInUse = false;
    AffineTransform validatedSourceTransform = new AffineTransform();
    int validatedRepeat = XRUtils.RepeatNone;
    int validatedFilter = XRUtils.FAST;
    void validateAsSource(AffineTransform sxForm, int repeat, int filter) {
        if (validatedClip != null) {
            validatedClip = null;
            renderQueue.setClipRectangles(picture, null);
        }
        if (validatedRepeat != repeat && repeat != -1) {
            validatedRepeat = repeat;
            renderQueue.setPictureRepeat(picture, repeat);
        }
        if (sxForm == null) {
            if (transformInUse) {
                validatedSourceTransform.setToIdentity();
                renderQueue.setPictureTransform(picture,
                                                validatedSourceTransform);
                transformInUse = false;
            }
        } else if (!transformInUse ||
                   (transformInUse && !sxForm.equals(validatedSourceTransform))) {
            validatedSourceTransform.setTransform(sxForm.getScaleX(),
                                                  sxForm.getShearY(),
                                                  sxForm.getShearX(),
                                                  sxForm.getScaleY(),
                                                  sxForm.getTranslateX(),
                                                  sxForm.getTranslateY());
            renderQueue.setPictureTransform(picture, validatedSourceTransform);
            transformInUse = true;
        }
        if (filter != validatedFilter && filter != -1) {
            renderQueue.setFilter(picture, filter);
            validatedFilter = filter;
        }
    }
    public void validateAsDestination(SunGraphics2D sg2d, Region clip) {
        if (!isValid()) {
            throw new InvalidPipeException("bounds changed");
        }
        boolean updateGCClip = false;
        if (clip != validatedClip) {
            renderQueue.setClipRectangles(picture, clip);
            validatedClip = clip;
            updateGCClip = true;
        }
        if (sg2d != null && sg2d.compositeState == SunGraphics2D.COMP_XOR) {
            if (validatedXorComp != sg2d.getComposite()) {
                validatedXorComp = (XORComposite) sg2d.getComposite();
                int xorpixelmod = validatedXorComp.getXorPixel();
                renderQueue.setGCMode(xgc, false);
                int pixel = sg2d.pixel;
                if (validatedGCForegroundPixel != pixel) {
                    renderQueue.setGCForeground(xgc, pixel ^ xorpixelmod);
                    validatedGCForegroundPixel = pixel;
                }
            }
            if (updateGCClip) {
                renderQueue.setGCClipRectangles(xgc, clip);
            }
        }
    }
    public synchronized void makePipes() { 
        if (xrpipe == null) {
            try {
                SunToolkit.awtLock();
                xgc = XCreateGC(getNativeOps());
                xrpipe = new XRRenderer(maskBuffer.getMaskBuffer());
                xrtxpipe = new PixelToShapeConverter(xrpipe);
                xrtextpipe = maskBuffer.getTextRenderer();
                xrDrawImage = new XRDrawImage();
                if (JulesPathBuf.isCairoAvailable()) {
                    aaShapePipe =
                       new JulesShapePipe(XRCompositeManager.getInstance(this));
                    aaPixelToShapeConv = new PixelToShapeConverter(aaShapePipe);
                }
            } finally {
                SunToolkit.awtUnlock();
            }
        }
    }
    public static class XRWindowSurfaceData extends XRSurfaceData {
        public XRWindowSurfaceData(X11ComponentPeer peer,
                                   XRGraphicsConfig gc, SurfaceType sType) {
            super(peer, gc, sType, peer.getColorModel(),
                  peer.getColorModel().getPixelSize(), Transparency.OPAQUE);
            if (isXRDrawableValid()) {
                initXRender(XRUtils.
                    getPictureFormatForTransparency(Transparency.OPAQUE));
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
       public void invalidate() {
           try {
               SunToolkit.awtLock();
               freeXSDOPicture(getNativeOps());
           }finally {
               SunToolkit.awtUnlock();
           }
           super.invalidate();
       }
    }
    public static class XRInternalSurfaceData extends XRSurfaceData {
        public XRInternalSurfaceData(XRBackend renderQueue, int pictXid,
                                     AffineTransform transform) {
          super(renderQueue);
          this.picture = pictXid;
          this.validatedSourceTransform = transform;
          if (validatedSourceTransform != null) {
              transformInUse = true;
          }
        }
        public boolean canSourceSendExposures(int x, int y, int w, int h) {
            return false;
        }
        public Rectangle getBounds() {
            return null;
        }
        public Object getDestination() {
            return null;
        }
        public SurfaceData getReplacement() {
            return null;
        }
    }
    public static class XRPixmapSurfaceData extends XRSurfaceData {
        Image offscreenImage;
        int width;
        int height;
        int transparency;
        public XRPixmapSurfaceData(XRGraphicsConfig gc, int width, int height,
                                   Image image, SurfaceType sType,
                                   ColorModel cm, long drawable,
                                   int transparency, int pictFormat,
                                   int depth) {
            super(null, gc, sType, cm, depth, transparency);
            this.width = width;
            this.height = height;
            offscreenImage = image;
            this.transparency = transparency;
            initSurface(depth, width, height, drawable, pictFormat);
            initXRender(pictFormat);
            makePipes();
        }
        public void initSurface(int depth, int width, int height,
                                long drawable, int pictFormat) {
            try {
                SunToolkit.awtLock();
                XRInitSurface(depth, width, height, drawable, pictFormat);
            } finally {
                SunToolkit.awtUnlock();
            }
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
            return (x < 0 || y < 0 || (x + w) > width || (y + h) > height);
        }
        public void flush() {
            invalidate();
            flushNativeSurface();
        }
        public Object getDestination() {
            return offscreenImage;
        }
    }
    public long getGC() {
        return xgc;
    }
    public static class LazyPipe extends ValidatePipe {
        public boolean validate(SunGraphics2D sg2d) {
            XRSurfaceData xsd = (XRSurfaceData) sg2d.surfaceData;
            if (!xsd.isXRDrawableValid()) {
                return false;
            }
            xsd.makePipes();
            return super.validate(sg2d);
        }
    }
    public int getPicture() {
        return picture;
    }
    public int getXid() {
        return xid;
    }
    public XRGraphicsConfig getGraphicsConfig() {
        return graphicsConfig;
    }
}
