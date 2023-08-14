class OGLUtilities {
    public static final int UNDEFINED       = OGLSurfaceData.UNDEFINED;
    public static final int WINDOW          = OGLSurfaceData.WINDOW;
    public static final int PBUFFER         = OGLSurfaceData.PBUFFER;
    public static final int TEXTURE         = OGLSurfaceData.TEXTURE;
    public static final int FLIP_BACKBUFFER = OGLSurfaceData.FLIP_BACKBUFFER;
    public static final int FBOBJECT        = OGLSurfaceData.FBOBJECT;
    private OGLUtilities() {
    }
    public static boolean isQueueFlusherThread() {
        return OGLRenderQueue.isQueueFlusherThread();
    }
    public static boolean invokeWithOGLContextCurrent(Graphics g, Runnable r) {
        OGLRenderQueue rq = OGLRenderQueue.getInstance();
        rq.lock();
        try {
            if (g != null) {
                if (!(g instanceof SunGraphics2D)) {
                    return false;
                }
                SurfaceData sData = ((SunGraphics2D)g).surfaceData;
                if (!(sData instanceof OGLSurfaceData)) {
                    return false;
                }
                OGLContext.validateContext((OGLSurfaceData)sData);
            }
            rq.flushAndInvokeNow(r);
            OGLContext.invalidateCurrentContext();
        } finally {
            rq.unlock();
        }
        return true;
    }
    public static boolean
        invokeWithOGLSharedContextCurrent(GraphicsConfiguration config,
                                          Runnable r)
    {
        if (!(config instanceof OGLGraphicsConfig)) {
            return false;
        }
        OGLRenderQueue rq = OGLRenderQueue.getInstance();
        rq.lock();
        try {
            OGLContext.setScratchSurface((OGLGraphicsConfig)config);
            rq.flushAndInvokeNow(r);
            OGLContext.invalidateCurrentContext();
        } finally {
            rq.unlock();
        }
        return true;
    }
    public static Rectangle getOGLViewport(Graphics g,
                                           int componentWidth,
                                           int componentHeight)
    {
        if (!(g instanceof SunGraphics2D)) {
            return null;
        }
        SunGraphics2D sg2d = (SunGraphics2D)g;
        SurfaceData sData = (SurfaceData)sg2d.surfaceData;
        int x0 = sg2d.transX;
        int y0 = sg2d.transY;
        Rectangle surfaceBounds = sData.getBounds();
        int x1 = x0;
        int y1 = surfaceBounds.height - (y0 + componentHeight);
        return new Rectangle(x1, y1, componentWidth, componentHeight);
    }
    public static Rectangle getOGLScissorBox(Graphics g) {
        if (!(g instanceof SunGraphics2D)) {
            return null;
        }
        SunGraphics2D sg2d = (SunGraphics2D)g;
        SurfaceData sData = (SurfaceData)sg2d.surfaceData;
        Region r = sg2d.getCompClip();
        if (!r.isRectangular()) {
            return null;
        }
        int x0 = r.getLoX();
        int y0 = r.getLoY();
        int w = r.getWidth();
        int h = r.getHeight();
        Rectangle surfaceBounds = sData.getBounds();
        int x1 = x0;
        int y1 = surfaceBounds.height - (y0 + h);
        return new Rectangle(x1, y1, w, h);
    }
    public static Object getOGLSurfaceIdentifier(Graphics g) {
        if (!(g instanceof SunGraphics2D)) {
            return null;
        }
        return ((SunGraphics2D)g).surfaceData;
    }
    public static int getOGLSurfaceType(Graphics g) {
        if (!(g instanceof SunGraphics2D)) {
            return UNDEFINED;
        }
        SurfaceData sData = ((SunGraphics2D)g).surfaceData;
        if (!(sData instanceof OGLSurfaceData)) {
            return UNDEFINED;
        }
        return ((OGLSurfaceData)sData).getType();
    }
    public static int getOGLTextureType(Graphics g) {
        if (!(g instanceof SunGraphics2D)) {
            return 0;
        }
        SurfaceData sData = ((SunGraphics2D)g).surfaceData;
        if (!(sData instanceof OGLSurfaceData)) {
            return 0;
        }
        return ((OGLSurfaceData)sData).getTextureTarget();
    }
}
