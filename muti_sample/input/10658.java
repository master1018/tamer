public abstract class WComponentPeer extends WObjectPeer
    implements ComponentPeer, DropTargetPeer
{
    protected volatile long hwnd;
    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.windows.WComponentPeer");
    private static final PlatformLogger shapeLog = PlatformLogger.getLogger("sun.awt.windows.shape.WComponentPeer");
    private static final PlatformLogger focusLog = PlatformLogger.getLogger("sun.awt.windows.focus.WComponentPeer");
    SurfaceData surfaceData;
    private RepaintArea paintArea;
    protected Win32GraphicsConfig winGraphicsConfig;
    boolean isLayouting = false;
    boolean paintPending = false;
    int     oldWidth = -1;
    int     oldHeight = -1;
    private int numBackBuffers = 0;
    private VolatileImage backBuffer = null;
    private BufferCapabilities backBufferCaps = null;
    private Color foreground;
    private Color background;
    private Font font;
    public native boolean isObscured();
    public boolean canDetermineObscurity() { return true; }
    int nDropTargets;
    long nativeDropTargetContext; 
    public synchronized native void pShow();
    public synchronized native void hide();
    public synchronized native void enable();
    public synchronized native void disable();
    public long getHWnd() {
        return hwnd;
    }
    public native Point getLocationOnScreen();
    public void setVisible(boolean b) {
        if (b) {
            show();
        } else {
            hide();
        }
    }
    public void show() {
        Dimension s = ((Component)target).getSize();
        oldHeight = s.height;
        oldWidth = s.width;
        pShow();
    }
    public void setEnabled(boolean b) {
        if (b) {
            enable();
        } else {
            disable();
        }
    }
    public int serialNum = 0;
    private native void reshapeNoCheck(int x, int y, int width, int height);
    public void setBounds(int x, int y, int width, int height, int op) {
        paintPending = (width != oldWidth) || (height != oldHeight);
        if ( (op & NO_EMBEDDED_CHECK) != 0 ) {
            reshapeNoCheck(x, y, width, height);
        } else {
            reshape(x, y, width, height);
        }
        if ((width != oldWidth) || (height != oldHeight)) {
            try {
                replaceSurfaceData();
            } catch (InvalidPipeException e) {
            }
            oldWidth = width;
            oldHeight = height;
        }
        serialNum++;
    }
    void dynamicallyLayoutContainer() {
        if (log.isLoggable(PlatformLogger.FINE)) {
            Container parent = WToolkit.getNativeContainer((Component)target);
            if (parent != null) {
                log.fine("Assertion (parent == null) failed");
            }
        }
        final Container cont = (Container)target;
        WToolkit.executeOnEventHandlerThread(cont, new Runnable() {
            public void run() {
                cont.invalidate();
                cont.validate();
                if (surfaceData instanceof D3DSurfaceData.D3DWindowSurfaceData ||
                    surfaceData instanceof OGLSurfaceData)
                {
                    try {
                        replaceSurfaceData();
                    } catch (InvalidPipeException e) {
                    }
                }
            }
        });
    }
    void paintDamagedAreaImmediately() {
        updateWindow();
        WToolkit.getWToolkit().flushPendingEvents();
        paintArea.paint(target, shouldClearRectBeforePaint());
    }
    native synchronized void updateWindow();
    public void paint(Graphics g) {
        ((Component)target).paint(g);
    }
    public void repaint(long tm, int x, int y, int width, int height) {
    }
    private static final double BANDING_DIVISOR = 4.0;
    private native int[] createPrintedPixels(int srcX, int srcY,
                                             int srcW, int srcH,
                                             int alpha);
    public void print(Graphics g) {
        Component comp = (Component)target;
        int totalW = comp.getWidth();
        int totalH = comp.getHeight();
        int hInc = (int)(totalH / BANDING_DIVISOR);
        if (hInc == 0) {
            hInc = totalH;
        }
        for (int startY = 0; startY < totalH; startY += hInc) {
            int endY = startY + hInc - 1;
            if (endY >= totalH) {
                endY = totalH - 1;
            }
            int h = endY - startY + 1;
            Color bgColor = comp.getBackground();
            int[] pix = createPrintedPixels(0, startY, totalW, h,
                                            bgColor == null ? 255 : bgColor.getAlpha());
            if (pix != null) {
                BufferedImage bim = new BufferedImage(totalW, h,
                                              BufferedImage.TYPE_INT_ARGB);
                bim.setRGB(0, 0, totalW, h, pix, 0, totalW);
                g.drawImage(bim, 0, startY, null);
                bim.flush();
            }
        }
        comp.print(g);
    }
    public void coalescePaintEvent(PaintEvent e) {
        Rectangle r = e.getUpdateRect();
        if (!(e instanceof IgnorePaintEvent)) {
            paintArea.add(r, e.getID());
        }
        if (log.isLoggable(PlatformLogger.FINEST)) {
            switch(e.getID()) {
            case PaintEvent.UPDATE:
                log.finest("coalescePaintEvent: UPDATE: add: x = " +
                    r.x + ", y = " + r.y + ", width = " + r.width + ", height = " + r.height);
                return;
            case PaintEvent.PAINT:
                log.finest("coalescePaintEvent: PAINT: add: x = " +
                    r.x + ", y = " + r.y + ", width = " + r.width + ", height = " + r.height);
                return;
            }
        }
    }
    public synchronized native void reshape(int x, int y, int width, int height);
    public boolean handleJavaKeyEvent(KeyEvent e) { return false; }
    public void handleJavaMouseEvent(MouseEvent e) {
        switch (e.getID()) {
          case MouseEvent.MOUSE_PRESSED:
              if (target == e.getSource() &&
                  !((Component)target).isFocusOwner() &&
                  WKeyboardFocusManagerPeer.shouldFocusOnClick((Component)target))
              {
                  WKeyboardFocusManagerPeer.requestFocusFor((Component)target,
                                                            CausedFocusEvent.Cause.MOUSE_EVENT);
              }
              break;
        }
    }
    native void nativeHandleEvent(AWTEvent e);
    public void handleEvent(AWTEvent e) {
        int id = e.getID();
        if ((e instanceof InputEvent) && !((InputEvent)e).isConsumed() &&
            ((Component)target).isEnabled())
        {
            if (e instanceof MouseEvent && !(e instanceof MouseWheelEvent)) {
                handleJavaMouseEvent((MouseEvent) e);
            } else if (e instanceof KeyEvent) {
                if (handleJavaKeyEvent((KeyEvent)e)) {
                    return;
                }
            }
        }
        switch(id) {
            case PaintEvent.PAINT:
                paintPending = false;
            case PaintEvent.UPDATE:
                if (!isLayouting && ! paintPending) {
                    paintArea.paint(target,shouldClearRectBeforePaint());
                }
                return;
            case FocusEvent.FOCUS_LOST:
            case FocusEvent.FOCUS_GAINED:
                handleJavaFocusEvent((FocusEvent)e);
            default:
            break;
        }
        nativeHandleEvent(e);
    }
    void handleJavaFocusEvent(FocusEvent fe) {
        if (focusLog.isLoggable(PlatformLogger.FINER)) focusLog.finer(fe.toString());
        setFocus(fe.getID() == FocusEvent.FOCUS_GAINED);
    }
    native void setFocus(boolean doSetFocus);
    public Dimension getMinimumSize() {
        return ((Component)target).getSize();
    }
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }
    public void layout() {}
    public Rectangle getBounds() {
        return ((Component)target).getBounds();
    }
    public boolean isFocusable() {
        return false;
    }
    public GraphicsConfiguration getGraphicsConfiguration() {
        if (winGraphicsConfig != null) {
            return winGraphicsConfig;
        }
        else {
            return ((Component)target).getGraphicsConfiguration();
        }
    }
    public SurfaceData getSurfaceData() {
        return surfaceData;
    }
    public void replaceSurfaceData() {
        replaceSurfaceData(this.numBackBuffers, this.backBufferCaps);
    }
    public void createScreenSurface(boolean isResize)
    {
        Win32GraphicsConfig gc = (Win32GraphicsConfig)getGraphicsConfiguration();
        ScreenUpdateManager mgr = ScreenUpdateManager.getInstance();
        surfaceData = mgr.createScreenSurface(gc, this, numBackBuffers, isResize);
    }
    public void replaceSurfaceData(int newNumBackBuffers,
                                   BufferCapabilities caps)
    {
        SurfaceData oldData = null;
        VolatileImage oldBB = null;
        synchronized(((Component)target).getTreeLock()) {
            synchronized(this) {
                if (pData == 0) {
                    return;
                }
                numBackBuffers = newNumBackBuffers;
                ScreenUpdateManager mgr = ScreenUpdateManager.getInstance();
                oldData = surfaceData;
                mgr.dropScreenSurface(oldData);
                createScreenSurface(true);
                if (oldData != null) {
                    oldData.invalidate();
                }
                oldBB = backBuffer;
                if (numBackBuffers > 0) {
                    backBufferCaps = caps;
                    Win32GraphicsConfig gc =
                        (Win32GraphicsConfig)getGraphicsConfiguration();
                    backBuffer = gc.createBackBuffer(this);
                } else if (backBuffer != null) {
                    backBufferCaps = null;
                    backBuffer = null;
                }
            }
        }
        if (oldData != null) {
            oldData.flush();
            oldData = null;
        }
        if (oldBB != null) {
            oldBB.flush();
            oldData = null;
        }
    }
    public void replaceSurfaceDataLater() {
        Runnable r = new Runnable() {
            public void run() {
                if (!isDisposed()) {
                    try {
                        replaceSurfaceData();
                    } catch (InvalidPipeException e) {
                    }
                }
            }
        };
        if (!PaintEventDispatcher.getPaintEventDispatcher().queueSurfaceDataReplacing((Component)target, r)) {
            postEvent(new InvocationEvent(Toolkit.getDefaultToolkit(), r));
        }
    }
    public boolean updateGraphicsData(GraphicsConfiguration gc) {
        winGraphicsConfig = (Win32GraphicsConfig)gc;
        try {
            replaceSurfaceData();
        } catch (InvalidPipeException e) {
        }
        return false;
    }
    public ColorModel getColorModel() {
        GraphicsConfiguration gc = getGraphicsConfiguration();
        if (gc != null) {
            return gc.getColorModel();
        }
        else {
            return null;
        }
    }
    public ColorModel getDeviceColorModel() {
        Win32GraphicsConfig gc =
            (Win32GraphicsConfig)getGraphicsConfiguration();
        if (gc != null) {
            return gc.getDeviceColorModel();
        }
        else {
            return null;
        }
    }
    public ColorModel getColorModel(int transparency) {
        GraphicsConfiguration gc = getGraphicsConfiguration();
        if (gc != null) {
            return gc.getColorModel(transparency);
        }
        else {
            return null;
        }
    }
    public java.awt.Toolkit getToolkit() {
        return Toolkit.getDefaultToolkit();
    }
    final static Font defaultFont = new Font(Font.DIALOG, Font.PLAIN, 12);
    public Graphics getGraphics() {
        if (isDisposed()) {
            return null;
        }
        Component target = (Component)getTarget();
        Window window = SunToolkit.getContainingWindow(target);
        if (window != null) {
            Graphics g =
                ((WWindowPeer)window.getPeer()).getTranslucentGraphics();
            if (g != null) {
                int x = 0, y = 0;
                for (Component c = target; c != window; c = c.getParent()) {
                    x += c.getX();
                    y += c.getY();
                }
                g.translate(x, y);
                g.clipRect(0, 0, target.getWidth(), target.getHeight());
                return g;
            }
        }
        SurfaceData surfaceData = this.surfaceData;
        if (surfaceData != null) {
            Color bgColor = background;
            if (bgColor == null) {
                bgColor = SystemColor.window;
            }
            Color fgColor = foreground;
            if (fgColor == null) {
                fgColor = SystemColor.windowText;
            }
            Font font = this.font;
            if (font == null) {
                font = defaultFont;
            }
            ScreenUpdateManager mgr =
                ScreenUpdateManager.getInstance();
            return mgr.createGraphics(surfaceData, this, fgColor,
                                      bgColor, font);
        }
        return null;
    }
    public FontMetrics getFontMetrics(Font font) {
        return WFontMetrics.getFontMetrics(font);
    }
    private synchronized native void _dispose();
    protected void disposeImpl() {
        SurfaceData oldData = surfaceData;
        surfaceData = null;
        ScreenUpdateManager.getInstance().dropScreenSurface(oldData);
        oldData.invalidate();
        WToolkit.targetDisposedPeer(target, this);
        _dispose();
    }
    public synchronized void setForeground(Color c) {
        foreground = c;
        _setForeground(c.getRGB());
    }
    public synchronized void setBackground(Color c) {
        background = c;
        _setBackground(c.getRGB());
    }
    public Color getBackgroundNoSync() {
        return background;
    }
    public native void _setForeground(int rgb);
    public native void _setBackground(int rgb);
    public synchronized void setFont(Font f) {
        font = f;
        _setFont(f);
    }
    public synchronized native void _setFont(Font f);
    public final void updateCursorImmediately() {
        WGlobalCursorManager.getCursorManager().updateCursorImmediately();
    }
    public boolean requestFocus(Component lightweightChild, boolean temporary,
                                boolean focusedWindowChangeAllowed, long time,
                                CausedFocusEvent.Cause cause)
    {
        if (WKeyboardFocusManagerPeer.
            processSynchronousLightweightTransfer((Component)target, lightweightChild, temporary,
                                                  focusedWindowChangeAllowed, time))
        {
            return true;
        }
        int result = WKeyboardFocusManagerPeer
            .shouldNativelyFocusHeavyweight((Component)target, lightweightChild,
                                            temporary, focusedWindowChangeAllowed,
                                            time, cause);
        switch (result) {
          case WKeyboardFocusManagerPeer.SNFH_FAILURE:
              return false;
          case WKeyboardFocusManagerPeer.SNFH_SUCCESS_PROCEED:
              if (focusLog.isLoggable(PlatformLogger.FINER)) {
                  focusLog.finer("Proceeding with request to " + lightweightChild + " in " + target);
              }
              Window parentWindow = SunToolkit.getContainingWindow((Component)target);
              if (parentWindow == null) {
                  return rejectFocusRequestHelper("WARNING: Parent window is null");
              }
              WWindowPeer wpeer = (WWindowPeer)parentWindow.getPeer();
              if (wpeer == null) {
                  return rejectFocusRequestHelper("WARNING: Parent window's peer is null");
              }
              boolean res = wpeer.requestWindowFocus(cause);
              if (focusLog.isLoggable(PlatformLogger.FINER)) focusLog.finer("Requested window focus: " + res);
              if (!(res && parentWindow.isFocused())) {
                  return rejectFocusRequestHelper("Waiting for asynchronous processing of the request");
              }
              return WKeyboardFocusManagerPeer.deliverFocus(lightweightChild,
                                                            (Component)target,
                                                            temporary,
                                                            focusedWindowChangeAllowed,
                                                            time, cause);
          case WKeyboardFocusManagerPeer.SNFH_SUCCESS_HANDLED:
              return true;
        }
        return false;
    }
    private boolean rejectFocusRequestHelper(String logMsg) {
        if (focusLog.isLoggable(PlatformLogger.FINER)) focusLog.finer(logMsg);
        WKeyboardFocusManagerPeer.removeLastFocusRequest((Component)target);
        return false;
    }
    public Image createImage(ImageProducer producer) {
        return new ToolkitImage(producer);
    }
    public Image createImage(int width, int height) {
        Win32GraphicsConfig gc =
            (Win32GraphicsConfig)getGraphicsConfiguration();
        return gc.createAcceleratedImage((Component)target, width, height);
    }
    public VolatileImage createVolatileImage(int width, int height) {
        return new SunVolatileImage((Component)target, width, height);
    }
    public boolean prepareImage(Image img, int w, int h, ImageObserver o) {
        return getToolkit().prepareImage(img, w, h, o);
    }
    public int checkImage(Image img, int w, int h, ImageObserver o) {
        return getToolkit().checkImage(img, w, h, o);
    }
    public String toString() {
        return getClass().getName() + "[" + target + "]";
    }
    private int updateX1, updateY1, updateX2, updateY2;
    WComponentPeer(Component target) {
        this.target = target;
        this.paintArea = new RepaintArea();
        Container parent = WToolkit.getNativeContainer(target);
        WComponentPeer parentPeer = (WComponentPeer) WToolkit.targetToPeer(parent);
        create(parentPeer);
        checkCreation();
        createScreenSurface(false);
        initialize();
        start();  
    }
    abstract void create(WComponentPeer parent);
    protected void checkCreation()
    {
        if ((hwnd == 0) || (pData == 0))
        {
            if (createError != null)
            {
                throw createError;
            }
            else
            {
                throw new InternalError("couldn't create component peer");
            }
        }
    }
    synchronized native void start();
    void initialize() {
        if (((Component)target).isVisible()) {
            show();  
        }
        Color fg = ((Component)target).getForeground();
        if (fg != null) {
            setForeground(fg);
        }
        Font  f = ((Component)target).getFont();
        if (f != null) {
            setFont(f);
        }
        if (! ((Component)target).isEnabled()) {
            disable();
        }
        Rectangle r = ((Component)target).getBounds();
        setBounds(r.x, r.y, r.width, r.height, SET_BOUNDS);
    }
    void handleRepaint(int x, int y, int w, int h) {
    }
    void handleExpose(int x, int y, int w, int h) {
        postPaintIfNecessary(x, y, w, h);
    }
    public void handlePaint(int x, int y, int w, int h) {
        postPaintIfNecessary(x, y, w, h);
    }
    private void postPaintIfNecessary(int x, int y, int w, int h) {
        if ( !AWTAccessor.getComponentAccessor().getIgnoreRepaint( (Component) target) ) {
            PaintEvent event = PaintEventDispatcher.getPaintEventDispatcher().
                createPaintEvent((Component)target, x, y, w, h);
            if (event != null) {
                postEvent(event);
            }
        }
    }
    void postEvent(AWTEvent event) {
        preprocessPostEvent(event);
        WToolkit.postEvent(WToolkit.targetToAppContext(target), event);
    }
    void preprocessPostEvent(AWTEvent event) {}
    public void beginLayout() {
        isLayouting = true;
    }
    public void endLayout() {
        if(!paintArea.isEmpty() && !paintPending &&
            !((Component)target).getIgnoreRepaint()) {
            postEvent(new PaintEvent((Component)target, PaintEvent.PAINT,
                          new Rectangle()));
        }
        isLayouting = false;
    }
    public native void beginValidate();
    public native void endValidate();
    public Dimension minimumSize() {
        return getMinimumSize();
    }
    public Dimension preferredSize() {
        return getPreferredSize();
    }
    public synchronized void addDropTarget(DropTarget dt) {
        if (nDropTargets == 0) {
            nativeDropTargetContext = addNativeDropTarget();
        }
        nDropTargets++;
    }
    public synchronized void removeDropTarget(DropTarget dt) {
        nDropTargets--;
        if (nDropTargets == 0) {
            removeNativeDropTarget();
            nativeDropTargetContext = 0;
        }
    }
    native long addNativeDropTarget();
    native void removeNativeDropTarget();
    native boolean nativeHandlesWheelScrolling();
    public boolean handlesWheelScrolling() {
        return nativeHandlesWheelScrolling();
    }
    public boolean isPaintPending() {
        return paintPending && isLayouting;
    }
    @Override
    public void createBuffers(int numBuffers, BufferCapabilities caps)
        throws AWTException
    {
        Win32GraphicsConfig gc =
            (Win32GraphicsConfig)getGraphicsConfiguration();
        gc.assertOperationSupported((Component)target, numBuffers, caps);
        try {
            replaceSurfaceData(numBuffers - 1, caps);
        } catch (InvalidPipeException e) {
            throw new AWTException(e.getMessage());
        }
    }
    @Override
    public void destroyBuffers() {
        replaceSurfaceData(0, null);
    }
    @Override
    public void flip(int x1, int y1, int x2, int y2,
                                  BufferCapabilities.FlipContents flipAction)
    {
        VolatileImage backBuffer = this.backBuffer;
        if (backBuffer == null) {
            throw new IllegalStateException("Buffers have not been created");
        }
        Win32GraphicsConfig gc =
            (Win32GraphicsConfig)getGraphicsConfiguration();
        gc.flip(this, (Component)target, backBuffer, x1, y1, x2, y2, flipAction);
    }
    @Override
    public synchronized Image getBackBuffer() {
        Image backBuffer = this.backBuffer;
        if (backBuffer == null) {
            throw new IllegalStateException("Buffers have not been created");
        }
        return backBuffer;
    }
    public BufferCapabilities getBackBufferCaps() {
        return backBufferCaps;
    }
    public int getBackBuffersNum() {
        return numBackBuffers;
    }
    public boolean shouldClearRectBeforePaint() {
        return true;
    }
    native void pSetParent(ComponentPeer newNativeParent);
    public void reparent(ContainerPeer newNativeParent) {
        pSetParent(newNativeParent);
    }
    public boolean isReparentSupported() {
        return true;
    }
    public void setBoundsOperation(int operation) {
    }
    private volatile boolean isAccelCapable = true;
    public boolean isAccelCapable() {
        if (!isAccelCapable ||
            !isContainingTopLevelAccelCapable((Component)target))
        {
            return false;
        }
        boolean isTranslucent =
            SunToolkit.isContainingTopLevelTranslucent((Component)target);
        return !isTranslucent || Win32GraphicsEnvironment.isVistaOS();
    }
    public void disableAcceleration() {
        isAccelCapable = false;
    }
    native void setRectangularShape(int lox, int loy, int hix, int hiy,
                     Region region);
    private static final boolean isContainingTopLevelAccelCapable(Component c) {
        while (c != null && !(c instanceof WEmbeddedFrame)) {
            c = c.getParent();
        }
        if (c == null) {
            return true;
        }
        return ((WEmbeddedFramePeer)c.getPeer()).isAccelCapable();
    }
    public void applyShape(Region shape) {
        if (shapeLog.isLoggable(PlatformLogger.FINER)) {
            shapeLog.finer(
                    "*** INFO: Setting shape: PEER: " + this
                    + "; TARGET: " + target
                    + "; SHAPE: " + shape);
        }
        if (shape != null) {
            setRectangularShape(shape.getLoX(), shape.getLoY(), shape.getHiX(), shape.getHiY(),
                    (shape.isRectangular() ? null : shape));
        } else {
            setRectangularShape(0, 0, 0, 0, null);
        }
    }
    public void setZOrder(ComponentPeer above) {
        long aboveHWND = (above != null) ? ((WComponentPeer)above).getHWnd() : 0;
        setZOrder(aboveHWND);
    }
    private native void setZOrder(long above);
}
