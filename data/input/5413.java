public abstract class GLXSurfaceData extends OGLSurfaceData {
    protected X11ComponentPeer peer;
    private GLXGraphicsConfig graphicsConfig;
    private native void initOps(X11ComponentPeer peer, long aData);
    protected native boolean initPbuffer(long pData, long pConfigInfo,
                                         boolean isOpaque,
                                         int width, int height);
    protected GLXSurfaceData(X11ComponentPeer peer, GLXGraphicsConfig gc,
                             ColorModel cm, int type)
    {
        super(gc, cm, type);
        this.peer = peer;
        this.graphicsConfig = gc;
        initOps(peer, graphicsConfig.getAData());
    }
    public GraphicsConfiguration getDeviceConfiguration() {
        return graphicsConfig;
    }
    public static GLXWindowSurfaceData createData(X11ComponentPeer peer) {
        GLXGraphicsConfig gc = getGC(peer);
        return new GLXWindowSurfaceData(peer, gc);
    }
    public static GLXOffScreenSurfaceData createData(X11ComponentPeer peer,
                                                     Image image,
                                                     int type)
    {
        GLXGraphicsConfig gc = getGC(peer);
        Rectangle r = peer.getBounds();
        if (type == FLIP_BACKBUFFER) {
            return new GLXOffScreenSurfaceData(peer, gc, r.width, r.height,
                                               image, peer.getColorModel(),
                                               FLIP_BACKBUFFER);
        } else {
            return new GLXVSyncOffScreenSurfaceData(peer, gc, r.width, r.height,
                                                    image, peer.getColorModel(),
                                                    type);
        }
    }
    public static GLXOffScreenSurfaceData createData(GLXGraphicsConfig gc,
                                                     int width, int height,
                                                     ColorModel cm,
                                                     Image image, int type)
    {
        return new GLXOffScreenSurfaceData(null, gc, width, height,
                                           image, cm, type);
    }
    public static GLXGraphicsConfig getGC(X11ComponentPeer peer) {
        if (peer != null) {
            return (GLXGraphicsConfig)peer.getGraphicsConfiguration();
        } else {
            GraphicsEnvironment env =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice gd = env.getDefaultScreenDevice();
            return (GLXGraphicsConfig)gd.getDefaultConfiguration();
        }
    }
    public static class GLXWindowSurfaceData extends GLXSurfaceData {
        public GLXWindowSurfaceData(X11ComponentPeer peer,
                                    GLXGraphicsConfig gc)
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
    public static class GLXVSyncOffScreenSurfaceData extends
        GLXOffScreenSurfaceData
    {
        private GLXOffScreenSurfaceData flipSurface;
        public GLXVSyncOffScreenSurfaceData(X11ComponentPeer peer,
                                            GLXGraphicsConfig gc,
                                            int width, int height,
                                            Image image, ColorModel cm,
                                            int type)
        {
            super(peer, gc, width, height, image, cm, type);
            flipSurface = GLXSurfaceData.createData(peer, image, FLIP_BACKBUFFER);
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
    public static class GLXOffScreenSurfaceData extends GLXSurfaceData {
        private Image offscreenImage;
        private int width, height;
        public GLXOffScreenSurfaceData(X11ComponentPeer peer,
                                       GLXGraphicsConfig gc,
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
}
