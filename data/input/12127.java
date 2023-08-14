public class D3DGraphicsConfig
    extends Win32GraphicsConfig
    implements AccelGraphicsConfig
{
    private static ImageCapabilities imageCaps = new D3DImageCaps();
    private BufferCapabilities bufferCaps;
    private D3DGraphicsDevice device;
    protected D3DGraphicsConfig(D3DGraphicsDevice device) {
        super(device, 0);
        this.device = device;
    }
    public SurfaceData createManagedSurface(int w, int h, int transparency) {
        return D3DSurfaceData.createData(this, w, h,
                                         getColorModel(transparency),
                                         null,
                                         D3DSurfaceData.TEXTURE);
    }
    @Override
    public synchronized void displayChanged() {
        super.displayChanged();
        D3DRenderQueue rq = D3DRenderQueue.getInstance();
        rq.lock();
        try {
            D3DContext.invalidateCurrentContext();
        } finally {
            rq.unlock();
        }
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
    @Override
    public String toString() {
        return ("D3DGraphicsConfig[dev="+screen+",pixfmt="+visual+"]");
    }
    @Override
    public SurfaceData createSurfaceData(WComponentPeer peer,
                                         int numBackBuffers)
    {
        return super.createSurfaceData(peer, numBackBuffers);
    }
    @Override
    public void assertOperationSupported(Component target,
                                         int numBuffers,
                                         BufferCapabilities caps)
        throws AWTException
    {
        if (numBuffers < 2 || numBuffers > 4) {
            throw new AWTException("Only 2-4 buffers supported");
        }
        if (caps.getFlipContents() == BufferCapabilities.FlipContents.COPIED &&
            numBuffers != 2)
        {
            throw new AWTException("FlipContents.COPIED is only" +
                                   "supported for 2 buffers");
        }
    }
    @Override
    public VolatileImage createBackBuffer(WComponentPeer peer) {
        Component target = (Component)peer.getTarget();
        int w = Math.max(1, target.getWidth());
        int h = Math.max(1, target.getHeight());
        return new SunVolatileImage(target, w, h, Boolean.TRUE);
    }
    @Override
    public void flip(WComponentPeer peer,
                     Component target, VolatileImage backBuffer,
                     int x1, int y1, int x2, int y2,
                     BufferCapabilities.FlipContents flipAction)
    {
        SurfaceManager d3dvsm =
            SurfaceManager.getManager(backBuffer);
        SurfaceData sd = d3dvsm.getPrimarySurfaceData();
        if (sd instanceof D3DSurfaceData) {
            D3DSurfaceData d3dsd = (D3DSurfaceData)sd;
            D3DSurfaceData.swapBuffers(d3dsd, x1, y1, x2, y2);
        } else {
            Graphics g = peer.getGraphics();
            try {
                g.drawImage(backBuffer,
                            x1, y1, x2, y2,
                            x1, y1, x2, y2,
                            null);
            } finally {
                g.dispose();
            }
        }
        if (flipAction == BufferCapabilities.FlipContents.BACKGROUND) {
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
    private static class D3DBufferCaps extends BufferCapabilities {
        public D3DBufferCaps() {
            super(imageCaps, imageCaps, FlipContents.UNDEFINED);
        }
        @Override
        public boolean isMultiBufferAvailable() {
            return true;
        }
    }
    @Override
    public BufferCapabilities getBufferCapabilities() {
        if (bufferCaps == null) {
            bufferCaps = new D3DBufferCaps();
        }
        return bufferCaps;
    }
    private static class D3DImageCaps extends ImageCapabilities {
        private D3DImageCaps() {
            super(true);
        }
        @Override
        public boolean isTrueVolatile() {
            return true;
        }
    }
    @Override
    public ImageCapabilities getImageCapabilities() {
        return imageCaps;
    }
    D3DGraphicsDevice getD3DDevice() {
        return device;
    }
    @Override
    public D3DContext getContext() {
        return device.getContext();
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
        boolean isOpaque = transparency == Transparency.OPAQUE;
        if (type == RT_TEXTURE) {
            int cap = isOpaque ? CAPS_RT_TEXTURE_OPAQUE : CAPS_RT_TEXTURE_ALPHA;
            if (!device.isCapPresent(cap)) {
                return null;
            }
        } else if (type == RT_PLAIN) {
            if (!isOpaque && !device.isCapPresent(CAPS_RT_PLAIN_ALPHA)) {
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
        return device.getContextCapabilities();
    }
    @Override
    public void addDeviceEventListener(AccelDeviceEventListener l) {
        AccelDeviceEventNotifier.addListener(l, device.getScreen());
    }
    @Override
    public void removeDeviceEventListener(AccelDeviceEventListener l) {
        AccelDeviceEventNotifier.removeListener(l);
    }
}
