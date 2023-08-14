public abstract class OGLSurfaceData extends SurfaceData
    implements AccelSurface {
    public static final int PBUFFER         = RT_PLAIN;
    public static final int FBOBJECT        = RT_TEXTURE;
    public static final int PF_INT_ARGB        = 0;
    public static final int PF_INT_ARGB_PRE    = 1;
    public static final int PF_INT_RGB         = 2;
    public static final int PF_INT_RGBX        = 3;
    public static final int PF_INT_BGR         = 4;
    public static final int PF_INT_BGRX        = 5;
    public static final int PF_USHORT_565_RGB  = 6;
    public static final int PF_USHORT_555_RGB  = 7;
    public static final int PF_USHORT_555_RGBX = 8;
    public static final int PF_BYTE_GRAY       = 9;
    public static final int PF_USHORT_GRAY     = 10;
    public static final int PF_3BYTE_BGR       = 11;
    private static final String DESC_OPENGL_SURFACE = "OpenGL Surface";
    private static final String DESC_OPENGL_SURFACE_RTT =
        "OpenGL Surface (render-to-texture)";
    private static final String DESC_OPENGL_TEXTURE = "OpenGL Texture";
    static final SurfaceType OpenGLSurface =
        SurfaceType.Any.deriveSubType(DESC_OPENGL_SURFACE,
                                      PixelConverter.ArgbPre.instance);
    static final SurfaceType OpenGLSurfaceRTT =
        OpenGLSurface.deriveSubType(DESC_OPENGL_SURFACE_RTT);
    static final SurfaceType OpenGLTexture =
        SurfaceType.Any.deriveSubType(DESC_OPENGL_TEXTURE);
    private static boolean isFBObjectEnabled;
    private static boolean isLCDShaderEnabled;
    private static boolean isBIOpShaderEnabled;
    private static boolean isGradShaderEnabled;
    private OGLGraphicsConfig graphicsConfig;
    protected int type;
    private int nativeWidth, nativeHeight;
    protected static OGLRenderer oglRenderPipe;
    protected static PixelToParallelogramConverter oglTxRenderPipe;
    protected static ParallelogramPipe oglAAPgramPipe;
    protected static OGLTextRenderer oglTextPipe;
    protected static OGLDrawImage oglImagePipe;
    protected native boolean initTexture(long pData,
                                         boolean isOpaque, boolean texNonPow2,
                                         boolean texRect,
                                         int width, int height);
    protected native boolean initFBObject(long pData,
                                          boolean isOpaque, boolean texNonPow2,
                                          boolean texRect,
                                          int width, int height);
    protected native boolean initFlipBackbuffer(long pData);
    protected abstract boolean initPbuffer(long pData, long pConfigInfo,
                                           boolean isOpaque,
                                           int width, int height);
    private native int getTextureTarget(long pData);
    private native int getTextureID(long pData);
    static {
        if (!GraphicsEnvironment.isHeadless()) {
            String fbo = (String)java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction(
                    "sun.java2d.opengl.fbobject"));
            isFBObjectEnabled = !"false".equals(fbo);
            String lcd = (String)java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction(
                    "sun.java2d.opengl.lcdshader"));
            isLCDShaderEnabled = !"false".equals(lcd);
            String biop = (String)java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction(
                    "sun.java2d.opengl.biopshader"));
            isBIOpShaderEnabled = !"false".equals(biop);
            String grad = (String)java.security.AccessController.doPrivileged(
                new sun.security.action.GetPropertyAction(
                    "sun.java2d.opengl.gradshader"));
            isGradShaderEnabled = !"false".equals(grad);
            OGLRenderQueue rq = OGLRenderQueue.getInstance();
            oglImagePipe = new OGLDrawImage();
            oglTextPipe = new OGLTextRenderer(rq);
            oglRenderPipe = new OGLRenderer(rq);
            if (GraphicsPrimitive.tracingEnabled()) {
                oglTextPipe = oglTextPipe.traceWrap();
            }
            oglAAPgramPipe = oglRenderPipe.getAAParallelogramPipe();
            oglTxRenderPipe =
                new PixelToParallelogramConverter(oglRenderPipe,
                                                  oglRenderPipe,
                                                  1.0, 0.25, true);
            OGLBlitLoops.register();
            OGLMaskFill.register();
            OGLMaskBlit.register();
        }
    }
    protected OGLSurfaceData(OGLGraphicsConfig gc,
                             ColorModel cm, int type)
    {
        super(getCustomSurfaceType(type), cm);
        this.graphicsConfig = gc;
        this.type = type;
        setBlitProxyKey(gc.getProxyKey());
    }
    @Override
    public SurfaceDataProxy makeProxyFor(SurfaceData srcData) {
        return OGLSurfaceDataProxy.createProxy(srcData, graphicsConfig);
    }
    private static SurfaceType getCustomSurfaceType(int oglType) {
        switch (oglType) {
        case TEXTURE:
            return OpenGLTexture;
        case FBOBJECT:
            return OpenGLSurfaceRTT;
        case PBUFFER:
        default:
            return OpenGLSurface;
        }
    }
    private void initSurfaceNow(int width, int height) {
        boolean isOpaque = (getTransparency() == Transparency.OPAQUE);
        boolean success = false;
        switch (type) {
        case PBUFFER:
            success = initPbuffer(getNativeOps(),
                                  graphicsConfig.getNativeConfigInfo(),
                                  isOpaque,
                                  width, height);
            break;
        case TEXTURE:
            success = initTexture(getNativeOps(),
                                  isOpaque, isTexNonPow2Available(),
                                  isTexRectAvailable(),
                                  width, height);
            break;
        case FBOBJECT:
            success = initFBObject(getNativeOps(),
                                   isOpaque, isTexNonPow2Available(),
                                   isTexRectAvailable(),
                                   width, height);
            break;
        case FLIP_BACKBUFFER:
            success = initFlipBackbuffer(getNativeOps());
            break;
        default:
            break;
        }
        if (!success) {
            throw new OutOfMemoryError("can't create offscreen surface");
        }
    }
    protected void initSurface(final int width, final int height) {
        OGLRenderQueue rq = OGLRenderQueue.getInstance();
        rq.lock();
        try {
            switch (type) {
            case TEXTURE:
            case PBUFFER:
            case FBOBJECT:
                OGLContext.setScratchSurface(graphicsConfig);
                break;
            default:
                break;
            }
            rq.flushAndInvokeNow(new Runnable() {
                public void run() {
                    initSurfaceNow(width, height);
                }
            });
        } finally {
            rq.unlock();
        }
    }
    public final OGLContext getContext() {
        return graphicsConfig.getContext();
    }
    final OGLGraphicsConfig getOGLGraphicsConfig() {
        return graphicsConfig;
    }
    public final int getType() {
        return type;
    }
    public final int getTextureTarget() {
        return getTextureTarget(getNativeOps());
    }
    public final int getTextureID() {
        return getTextureID(getNativeOps());
    }
    public long getNativeResource(int resType) {
        if (resType == TEXTURE) {
            return getTextureID();
        }
        return 0L;
    }
    public Raster getRaster(int x, int y, int w, int h) {
        throw new InternalError("not implemented yet");
    }
    public boolean canRenderLCDText(SunGraphics2D sg2d) {
        return
            graphicsConfig.isCapPresent(CAPS_EXT_LCD_SHADER) &&
            sg2d.compositeState <= SunGraphics2D.COMP_ISCOPY &&
            sg2d.paintState <= SunGraphics2D.PAINT_OPAQUECOLOR &&
            sg2d.surfaceData.getTransparency() == Transparency.OPAQUE;
    }
    public void validatePipe(SunGraphics2D sg2d) {
        TextPipe textpipe;
        boolean validated = false;
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
            textpipe = oglTextPipe;
        } else {
            super.validatePipe(sg2d);
            textpipe = sg2d.textpipe;
            validated = true;
        }
        PixelToParallelogramConverter txPipe = null;
        OGLRenderer nonTxPipe = null;
        if (sg2d.antialiasHint != SunHints.INTVAL_ANTIALIAS_ON) {
            if (sg2d.paintState <= sg2d.PAINT_ALPHACOLOR) {
                if (sg2d.compositeState <= sg2d.COMP_XOR) {
                    txPipe = oglTxRenderPipe;
                    nonTxPipe = oglRenderPipe;
                }
            } else if (sg2d.compositeState <= sg2d.COMP_ALPHA) {
                if (OGLPaints.isValid(sg2d)) {
                    txPipe = oglTxRenderPipe;
                    nonTxPipe = oglRenderPipe;
                }
            }
        } else {
            if (sg2d.paintState <= sg2d.PAINT_ALPHACOLOR) {
                if (graphicsConfig.isCapPresent(CAPS_PS30) &&
                    (sg2d.imageComp == CompositeType.SrcOverNoEa ||
                     sg2d.imageComp == CompositeType.SrcOver))
                {
                    if (!validated) {
                        super.validatePipe(sg2d);
                        validated = true;
                    }
                    PixelToParallelogramConverter aaConverter =
                        new PixelToParallelogramConverter(sg2d.shapepipe,
                                                          oglAAPgramPipe,
                                                          1.0/8.0, 0.499,
                                                          false);
                    sg2d.drawpipe = aaConverter;
                    sg2d.fillpipe = aaConverter;
                    sg2d.shapepipe = aaConverter;
                } else if (sg2d.compositeState == sg2d.COMP_XOR) {
                    txPipe = oglTxRenderPipe;
                    nonTxPipe = oglRenderPipe;
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
        sg2d.imagepipe = oglImagePipe;
    }
    @Override
    protected MaskFill getMaskFill(SunGraphics2D sg2d) {
        if (sg2d.paintState > sg2d.PAINT_ALPHACOLOR) {
            if (!OGLPaints.isValid(sg2d) ||
                !graphicsConfig.isCapPresent(CAPS_MULTITEXTURE))
            {
                return null;
            }
        }
        return super.getMaskFill(sg2d);
    }
    public boolean copyArea(SunGraphics2D sg2d,
                            int x, int y, int w, int h, int dx, int dy)
    {
        if (sg2d.transformState < sg2d.TRANSFORM_TRANSLATESCALE &&
            sg2d.compositeState < sg2d.COMP_XOR)
        {
            x += sg2d.transX;
            y += sg2d.transY;
            oglRenderPipe.copyArea(sg2d, x, y, w, h, dx, dy);
            return true;
        }
        return false;
    }
    public void flush() {
        invalidate();
        OGLRenderQueue rq = OGLRenderQueue.getInstance();
        rq.lock();
        try {
            OGLContext.setScratchSurface(graphicsConfig);
            RenderBuffer buf = rq.getBuffer();
            rq.ensureCapacityAndAlignment(12, 4);
            buf.putInt(FLUSH_SURFACE);
            buf.putLong(getNativeOps());
            rq.flushNow();
        } finally {
            rq.unlock();
        }
    }
    static void dispose(long pData, long pConfigInfo) {
        OGLRenderQueue rq = OGLRenderQueue.getInstance();
        rq.lock();
        try {
            OGLContext.setScratchSurface(pConfigInfo);
            RenderBuffer buf = rq.getBuffer();
            rq.ensureCapacityAndAlignment(12, 4);
            buf.putInt(DISPOSE_SURFACE);
            buf.putLong(pData);
            rq.flushNow();
        } finally {
            rq.unlock();
        }
    }
    static void swapBuffers(long window) {
        OGLRenderQueue rq = OGLRenderQueue.getInstance();
        rq.lock();
        try {
            RenderBuffer buf = rq.getBuffer();
            rq.ensureCapacityAndAlignment(12, 4);
            buf.putInt(SWAP_BUFFERS);
            buf.putLong(window);
            rq.flushNow();
        } finally {
            rq.unlock();
        }
    }
    boolean isTexNonPow2Available() {
        return graphicsConfig.isCapPresent(CAPS_TEXNONPOW2);
    }
    boolean isTexRectAvailable() {
        return graphicsConfig.isCapPresent(CAPS_EXT_TEXRECT);
    }
    public Rectangle getNativeBounds() {
        OGLRenderQueue rq = OGLRenderQueue.getInstance();
        rq.lock();
        try {
            return new Rectangle(nativeWidth, nativeHeight);
        } finally {
            rq.unlock();
        }
    }
}
