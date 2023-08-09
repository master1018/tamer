public abstract class WGLSurfaceData extends OGLSurfaceData {
    protected WComponentPeer peer;
    private WGLGraphicsConfig graphicsConfig;
    private native void initOps(long pConfigInfo, WComponentPeer peer,
                                long hwnd);
    protected native boolean initPbuffer(long pData, long pConfigInfo,
                                         boolean isOpaque,
                                         int width, int height);
    protected WGLSurfaceData(WComponentPeer peer, WGLGraphicsConfig gc,
                             ColorModel cm, int type)
    {
        super(gc, cm, type);
        this.peer = peer;
        this.graphicsConfig = gc;
        long pConfigInfo = gc.getNativeConfigInfo();
        long hwnd = peer != null ? peer.getHWnd() : 0L;
        initOps(pConfigInfo, peer, hwnd);
    }
    public GraphicsConfiguration getDeviceConfiguration() {
        return graphicsConfig;
    }
    public static WGLWindowSurfaceData createData(WComponentPeer peer) {
        if (!peer.isAccelCapable() ||
            !SunToolkit.isContainingTopLevelOpaque((Component)peer.getTarget()))
        {
            return null;
        }
        WGLGraphicsConfig gc = getGC(peer);
        return new WGLWindowSurfaceData(peer, gc);
    }
    public static WGLOffScreenSurfaceData createData(WComponentPeer peer,
                                                     Image image,
                                                     int type)
    {
        if (!peer.isAccelCapable() ||
            !SunToolkit.isContainingTopLevelOpaque((Component)peer.getTarget()))
        {
            return null;
        }
        WGLGraphicsConfig gc = getGC(peer);
        Rectangle r = peer.getBounds();
        if (type == FLIP_BACKBUFFER) {
            return new WGLOffScreenSurfaceData(peer, gc, r.width, r.height,
                                               image, peer.getColorModel(),
                                               type);
        } else {
            return new WGLVSyncOffScreenSurfaceData(peer, gc, r.width, r.height,
                                                    image, peer.getColorModel(),
                                                    type);
        }
    }
    public static WGLOffScreenSurfaceData createData(WGLGraphicsConfig gc,
                                                     int width, int height,
                                                     ColorModel cm,
                                                     Image image, int type)
    {
        return new WGLOffScreenSurfaceData(null, gc, width, height,
                                           image, cm, type);
    }
    public static WGLGraphicsConfig getGC(WComponentPeer peer) {
        if (peer != null) {
            return (WGLGraphicsConfig)peer.getGraphicsConfiguration();
        } else {
            GraphicsEnvironment env =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = env.getDefaultScreenDevice();
            return (WGLGraphicsConfig)gd.getDefaultConfiguration();
        }
    }
    public static class WGLWindowSurfaceData extends WGLSurfaceData {
        public WGLWindowSurfaceData(WComponentPeer peer,
                                    WGLGraphicsConfig gc)
        {
            super(peer, gc, peer.getColorModel(), WINDOW);
        }
        public SurfaceData getReplacement() {
            return peer.getSurfaceData();
        }
        public Rectangle getBounds() {
            Rectangle r = peer.getBounds();
            r.x = r.y = 0;
            return r;
        }
        public Object getDestination() {
            return peer.getTarget();
        }
    }
    public static class WGLVSyncOffScreenSurfaceData extends
        WGLOffScreenSurfaceData
    {
        private WGLOffScreenSurfaceData flipSurface;
        public WGLVSyncOffScreenSurfaceData(WComponentPeer peer,
                                            WGLGraphicsConfig gc,
                                            int width, int height,
                                            Image image, ColorModel cm,
                                            int type)
        {
            super(peer, gc, width, height, image, cm, type);
            flipSurface = WGLSurfaceData.createData(peer, image, FLIP_BACKBUFFER);
        }
        public SurfaceData getFlipSurface() {
            return flipSurface;
        }
        @Override
        public void flush() {
            flipSurface.flush();
            super.flush();
        }
    }
    public static class WGLOffScreenSurfaceData extends WGLSurfaceData {
        private Image offscreenImage;
        private int width, height;
        public WGLOffScreenSurfaceData(WComponentPeer peer,
                                       WGLGraphicsConfig gc,
                                       int width, int height,
                                       Image image, ColorModel cm,
                                       int type)
        {
            super(peer, gc, cm, type);
            this.width = width;
            this.height = height;
            offscreenImage = image;
            initSurface(width, height);
        }
        public SurfaceData getReplacement() {
            return restoreContents(offscreenImage);
        }
        public Rectangle getBounds() {
            if (type == FLIP_BACKBUFFER) {
                Rectangle r = peer.getBounds();
                r.x = r.y = 0;
                return r;
            } else {
                return new Rectangle(width, height);
            }
        }
        public Object getDestination() {
            return offscreenImage;
        }
    }
    public static native boolean updateWindowAccelImpl(long psdops,
                                                       WComponentPeer peer,
                                                       int w, int h);
}
