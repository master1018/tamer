public class GLXGraphicsConfig
    extends X11GraphicsConfig
    implements OGLGraphicsConfig
{
    private static ImageCapabilities imageCaps = new GLXImageCaps();
    private BufferCapabilities bufferCaps;
    private long pConfigInfo;
    private ContextCapabilities oglCaps;
    private OGLContext context;
    private static native long getGLXConfigInfo(int screennum, int visualnum);
    private static native int getOGLCapabilities(long configInfo);
    private native void initConfig(long aData, long ctxinfo);
    private GLXGraphicsConfig(X11GraphicsDevice device, int visualnum,
                              long configInfo, ContextCapabilities oglCaps)
    {
        super(device, visualnum, 0, 0,
              (oglCaps.getCaps() & CAPS_DOUBLEBUFFERED) != 0);
        pConfigInfo = configInfo;
        initConfig(getAData(), configInfo);
        this.oglCaps = oglCaps;
        context = new OGLContext(OGLRenderQueue.getInstance(), this);
    }
    @Override
    public Object getProxyKey() {
        return this;
    }
    @Override
    public SurfaceData createManagedSurface(int w, int h, int transparency) {
        return GLXSurfaceData.createData(this, w, h,
                                         getColorModel(transparency),
                                         null,
                                         OGLSurfaceData.TEXTURE);
    }
    public static GLXGraphicsConfig getConfig(X11GraphicsDevice device,
                                              int visualnum)
    {
        if (!X11GraphicsEnvironment.isGLXAvailable()) {
            return null;
        }
        long cfginfo = 0;
        final String ids[] = new String[1];
        OGLRenderQueue rq = OGLRenderQueue.getInstance();
        rq.lock();
        try {
            OGLContext.invalidateCurrentContext();
            GLXGetConfigInfo action =
                new GLXGetConfigInfo(device.getScreen(), visualnum);
            rq.flushAndInvokeNow(action);
            cfginfo = action.getConfigInfo();
            if (cfginfo != 0L) {
                OGLContext.setScratchSurface(cfginfo);
                rq.flushAndInvokeNow(new Runnable() {
                    public void run() {
                        ids[0] = OGLContext.getOGLIdString();
                    }
                });
            }
        } finally {
            rq.unlock();
        }
        if (cfginfo == 0) {
            return null;
        }
        int oglCaps = getOGLCapabilities(cfginfo);
        ContextCapabilities caps = new OGLContextCaps(oglCaps, ids[0]);
        return new GLXGraphicsConfig(device, visualnum, cfginfo, caps);
    }
    private static class GLXGetConfigInfo implements Runnable {
        private int screen;
        private int visual;
        private long cfginfo;
        private GLXGetConfigInfo(int screen, int visual) {
            this.screen = screen;
            this.visual = visual;
        }
        public void run() {
            cfginfo = getGLXConfigInfo(screen, visual);
        }
        public long getConfigInfo() {
            return cfginfo;
        }
    }
    @Override
    public final boolean isCapPresent(int cap) {
        return ((oglCaps.getCaps() & cap) != 0);
    }
    @Override
    public final long getNativeConfigInfo() {
        return pConfigInfo;
    }
    @Override
    public final OGLContext getContext() {
        return context;
    }
    @Override
    public BufferedImage createCompatibleImage(int width, int height) {
        ColorModel model = new DirectColorModel(24, 0xff0000, 0xff00, 0xff);
        WritableRaster
            raster = model.createCompatibleWritableRaster(width, height);
        return new BufferedImage(model, raster, model.isAlphaPremultiplied(),
                                 null);
    }
    @Override
    public ColorModel getColorModel(int transparency) {
        switch (transparency) {
        case Transparency.OPAQUE:
            return new DirectColorModel(24, 0xff0000, 0xff00, 0xff);
        case Transparency.BITMASK:
            return new DirectColorModel(25, 0xff0000, 0xff00, 0xff, 0x1000000);
        case Transparency.TRANSLUCENT:
            ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            return new DirectColorModel(cs, 32,
                                        0xff0000, 0xff00, 0xff, 0xff000000,
                                        true, DataBuffer.TYPE_INT);
        default:
            return null;
        }
    }
    public String toString() {
        return ("GLXGraphicsConfig[dev="+screen+
                ",vis=0x"+Integer.toHexString(visual)+
                "]");
    }
    @Override
    public SurfaceData createSurfaceData(X11ComponentPeer peer) {
        return GLXSurfaceData.createData(peer);
    }
    @Override
    public Image createAcceleratedImage(Component target,
                                        int width, int height)
    {
        ColorModel model = getColorModel(Transparency.OPAQUE);
        WritableRaster wr =
            model.createCompatibleWritableRaster(width, height);
        return new OffScreenImage(target, model, wr,
                                  model.isAlphaPremultiplied());
    }
    @Override
    public long createBackBuffer(X11ComponentPeer peer,
                                 int numBuffers, BufferCapabilities caps)
        throws AWTException
    {
        if (numBuffers > 2) {
            throw new AWTException(
                "Only double or single buffering is supported");
        }
        BufferCapabilities configCaps = getBufferCapabilities();
        if (!configCaps.isPageFlipping()) {
            throw new AWTException("Page flipping is not supported");
        }
        if (caps.getFlipContents() == BufferCapabilities.FlipContents.PRIOR) {
            throw new AWTException("FlipContents.PRIOR is not supported");
        }
        return 1;
    }
    @Override
    public void destroyBackBuffer(long backBuffer) {
    }
    @Override
    public VolatileImage createBackBufferImage(Component target,
                                               long backBuffer)
    {
        return new SunVolatileImage(target,
                                    target.getWidth(), target.getHeight(),
                                    Boolean.TRUE);
    }
    @Override
    public void flip(X11ComponentPeer peer,
                     Component target, VolatileImage xBackBuffer,
                     int x1, int y1, int x2, int y2,
                     BufferCapabilities.FlipContents flipAction)
    {
        if (flipAction == BufferCapabilities.FlipContents.COPIED) {
            SurfaceManager vsm = SurfaceManager.getManager(xBackBuffer);
            SurfaceData sd = vsm.getPrimarySurfaceData();
            if (sd instanceof GLXVSyncOffScreenSurfaceData) {
                GLXVSyncOffScreenSurfaceData vsd =
                    (GLXVSyncOffScreenSurfaceData)sd;
                SurfaceData bbsd = vsd.getFlipSurface();
                Graphics2D bbg =
                    new SunGraphics2D(bbsd, Color.black, Color.white, null);
                try {
                    bbg.drawImage(xBackBuffer, 0, 0, null);
                } finally {
                    bbg.dispose();
                }
            } else {
                Graphics g = peer.getGraphics();
                try {
                    g.drawImage(xBackBuffer,
                                x1, y1, x2, y2,
                                x1, y1, x2, y2,
                                null);
                } finally {
                    g.dispose();
                }
                return;
            }
        } else if (flipAction == BufferCapabilities.FlipContents.PRIOR) {
            return;
        }
        OGLSurfaceData.swapBuffers(peer.getContentWindow());
        if (flipAction == BufferCapabilities.FlipContents.BACKGROUND) {
            Graphics g = xBackBuffer.getGraphics();
            try {
                g.setColor(target.getBackground());
                g.fillRect(0, 0,
                           xBackBuffer.getWidth(),
                           xBackBuffer.getHeight());
            } finally {
                g.dispose();
            }
        }
    }
    private static class GLXBufferCaps extends BufferCapabilities {
        public GLXBufferCaps(boolean dblBuf) {
            super(imageCaps, imageCaps,
                  dblBuf ? FlipContents.UNDEFINED : null);
        }
    }
    @Override
    public BufferCapabilities getBufferCapabilities() {
        if (bufferCaps == null) {
            bufferCaps = new GLXBufferCaps(isDoubleBuffered());
        }
        return bufferCaps;
    }
    private static class GLXImageCaps extends ImageCapabilities {
        private GLXImageCaps() {
            super(true);
        }
        public boolean isTrueVolatile() {
            return true;
        }
    }
    @Override
    public ImageCapabilities getImageCapabilities() {
        return imageCaps;
    }
    @Override
    public VolatileImage
        createCompatibleVolatileImage(int width, int height,
                                      int transparency, int type)
    {
        if (type == FLIP_BACKBUFFER || type == WINDOW || type == UNDEFINED ||
            transparency == Transparency.BITMASK)
        {
            return null;
        }
        if (type == FBOBJECT) {
            if (!isCapPresent(CAPS_EXT_FBOBJECT)) {
                return null;
            }
        } else if (type == PBUFFER) {
            boolean isOpaque = transparency == Transparency.OPAQUE;
            if (!isOpaque && !isCapPresent(CAPS_STORED_ALPHA)) {
                return null;
            }
        }
        SunVolatileImage vi = new AccelTypedVolatileImage(this, width, height,
                                                          transparency, type);
        Surface sd = vi.getDestSurface();
        if (!(sd instanceof AccelSurface) ||
            ((AccelSurface)sd).getType() != type)
        {
            vi.flush();
            vi = null;
        }
        return vi;
    }
    @Override
    public ContextCapabilities getContextCapabilities() {
        return oglCaps;
    }
    @Override
    public void addDeviceEventListener(AccelDeviceEventListener l) {
        AccelDeviceEventNotifier.addListener(l, screen.getScreen());
    }
    @Override
    public void removeDeviceEventListener(AccelDeviceEventListener l) {
        AccelDeviceEventNotifier.removeListener(l);
    }
}
