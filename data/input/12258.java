public abstract class TranslucentWindowPainter {
    protected Window window;
    protected WWindowPeer peer;
    private static final boolean forceOpt  =
        Boolean.valueOf(AccessController.doPrivileged(
            new GetPropertyAction("sun.java2d.twp.forceopt", "false")));
    private static final boolean forceSW  =
        Boolean.valueOf(AccessController.doPrivileged(
            new GetPropertyAction("sun.java2d.twp.forcesw", "false")));
    public static TranslucentWindowPainter createInstance(WWindowPeer peer) {
        GraphicsConfiguration gc = peer.getGraphicsConfiguration();
        if (!forceSW && gc instanceof AccelGraphicsConfig) {
            String gcName = gc.getClass().getSimpleName();
            AccelGraphicsConfig agc = (AccelGraphicsConfig)gc;
            if ((agc.getContextCapabilities().getCaps() & CAPS_PS30) != 0 ||
                forceOpt)
            {
                if (gcName.startsWith("D3D")) {
                    return new VIOptD3DWindowPainter(peer);
                } else if (forceOpt && gcName.startsWith("WGL")) {
                    return new VIOptWGLWindowPainter(peer);
                }
            }
        }
        return new BIWindowPainter(peer);
    }
    protected TranslucentWindowPainter(WWindowPeer peer) {
        this.peer = peer;
        this.window = (Window)peer.getTarget();
    }
    protected abstract Image getBackBuffer(boolean clear);
    protected abstract boolean update(Image bb);
    public abstract void flush();
    public void updateWindow(boolean repaint) {
        boolean done = false;
        Image bb = getBackBuffer(repaint);
        while (!done) {
            if (repaint) {
                Graphics2D g = (Graphics2D)bb.getGraphics();
                try {
                    window.paintAll(g);
                } finally {
                    g.dispose();
                }
            }
            done = update(bb);
            if (!done) {
                repaint = true;
                bb = getBackBuffer(true);
            }
        }
    }
    private static final Image clearImage(Image bb) {
        Graphics2D g = (Graphics2D)bb.getGraphics();
        int w = bb.getWidth(null);
        int h = bb.getHeight(null);
        g.setComposite(AlphaComposite.Src);
        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, w, h);
        return bb;
    }
    private static class BIWindowPainter extends TranslucentWindowPainter {
        private BufferedImage backBuffer;
        protected BIWindowPainter(WWindowPeer peer) {
            super(peer);
        }
        @Override
        protected Image getBackBuffer(boolean clear) {
            int w = window.getWidth();
            int h = window.getHeight();
            if (backBuffer == null ||
                backBuffer.getWidth() != w ||
                backBuffer.getHeight() != h)
            {
                flush();
                backBuffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB_PRE);
            }
            return clear ? (BufferedImage)clearImage(backBuffer) : backBuffer;
        }
        @Override
        protected boolean update(Image bb) {
            VolatileImage viBB = null;
            if (bb instanceof BufferedImage) {
                BufferedImage bi = (BufferedImage)bb;
                int data[] =
                    ((DataBufferInt)bi.getRaster().getDataBuffer()).getData();
                peer.updateWindowImpl(data, bi.getWidth(), bi.getHeight());
                return true;
            } else if (bb instanceof VolatileImage) {
                viBB = (VolatileImage)bb;
                if (bb instanceof DestSurfaceProvider) {
                    Surface s = ((DestSurfaceProvider)bb).getDestSurface();
                    if (s instanceof BufImgSurfaceData) {
                        int w = viBB.getWidth();
                        int h = viBB.getHeight();
                        BufImgSurfaceData bisd = (BufImgSurfaceData)s;
                        int data[] = ((DataBufferInt)bisd.getRaster(0,0,w,h).
                            getDataBuffer()).getData();
                        peer.updateWindowImpl(data, w, h);
                        return true;
                    }
                }
            }
            BufferedImage bi = (BufferedImage)clearImage(backBuffer);
            int data[] =
                ((DataBufferInt)bi.getRaster().getDataBuffer()).getData();
            peer.updateWindowImpl(data, bi.getWidth(), bi.getHeight());
            return (viBB != null ? !viBB.contentsLost() : true);
        }
        public void flush() {
            if (backBuffer != null) {
                backBuffer.flush();
                backBuffer = null;
            }
        }
    }
    private static class VIWindowPainter extends BIWindowPainter {
        private VolatileImage viBB;
        protected VIWindowPainter(WWindowPeer peer) {
            super(peer);
        }
        @Override
        protected Image getBackBuffer(boolean clear) {
            int w = window.getWidth();
            int h = window.getHeight();
            GraphicsConfiguration gc = peer.getGraphicsConfiguration();
            if (viBB == null || viBB.getWidth() != w || viBB.getHeight() != h ||
                viBB.validate(gc) == IMAGE_INCOMPATIBLE)
            {
                flush();
                if (gc instanceof AccelGraphicsConfig) {
                    AccelGraphicsConfig agc = ((AccelGraphicsConfig)gc);
                    viBB = agc.createCompatibleVolatileImage(w, h,
                                                             TRANSLUCENT,
                                                             RT_PLAIN);
                }
                if (viBB == null) {
                    viBB = gc.createCompatibleVolatileImage(w, h, TRANSLUCENT);
                }
                viBB.validate(gc);
            }
            return clear ? clearImage(viBB) : viBB;
        }
        @Override
        public void flush() {
            if (viBB != null) {
                viBB.flush();
                viBB = null;
            }
        }
    }
    private abstract static class VIOptWindowPainter extends VIWindowPainter {
        protected VIOptWindowPainter(WWindowPeer peer) {
            super(peer);
        }
        protected abstract boolean updateWindowAccel(long psdops, int w, int h);
        @Override
        protected boolean update(Image bb) {
            if (bb instanceof DestSurfaceProvider) {
                Surface s = ((DestSurfaceProvider)bb).getDestSurface();
                if (s instanceof AccelSurface) {
                    final int w = bb.getWidth(null);
                    final int h = bb.getHeight(null);
                    final boolean arr[] = { false };
                    final AccelSurface as = (AccelSurface)s;
                    RenderQueue rq = as.getContext().getRenderQueue();
                    rq.lock();
                    try {
                        as.getContext().validateContext(as);
                        rq.flushAndInvokeNow(new Runnable() {
                            public void run() {
                                long psdops = as.getNativeOps();
                                arr[0] = updateWindowAccel(psdops, w, h);
                            }
                        });
                    } catch (InvalidPipeException e) {
                    } finally {
                        rq.unlock();
                    }
                    return arr[0];
                }
            }
            return super.update(bb);
        }
    }
    private static class VIOptD3DWindowPainter extends VIOptWindowPainter {
        protected VIOptD3DWindowPainter(WWindowPeer peer) {
            super(peer);
        }
        @Override
        protected boolean updateWindowAccel(long psdops, int w, int h) {
            return sun.java2d.d3d.D3DSurfaceData.
                updateWindowAccelImpl(psdops, peer.getData(), w, h);
        }
    }
    private static class VIOptWGLWindowPainter extends VIOptWindowPainter {
        protected VIOptWGLWindowPainter(WWindowPeer peer) {
            super(peer);
        }
        @Override
        protected boolean updateWindowAccel(long psdops, int w, int h) {
            return sun.java2d.opengl.WGLSurfaceData.
                updateWindowAccelImpl(psdops, peer, w, h);
        }
    }
}
