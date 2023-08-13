public class D3DScreenUpdateManager extends ScreenUpdateManager
    implements Runnable
{
    private static final int MIN_WIN_SIZE = 150;
    private volatile boolean done;
    private volatile Thread screenUpdater;
    private boolean needsUpdateNow;
    private Object runLock = new Object();
    private ArrayList<D3DWindowSurfaceData> d3dwSurfaces;
    private HashMap<D3DWindowSurfaceData, GDIWindowSurfaceData> gdiSurfaces;
    public D3DScreenUpdateManager() {
        done = false;
        AccessController.doPrivileged(
            new PrivilegedAction() {
                public Object run() {
                    ThreadGroup currentTG =
                        Thread.currentThread().getThreadGroup();
                    ThreadGroup parentTG = currentTG.getParent();
                    while (parentTG != null) {
                        currentTG = parentTG;
                        parentTG = currentTG.getParent();
                    }
                    Thread shutdown = new Thread(currentTG, new Runnable() {
                            public void run() {
                                done = true;
                                wakeUpUpdateThread();
                            }
                        });
                    shutdown.setContextClassLoader(null);
                    try {
                        Runtime.getRuntime().addShutdownHook(shutdown);
                    } catch (Exception e) {
                        done = true;
                    }
                    return null;
                }
            }
        );
    }
    @Override
    public SurfaceData createScreenSurface(Win32GraphicsConfig gc,
                                           WComponentPeer peer,
                                           int bbNum, boolean isResize)
    {
        if (done || !(gc instanceof D3DGraphicsConfig)) {
            return super.createScreenSurface(gc, peer, bbNum, isResize);
        }
        SurfaceData sd = null;
        if (canUseD3DOnScreen(peer, gc, bbNum)) {
            try {
                sd = D3DSurfaceData.createData(peer);
            }  catch (InvalidPipeException ipe) {
                sd = null;
            }
        }
        if (sd == null) {
            sd = GDIWindowSurfaceData.createData(peer);
        }
        if (isResize) {
            repaintPeerTarget(peer);
        }
        return sd;
    }
    public static boolean canUseD3DOnScreen(final WComponentPeer peer,
                                            final Win32GraphicsConfig gc,
                                            final int bbNum)
    {
        if (!(gc instanceof D3DGraphicsConfig)) {
            return false;
        }
        D3DGraphicsConfig d3dgc = (D3DGraphicsConfig)gc;
        D3DGraphicsDevice d3dgd = d3dgc.getD3DDevice();
        String peerName = peer.getClass().getName();
        Rectangle r = peer.getBounds();
        Component target = (Component)peer.getTarget();
        Window fsw = d3dgd.getFullScreenWindow();
        return
            WindowsFlags.isD3DOnScreenEnabled() &&
            d3dgd.isD3DEnabledOnDevice() &&
            peer.isAccelCapable() &&
            (r.width > MIN_WIN_SIZE || r.height > MIN_WIN_SIZE) &&
            bbNum == 0 &&
            (fsw == null || (fsw == target && !hasHWChildren(target))) &&
            (peerName.equals("sun.awt.windows.WCanvasPeer") ||
             peerName.equals("sun.awt.windows.WDialogPeer") ||
             peerName.equals("sun.awt.windows.WPanelPeer")  ||
             peerName.equals("sun.awt.windows.WWindowPeer") ||
             peerName.equals("sun.awt.windows.WFramePeer")  ||
             peerName.equals("sun.awt.windows.WEmbeddedFramePeer"));
    }
    @Override
    public Graphics2D createGraphics(SurfaceData sd,
            WComponentPeer peer, Color fgColor, Color bgColor, Font font)
    {
        if (!done && sd instanceof D3DWindowSurfaceData) {
            D3DWindowSurfaceData d3dw = (D3DWindowSurfaceData)sd;
            if (!d3dw.isSurfaceLost() || validate(d3dw)) {
                trackScreenSurface(d3dw);
                return new SunGraphics2D(sd, fgColor, bgColor, font);
            }
            sd = getGdiSurface(d3dw);
        }
        return super.createGraphics(sd, peer, fgColor, bgColor, font);
    }
    private void repaintPeerTarget(WComponentPeer peer) {
        Component target = (Component)peer.getTarget();
        Rectangle bounds = AWTAccessor.getComponentAccessor().getBounds(target);
        peer.handlePaint(0, 0, bounds.width, bounds.height);
    }
    private void trackScreenSurface(SurfaceData sd) {
        if (!done && sd instanceof D3DWindowSurfaceData) {
            synchronized (this) {
                if (d3dwSurfaces == null) {
                    d3dwSurfaces = new ArrayList<D3DWindowSurfaceData>();
                }
                D3DWindowSurfaceData d3dw = (D3DWindowSurfaceData)sd;
                if (!d3dwSurfaces.contains(d3dw)) {
                    d3dwSurfaces.add(d3dw);
                }
            }
            startUpdateThread();
        }
    }
    @Override
    public synchronized void dropScreenSurface(SurfaceData sd) {
        if (d3dwSurfaces != null && sd instanceof D3DWindowSurfaceData) {
            D3DWindowSurfaceData d3dw = (D3DWindowSurfaceData)sd;
            removeGdiSurface(d3dw);
            d3dwSurfaces.remove(d3dw);
        }
    }
    @Override
    public SurfaceData getReplacementScreenSurface(WComponentPeer peer,
                                                   SurfaceData sd)
    {
        SurfaceData newSurface = super.getReplacementScreenSurface(peer, sd);
        trackScreenSurface(newSurface);
        return newSurface;
    }
    private void removeGdiSurface(final D3DWindowSurfaceData d3dw) {
        if (gdiSurfaces != null) {
            GDIWindowSurfaceData gdisd = gdiSurfaces.get(d3dw);
            if (gdisd != null) {
                gdisd.invalidate();
                gdiSurfaces.remove(d3dw);
            }
        }
    }
    private synchronized void startUpdateThread() {
        if (screenUpdater == null) {
            screenUpdater = (Thread)java.security.AccessController.doPrivileged(
                new java.security.PrivilegedAction() {
                    public Object run() {
                        ThreadGroup tg =
                            Thread.currentThread().getThreadGroup();
                        for (ThreadGroup tgn = tg;
                             tgn != null; tg = tgn, tgn = tg.getParent());
                        Thread t = new Thread(tg, D3DScreenUpdateManager.this,
                                              "D3D Screen Updater");
                        t.setPriority(Thread.NORM_PRIORITY + 2);
                        t.setDaemon(true);
                        return t;
                    }
            });
            screenUpdater.start();
        } else {
            wakeUpUpdateThread();
        }
    }
    public void wakeUpUpdateThread() {
        synchronized (runLock) {
            runLock.notifyAll();
        }
    }
    public void runUpdateNow() {
        synchronized (this) {
            if (done || screenUpdater == null ||
                d3dwSurfaces  == null || d3dwSurfaces.size() == 0)
            {
                return;
            }
        }
        synchronized (runLock) {
            needsUpdateNow = true;
            runLock.notifyAll();
            while (needsUpdateNow) {
                try {
                    runLock.wait();
                } catch (InterruptedException e) {}
            }
        }
    }
    public void run() {
        while (!done) {
            synchronized (runLock) {
                long timeout = d3dwSurfaces.size() > 0 ? 100 : 0;
                if (!needsUpdateNow) {
                    try { runLock.wait(timeout); }
                        catch (InterruptedException e) {}
                }
            }
            D3DWindowSurfaceData surfaces[] = new D3DWindowSurfaceData[] {};
            synchronized (this) {
                surfaces = d3dwSurfaces.toArray(surfaces);
            }
            for (D3DWindowSurfaceData sd : surfaces) {
                if (sd.isValid() && (sd.isDirty() || sd.isSurfaceLost())) {
                    if (!sd.isSurfaceLost()) {
                        D3DRenderQueue rq = D3DRenderQueue.getInstance();
                        rq.lock();
                        try {
                            Rectangle r = sd.getBounds();
                            D3DSurfaceData.swapBuffers(sd, 0, 0,
                                                       r.width, r.height);
                            sd.markClean();
                        } finally {
                            rq.unlock();
                        }
                    } else if (!validate(sd)) {
                        sd.getPeer().replaceSurfaceDataLater();
                    }
                }
            }
            synchronized (runLock) {
                needsUpdateNow = false;
                runLock.notifyAll();
            }
        }
    }
    private boolean validate(D3DWindowSurfaceData sd) {
        if (sd.isSurfaceLost()) {
            try {
                sd.restoreSurface();
                Color bg = sd.getPeer().getBackgroundNoSync();
                SunGraphics2D sg2d = new SunGraphics2D(sd, bg, bg, null);
                sg2d.fillRect(0, 0, sd.getBounds().width, sd.getBounds().height);
                sg2d.dispose();
                sd.markClean();
                repaintPeerTarget(sd.getPeer());
            } catch (InvalidPipeException ipe) {
                return false;
            }
        }
        return true;
    }
    private synchronized SurfaceData getGdiSurface(D3DWindowSurfaceData d3dw) {
        if (gdiSurfaces == null) {
            gdiSurfaces =
                new HashMap<D3DWindowSurfaceData, GDIWindowSurfaceData>();
        }
        GDIWindowSurfaceData gdisd = gdiSurfaces.get(d3dw);
        if (gdisd == null) {
            gdisd = GDIWindowSurfaceData.createData(d3dw.getPeer());
            gdiSurfaces.put(d3dw, gdisd);
        }
        return gdisd;
    }
    private static boolean hasHWChildren(Component comp) {
        if (comp instanceof Container) {
            for (Component c : ((Container)comp).getComponents()) {
                if (c.getPeer() instanceof WComponentPeer || hasHWChildren(c)) {
                    return true;
                }
            }
        }
        return false;
    }
}
