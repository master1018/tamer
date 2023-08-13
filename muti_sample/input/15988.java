public class D3DSurfaceData extends SurfaceData implements AccelSurface {
    public static final int D3D_DEVICE_RESOURCE= 100;
    public static final int ST_INT_ARGB        = 0;
    public static final int ST_INT_ARGB_PRE    = 1;
    public static final int ST_INT_ARGB_BM     = 2;
    public static final int ST_INT_RGB         = 3;
    public static final int ST_INT_BGR         = 4;
    public static final int ST_USHORT_565_RGB  = 5;
    public static final int ST_USHORT_555_RGB  = 6;
    public static final int ST_BYTE_INDEXED    = 7;
    public static final int ST_BYTE_INDEXED_BM = 8;
    public static final int ST_3BYTE_BGR       = 9;
    public static final int SWAP_DISCARD       = 1;
    public static final int SWAP_FLIP          = 2;
    public static final int SWAP_COPY          = 3;
    private static final String DESC_D3D_SURFACE = "D3D Surface";
    private static final String DESC_D3D_SURFACE_RTT =
        "D3D Surface (render-to-texture)";
    private static final String DESC_D3D_TEXTURE = "D3D Texture";
    static final SurfaceType D3DSurface =
        SurfaceType.Any.deriveSubType(DESC_D3D_SURFACE,
                                      PixelConverter.ArgbPre.instance);
    static final SurfaceType D3DSurfaceRTT =
        D3DSurface.deriveSubType(DESC_D3D_SURFACE_RTT);
    static final SurfaceType D3DTexture =
        SurfaceType.Any.deriveSubType(DESC_D3D_TEXTURE);
    private int type;
    private int width, height;
    private int nativeWidth, nativeHeight;
    protected WComponentPeer peer;
    private Image offscreenImage;
    protected D3DGraphicsDevice graphicsDevice;
    private int swapEffect;
    private VSyncType syncType;
    private int backBuffersNum;
    private WritableRasterNative wrn;
    protected static D3DRenderer d3dRenderPipe;
    protected static PixelToParallelogramConverter d3dTxRenderPipe;
    protected static ParallelogramPipe d3dAAPgramPipe;
    protected static D3DTextRenderer d3dTextPipe;
    protected static D3DDrawImage d3dImagePipe;
    private native boolean initTexture(long pData, boolean isRTT,
                                       boolean isOpaque);
    private native boolean initFlipBackbuffer(long pData, long pPeerData,
                                              int numbuffers,
                                              int swapEffect, int syncType);
    private native boolean initRTSurface(long pData, boolean isOpaque);
    private native void initOps(int screen, int width, int height);
    static {
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        d3dImagePipe = new D3DDrawImage();
        d3dTextPipe = new D3DTextRenderer(rq);
        d3dRenderPipe = new D3DRenderer(rq);
        if (GraphicsPrimitive.tracingEnabled()) {
            d3dTextPipe = d3dTextPipe.traceWrap();
            d3dRenderPipe = d3dRenderPipe.traceWrap();
        }
        d3dAAPgramPipe = d3dRenderPipe.getAAParallelogramPipe();
        d3dTxRenderPipe =
            new PixelToParallelogramConverter(d3dRenderPipe, d3dRenderPipe,
                                              1.0, 0.25, true);
        D3DBlitLoops.register();
        D3DMaskFill.register();
        D3DMaskBlit.register();
    }
    protected D3DSurfaceData(WComponentPeer peer, D3DGraphicsConfig gc,
                             int width, int height, Image image,
                             ColorModel cm, int numBackBuffers,
                             int swapEffect, VSyncType vSyncType,
                             int type)
    {
        super(getCustomSurfaceType(type), cm);
        this.graphicsDevice = gc.getD3DDevice();
        this.peer = peer;
        this.type = type;
        this.width = width;
        this.height = height;
        this.offscreenImage = image;
        this.backBuffersNum = numBackBuffers;
        this.swapEffect = swapEffect;
        this.syncType = vSyncType;
        initOps(graphicsDevice.getScreen(), width, height);
        if (type == WINDOW) {
            setSurfaceLost(true);
        } else {
            initSurface();
        }
        setBlitProxyKey(gc.getProxyKey());
    }
    @Override
    public SurfaceDataProxy makeProxyFor(SurfaceData srcData) {
        return D3DSurfaceDataProxy.
            createProxy(srcData,
                        (D3DGraphicsConfig)graphicsDevice.getDefaultConfiguration());
    }
    public static D3DSurfaceData createData(WComponentPeer peer, Image image) {
        D3DGraphicsConfig gc = getGC(peer);
        if (gc == null || !peer.isAccelCapable()) {
            return null;
        }
        BufferCapabilities caps = peer.getBackBufferCaps();
        VSyncType vSyncType = VSYNC_DEFAULT;
        if (caps instanceof ExtendedBufferCapabilities) {
            vSyncType = ((ExtendedBufferCapabilities)caps).getVSync();
        }
        Rectangle r = peer.getBounds();
        BufferCapabilities.FlipContents flip = caps.getFlipContents();
        int swapEffect;
        if (flip == FlipContents.COPIED) {
            swapEffect = SWAP_COPY;
        } else if (flip == FlipContents.PRIOR) {
            swapEffect = SWAP_FLIP;
        } else { 
            swapEffect = SWAP_DISCARD;
        }
        return new D3DSurfaceData(peer, gc, r.width, r.height,
                                  image, peer.getColorModel(),
                                  peer.getBackBuffersNum(),
                                  swapEffect, vSyncType, FLIP_BACKBUFFER);
    }
    public static D3DSurfaceData createData(WComponentPeer peer) {
        D3DGraphicsConfig gc = getGC(peer);
        if (gc == null || !peer.isAccelCapable()) {
            return null;
        }
        return new D3DWindowSurfaceData(peer, gc);
    }
    public static D3DSurfaceData createData(D3DGraphicsConfig gc,
                                            int width, int height,
                                            ColorModel cm,
                                            Image image, int type)
    {
        if (type == RT_TEXTURE) {
            boolean isOpaque = cm.getTransparency() == Transparency.OPAQUE;
            int cap = isOpaque ? CAPS_RT_TEXTURE_OPAQUE : CAPS_RT_TEXTURE_ALPHA;
            if (!gc.getD3DDevice().isCapPresent(cap)) {
                type = RT_PLAIN;
            }
        }
        D3DSurfaceData ret = null;
        try {
            ret = new D3DSurfaceData(null, gc, width, height,
                                     image, cm, 0, SWAP_DISCARD, VSYNC_DEFAULT,
                                     type);
        } catch (InvalidPipeException ipe) {
            if (type == RT_TEXTURE) {
                if (((SunVolatileImage)image).getForcedAccelSurfaceType() !=
                    RT_TEXTURE)
                {
                    type = RT_PLAIN;
                    ret = new D3DSurfaceData(null, gc, width, height,
                                             image, cm, 0, SWAP_DISCARD,
                                             VSYNC_DEFAULT, type);
                }
            }
        }
        return ret;
    }
    private static SurfaceType getCustomSurfaceType(int d3dType) {
        switch (d3dType) {
        case TEXTURE:
            return D3DTexture;
        case RT_TEXTURE:
            return D3DSurfaceRTT;
        default:
            return D3DSurface;
        }
    }
    private boolean initSurfaceNow() {
        boolean isOpaque = (getTransparency() == Transparency.OPAQUE);
        switch (type) {
            case RT_PLAIN:
                return initRTSurface(getNativeOps(), isOpaque);
            case TEXTURE:
                return initTexture(getNativeOps(), false, isOpaque);
            case RT_TEXTURE:
                return initTexture(getNativeOps(), true,  isOpaque);
            case WINDOW:
            case FLIP_BACKBUFFER:
                return initFlipBackbuffer(getNativeOps(), peer.getData(),
                                          backBuffersNum, swapEffect,
                                          syncType.id());
            default:
                return false;
        }
    }
    protected void initSurface() {
        synchronized (this) {
            wrn = null;
        }
        class Status {
            boolean success = false;
        };
        final Status status = new Status();
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        rq.lock();
        try {
            rq.flushAndInvokeNow(new Runnable() {
                public void run() {
                    status.success = initSurfaceNow();
                }
            });
            if (!status.success) {
                throw new InvalidPipeException("Error creating D3DSurface");
            }
        } finally {
            rq.unlock();
        }
    }
    public final D3DContext getContext() {
        return graphicsDevice.getContext();
    }
    public final int getType() {
        return type;
    }
    private static native int  dbGetPixelNative(long pData, int x, int y);
    private static native void dbSetPixelNative(long pData, int x, int y,
                                                int pixel);
    static class D3DDataBufferNative extends DataBufferNative {
        int pixel;
        protected D3DDataBufferNative(SurfaceData sData,
                                      int type, int w, int h)
        {
            super(sData, type, w, h);
        }
        protected int getElem(final int x, final int y,
                              final SurfaceData sData)
        {
            if (sData.isSurfaceLost()) {
                return 0;
            }
            int retPixel;
            D3DRenderQueue rq = D3DRenderQueue.getInstance();
            rq.lock();
            try {
                rq.flushAndInvokeNow(new Runnable() {
                    public void run() {
                        pixel = dbGetPixelNative(sData.getNativeOps(), x, y);
                    }
                });
            } finally {
                retPixel = pixel;
                rq.unlock();
            }
            return retPixel;
        }
        protected void setElem(final int x, final int y, final int pixel,
                               final SurfaceData sData)
        {
            if (sData.isSurfaceLost()) {
                  return;
            }
            D3DRenderQueue rq = D3DRenderQueue.getInstance();
            rq.lock();
            try {
                rq.flushAndInvokeNow(new Runnable() {
                    public void run() {
                        dbSetPixelNative(sData.getNativeOps(), x, y, pixel);
                    }
                });
                sData.markDirty();
            } finally {
                rq.unlock();
            }
        }
    }
    public synchronized Raster getRaster(int x, int y, int w, int h) {
        if (wrn == null) {
            DirectColorModel dcm = (DirectColorModel)getColorModel();
            SampleModel smHw;
            int dataType = 0;
            int scanStride = width;
            if (dcm.getPixelSize() == 24 || dcm.getPixelSize() == 32) {
                dataType = DataBuffer.TYPE_INT;
            } else {
                dataType = DataBuffer.TYPE_USHORT;
            }
            smHw = new SinglePixelPackedSampleModel(dataType, width, height,
                                                    scanStride, dcm.getMasks());
            DataBuffer dbn = new D3DDataBufferNative(this, dataType,
                                                     width, height);
            wrn = WritableRasterNative.createNativeRaster(smHw, dbn);
        }
        return wrn;
    }
    public boolean canRenderLCDText(SunGraphics2D sg2d) {
        return
            graphicsDevice.isCapPresent(CAPS_LCD_SHADER) &&
            sg2d.compositeState <= SunGraphics2D.COMP_ISCOPY &&
            sg2d.paintState <= SunGraphics2D.PAINT_OPAQUECOLOR   &&
            sg2d.surfaceData.getTransparency() == Transparency.OPAQUE;
    }
    void disableAccelerationForSurface() {
        if (offscreenImage != null) {
            SurfaceManager sm = SurfaceManager.getManager(offscreenImage);
            if (sm instanceof D3DVolatileSurfaceManager) {
                setSurfaceLost(true);
                ((D3DVolatileSurfaceManager)sm).setAccelerationEnabled(false);
            }
        }
    }
    public void validatePipe(SunGraphics2D sg2d) {
        TextPipe textpipe;
        boolean validated = false;
        if (sg2d.compositeState >= sg2d.COMP_XOR) {
            super.validatePipe(sg2d);
            sg2d.imagepipe = d3dImagePipe;
            disableAccelerationForSurface();
            return;
        }
        if (
            (sg2d.compositeState <= sg2d.COMP_ISCOPY &&
             sg2d.paintState <= sg2d.PAINT_ALPHACOLOR)        ||
            (sg2d.compositeState == sg2d.COMP_ALPHA    &&
             sg2d.paintState <= sg2d.PAINT_ALPHACOLOR &&
             (((AlphaComposite)sg2d.composite).getRule() ==
              AlphaComposite.SRC_OVER))                       ||
            (sg2d.compositeState == sg2d.COMP_XOR &&
             sg2d.paintState <= sg2d.PAINT_ALPHACOLOR))
        {
            textpipe = d3dTextPipe;
        } else {
            super.validatePipe(sg2d);
            textpipe = sg2d.textpipe;
            validated = true;
        }
        PixelToParallelogramConverter txPipe = null;
        D3DRenderer nonTxPipe = null;
        if (sg2d.antialiasHint != SunHints.INTVAL_ANTIALIAS_ON) {
            if (sg2d.paintState <= sg2d.PAINT_ALPHACOLOR) {
                if (sg2d.compositeState <= sg2d.COMP_XOR) {
                    txPipe = d3dTxRenderPipe;
                    nonTxPipe = d3dRenderPipe;
                }
            } else if (sg2d.compositeState <= sg2d.COMP_ALPHA) {
                if (D3DPaints.isValid(sg2d)) {
                    txPipe = d3dTxRenderPipe;
                    nonTxPipe = d3dRenderPipe;
                }
            }
        } else {
            if (sg2d.paintState <= sg2d.PAINT_ALPHACOLOR) {
                if (graphicsDevice.isCapPresent(CAPS_AA_SHADER) &&
                    (sg2d.imageComp == CompositeType.SrcOverNoEa ||
                     sg2d.imageComp == CompositeType.SrcOver))
                {
                    if (!validated) {
                        super.validatePipe(sg2d);
                        validated = true;
                    }
                    PixelToParallelogramConverter aaConverter =
                        new PixelToParallelogramConverter(sg2d.shapepipe,
                                                          d3dAAPgramPipe,
                                                          1.0/8.0, 0.499,
                                                          false);
                    sg2d.drawpipe = aaConverter;
                    sg2d.fillpipe = aaConverter;
                    sg2d.shapepipe = aaConverter;
                } else if (sg2d.compositeState == sg2d.COMP_XOR) {
                    txPipe = d3dTxRenderPipe;
                    nonTxPipe = d3dRenderPipe;
                }
            }
        }
        if (txPipe != null) {
            if (sg2d.transformState >= sg2d.TRANSFORM_TRANSLATESCALE) {
                sg2d.drawpipe = txPipe;
                sg2d.fillpipe = txPipe;
            } else if (sg2d.strokeState != sg2d.STROKE_THIN) {
                sg2d.drawpipe = txPipe;
                sg2d.fillpipe = nonTxPipe;
            } else {
                sg2d.drawpipe = nonTxPipe;
                sg2d.fillpipe = nonTxPipe;
            }
            sg2d.shapepipe = txPipe;
        } else {
            if (!validated) {
                super.validatePipe(sg2d);
            }
        }
        sg2d.textpipe = textpipe;
        sg2d.imagepipe = d3dImagePipe;
    }
    @Override
    protected MaskFill getMaskFill(SunGraphics2D sg2d) {
        if (sg2d.paintState > sg2d.PAINT_ALPHACOLOR) {
            if (!D3DPaints.isValid(sg2d) ||
                !graphicsDevice.isCapPresent(CAPS_MULTITEXTURE))
            {
                return null;
            }
        }
        return super.getMaskFill(sg2d);
    }
    @Override
    public boolean copyArea(SunGraphics2D sg2d,
                            int x, int y, int w, int h, int dx, int dy)
    {
        if (sg2d.transformState < sg2d.TRANSFORM_TRANSLATESCALE &&
            sg2d.compositeState < sg2d.COMP_XOR)
        {
            x += sg2d.transX;
            y += sg2d.transY;
            d3dRenderPipe.copyArea(sg2d, x, y, w, h, dx, dy);
            return true;
        }
        return false;
    }
    @Override
    public void flush() {
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        rq.lock();
        try {
            RenderBuffer buf = rq.getBuffer();
            rq.ensureCapacityAndAlignment(12, 4);
            buf.putInt(FLUSH_SURFACE);
            buf.putLong(getNativeOps());
            rq.flushNow();
        } finally {
            rq.unlock();
        }
    }
    static void dispose(long pData) {
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        rq.lock();
        try {
            RenderBuffer buf = rq.getBuffer();
            rq.ensureCapacityAndAlignment(12, 4);
            buf.putInt(DISPOSE_SURFACE);
            buf.putLong(pData);
            rq.flushNow();
        } finally {
            rq.unlock();
        }
    }
    static void swapBuffers(D3DSurfaceData sd,
                            final int x1, final int y1,
                            final int x2, final int y2)
    {
        long pData = sd.getNativeOps();
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        if (rq.isRenderQueueThread()) {
            if (!rq.tryLock()) {
                final Component target = (Component)sd.getPeer().getTarget();
                SunToolkit.executeOnEventHandlerThread(target, new Runnable() {
                    public void run() {
                        target.repaint(x1, y1, x2, y2);
                    }
                });
                return;
            }
        } else {
            rq.lock();
        }
        try {
            RenderBuffer buf = rq.getBuffer();
            rq.ensureCapacityAndAlignment(28, 4);
            buf.putInt(SWAP_BUFFERS);
            buf.putLong(pData);
            buf.putInt(x1);
            buf.putInt(y1);
            buf.putInt(x2);
            buf.putInt(y2);
            rq.flushNow();
        } finally {
            rq.unlock();
        }
    }
    public Object getDestination() {
        return offscreenImage;
    }
    public Rectangle getBounds() {
        if (type == FLIP_BACKBUFFER || type == WINDOW) {
            Rectangle r = peer.getBounds();
            r.x = r.y = 0;
            return r;
        } else {
            return new Rectangle(width, height);
        }
    }
    public Rectangle getNativeBounds() {
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        rq.lock();
        try {
            return new Rectangle(nativeWidth, nativeHeight);
        } finally {
            rq.unlock();
        }
    }
    public GraphicsConfiguration getDeviceConfiguration() {
        return graphicsDevice.getDefaultConfiguration();
    }
    public SurfaceData getReplacement() {
        return restoreContents(offscreenImage);
    }
    private static D3DGraphicsConfig getGC(WComponentPeer peer) {
        GraphicsConfiguration gc;
        if (peer != null) {
            gc =  peer.getGraphicsConfiguration();
        } else {
            GraphicsEnvironment env =
                    GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = env.getDefaultScreenDevice();
            gc = gd.getDefaultConfiguration();
        }
        return (gc instanceof D3DGraphicsConfig) ? (D3DGraphicsConfig)gc : null;
    }
    void restoreSurface() {
        initSurface();
    }
    WComponentPeer getPeer() {
        return peer;
    }
    @Override
    public void setSurfaceLost(boolean lost) {
        super.setSurfaceLost(lost);
        if (lost && offscreenImage != null) {
            SurfaceManager sm = SurfaceManager.getManager(offscreenImage);
            sm.acceleratedSurfaceLost();
        }
    }
    private static native long getNativeResourceNative(long sdops, int resType);
    public long getNativeResource(int resType) {
        return getNativeResourceNative(getNativeOps(), resType);
    }
    public static class D3DWindowSurfaceData extends D3DSurfaceData {
        StateTracker dirtyTracker;
        public D3DWindowSurfaceData(WComponentPeer peer,
                                    D3DGraphicsConfig gc)
        {
            super(peer, gc,
                  peer.getBounds().width, peer.getBounds().height,
                  null, peer.getColorModel(), 1, SWAP_COPY, VSYNC_DEFAULT,
                  WINDOW);
            dirtyTracker = getStateTracker();
        }
        @Override
        public SurfaceData getReplacement() {
            ScreenUpdateManager mgr = ScreenUpdateManager.getInstance();
            return mgr.getReplacementScreenSurface(peer, this);
        }
        @Override
        public Object getDestination() {
            return peer.getTarget();
        }
        @Override
        void disableAccelerationForSurface() {
            setSurfaceLost(true);
            invalidate();
            flush();
            peer.disableAcceleration();
            ScreenUpdateManager.getInstance().dropScreenSurface(this);
        }
        @Override
        void restoreSurface() {
            if (!peer.isAccelCapable()) {
                throw new InvalidPipeException("Onscreen acceleration " +
                                               "disabled for this surface");
            }
            Window fsw = graphicsDevice.getFullScreenWindow();
            if (fsw != null && fsw != peer.getTarget()) {
                throw new InvalidPipeException("Can't restore onscreen surface"+
                                               " when in full-screen mode");
            }
            super.restoreSurface();
            setSurfaceLost(false);
            D3DRenderQueue rq = D3DRenderQueue.getInstance();
            rq.lock();
            try {
                getContext().invalidateContext();
            } finally {
                rq.unlock();
            }
        }
        public boolean isDirty() {
            return !dirtyTracker.isCurrent();
        }
        public void markClean() {
            dirtyTracker = getStateTracker();
        }
    }
    public static native boolean updateWindowAccelImpl(long pd3dsd, long pData,
                                                       int w, int h);
}
