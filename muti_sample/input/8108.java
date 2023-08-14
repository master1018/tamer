public class XBaseWindow {
    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.XBaseWindow");
    private static final PlatformLogger insLog = PlatformLogger.getLogger("sun.awt.X11.insets.XBaseWindow");
    private static final PlatformLogger eventLog = PlatformLogger.getLogger("sun.awt.X11.event.XBaseWindow");
    private static final PlatformLogger focusLog = PlatformLogger.getLogger("sun.awt.X11.focus.XBaseWindow");
    private static final PlatformLogger grabLog = PlatformLogger.getLogger("sun.awt.X11.grab.XBaseWindow");
    public static final String
        PARENT_WINDOW = "parent window", 
        BOUNDS = "bounds", 
        OVERRIDE_REDIRECT = "overrideRedirect", 
        EVENT_MASK = "event mask", 
        VALUE_MASK = "value mask", 
        BORDER_PIXEL = "border pixel", 
        COLORMAP = "color map", 
        DEPTH = "visual depth", 
        VISUAL_CLASS = "visual class", 
        VISUAL = "visual", 
        EMBEDDED = "embedded", 
        DELAYED = "delayed", 
        PARENT = "parent", 
        BACKGROUND_PIXMAP = "pixmap", 
        VISIBLE = "visible", 
        SAVE_UNDER = "save under", 
        BACKING_STORE = "backing store", 
        BIT_GRAVITY = "bit gravity"; 
    private XCreateWindowParams delayedParams;
    Set<Long> children = new HashSet<Long>();
    long window;
    boolean visible;
    boolean mapped;
    boolean embedded;
    Rectangle maxBounds;
    volatile XBaseWindow parentWindow;
    private boolean disposed;
    private long screen;
    private XSizeHints hints;
    private XWMHints wmHints;
    final static int MIN_SIZE = 1;
    final static int DEF_LOCATION = 1;
    private static XAtom wm_client_leader;
    static enum InitialiseState {
        INITIALISING,
        NOT_INITIALISED,
        INITIALISED,
        FAILED_INITIALISATION
    };
    private InitialiseState initialising;
    int x;
    int y;
    int width;
    int height;
    void awtLock() {
        XToolkit.awtLock();
    }
    void awtUnlock() {
        XToolkit.awtUnlock();
    }
    void awtLockNotifyAll() {
        XToolkit.awtLockNotifyAll();
    }
    void awtLockWait() throws InterruptedException {
        XToolkit.awtLockWait();
    }
    protected final void init(long parentWindow, Rectangle bounds) {}
    protected final void preInit() {}
    protected final void postInit() {}
    static class StateLock extends Object { }
    protected StateLock state_lock;
    void instantPreInit(XCreateWindowParams params) {
        state_lock = new StateLock();
        initialising = InitialiseState.NOT_INITIALISED;
    }
    void preInit(XCreateWindowParams params) {
        state_lock = new StateLock();
        initialising = InitialiseState.NOT_INITIALISED;
        embedded = Boolean.TRUE.equals(params.get(EMBEDDED));
        visible = Boolean.TRUE.equals(params.get(VISIBLE));
        Object parent = params.get(PARENT);
        if (parent instanceof XBaseWindow) {
            parentWindow = (XBaseWindow)parent;
        } else {
            Long parentWindowID = (Long)params.get(PARENT_WINDOW);
            if (parentWindowID != null) {
                parentWindow = XToolkit.windowToXWindow(parentWindowID);
            }
        }
        Long eventMask = (Long)params.get(EVENT_MASK);
        if (eventMask != null) {
            long mask = eventMask.longValue();
            mask |= XConstants.SubstructureNotifyMask;
            params.put(EVENT_MASK, mask);
        }
        screen = -1;
    }
    void postInit(XCreateWindowParams params) {
        if (log.isLoggable(PlatformLogger.FINE)) log.fine("WM name is " + getWMName());
        updateWMName();
        initClientLeader();
    }
    protected final void init(XCreateWindowParams params) {
        awtLock();
        initialising = InitialiseState.INITIALISING;
        awtUnlock();
        try {
            if (!Boolean.TRUE.equals(params.get(DELAYED))) {
                preInit(params);
                create(params);
                postInit(params);
            } else {
                instantPreInit(params);
                delayedParams = params;
            }
            awtLock();
            initialising = InitialiseState.INITIALISED;
            awtLockNotifyAll();
            awtUnlock();
        } catch (RuntimeException re) {
            awtLock();
            initialising = InitialiseState.FAILED_INITIALISATION;
            awtLockNotifyAll();
            awtUnlock();
            throw re;
        } catch (Throwable t) {
            log.warning("Exception during peer initialization", t);
            awtLock();
            initialising = InitialiseState.FAILED_INITIALISATION;
            awtLockNotifyAll();
            awtUnlock();
        }
    }
    public boolean checkInitialised() {
        awtLock();
        try {
            switch (initialising) {
              case INITIALISED:
                  return true;
              case INITIALISING:
                  try {
                      while (initialising != InitialiseState.INITIALISED) {
                          awtLockWait();
                      }
                  } catch (InterruptedException ie) {
                      return false;
                  }
                  return true;
              case NOT_INITIALISED:
              case FAILED_INITIALISATION:
                  return false;
              default:
                  return false;
            }
        } finally {
            awtUnlock();
        }
    }
    XBaseWindow() {
        this(new XCreateWindowParams());
    }
    XBaseWindow(long parentWindow, Rectangle bounds) {
        this(new XCreateWindowParams(new Object[] {
            BOUNDS, bounds,
            PARENT_WINDOW, Long.valueOf(parentWindow)}));
    }
    XBaseWindow(Rectangle bounds) {
        this(new XCreateWindowParams(new Object[] {
            BOUNDS, bounds
        }));
    }
    public XBaseWindow (XCreateWindowParams params) {
        init(params);
    }
    XBaseWindow(long parentWindow) {
        this(new XCreateWindowParams(new Object[] {
            PARENT_WINDOW, Long.valueOf(parentWindow),
            EMBEDDED, Boolean.TRUE
        }));
    }
    protected void checkParams(XCreateWindowParams params) {
        if (params == null) {
            throw new IllegalArgumentException("Window creation parameters are null");
        }
        params.putIfNull(PARENT_WINDOW, Long.valueOf(XToolkit.getDefaultRootWindow()));
        params.putIfNull(BOUNDS, new Rectangle(DEF_LOCATION, DEF_LOCATION, MIN_SIZE, MIN_SIZE));
        params.putIfNull(DEPTH, Integer.valueOf((int)XConstants.CopyFromParent));
        params.putIfNull(VISUAL, Long.valueOf(XConstants.CopyFromParent));
        params.putIfNull(VISUAL_CLASS, Integer.valueOf((int)XConstants.InputOnly));
        params.putIfNull(VALUE_MASK, Long.valueOf(XConstants.CWEventMask));
        Rectangle bounds = (Rectangle)params.get(BOUNDS);
        bounds.width = Math.max(MIN_SIZE, bounds.width);
        bounds.height = Math.max(MIN_SIZE, bounds.height);
        Long eventMaskObj = (Long)params.get(EVENT_MASK);
        long eventMask = eventMaskObj != null ? eventMaskObj.longValue() : 0;
        eventMask |= XConstants.PropertyChangeMask | XConstants.OwnerGrabButtonMask;
        params.put(EVENT_MASK, Long.valueOf(eventMask));
    }
    private final void create(XCreateWindowParams params) {
        XToolkit.awtLock();
        try {
            XSetWindowAttributes xattr = new XSetWindowAttributes();
            try {
                checkParams(params);
                long value_mask = ((Long)params.get(VALUE_MASK)).longValue();
                Long eventMask = (Long)params.get(EVENT_MASK);
                xattr.set_event_mask(eventMask.longValue());
                value_mask |= XConstants.CWEventMask;
                Long border_pixel = (Long)params.get(BORDER_PIXEL);
                if (border_pixel != null) {
                    xattr.set_border_pixel(border_pixel.longValue());
                    value_mask |= XConstants.CWBorderPixel;
                }
                Long colormap = (Long)params.get(COLORMAP);
                if (colormap != null) {
                    xattr.set_colormap(colormap.longValue());
                    value_mask |= XConstants.CWColormap;
                }
                Long background_pixmap = (Long)params.get(BACKGROUND_PIXMAP);
                if (background_pixmap != null) {
                    xattr.set_background_pixmap(background_pixmap.longValue());
                    value_mask |= XConstants.CWBackPixmap;
                }
                Long parentWindow = (Long)params.get(PARENT_WINDOW);
                Rectangle bounds = (Rectangle)params.get(BOUNDS);
                Integer depth = (Integer)params.get(DEPTH);
                Integer visual_class = (Integer)params.get(VISUAL_CLASS);
                Long visual = (Long)params.get(VISUAL);
                Boolean overrideRedirect = (Boolean)params.get(OVERRIDE_REDIRECT);
                if (overrideRedirect != null) {
                    xattr.set_override_redirect(overrideRedirect.booleanValue());
                    value_mask |= XConstants.CWOverrideRedirect;
                }
                Boolean saveUnder = (Boolean)params.get(SAVE_UNDER);
                if (saveUnder != null) {
                    xattr.set_save_under(saveUnder.booleanValue());
                    value_mask |= XConstants.CWSaveUnder;
                }
                Integer backingStore = (Integer)params.get(BACKING_STORE);
                if (backingStore != null) {
                    xattr.set_backing_store(backingStore.intValue());
                    value_mask |= XConstants.CWBackingStore;
                }
                Integer bitGravity = (Integer)params.get(BIT_GRAVITY);
                if (bitGravity != null) {
                    xattr.set_bit_gravity(bitGravity.intValue());
                    value_mask |= XConstants.CWBitGravity;
                }
                if (log.isLoggable(PlatformLogger.FINE)) {
                    log.fine("Creating window for " + this + " with the following attributes: \n" + params);
                }
                window = XlibWrapper.XCreateWindow(XToolkit.getDisplay(),
                                   parentWindow.longValue(),
                                   bounds.x, bounds.y, 
                                   bounds.width, bounds.height, 
                                   0, 
                                   depth.intValue(), 
                                   visual_class.intValue(), 
                                   visual.longValue(), 
                                   value_mask,  
                                   xattr.pData); 
                if (window == 0) {
                    throw new IllegalStateException("Couldn't create window because of wrong parameters. Run with NOISY_AWT to see details");
                }
                XToolkit.addToWinMap(window, this);
            } finally {
                xattr.dispose();
            }
        } finally {
            XToolkit.awtUnlock();
        }
    }
    public XCreateWindowParams getDelayedParams() {
        return delayedParams;
    }
    protected String getWMName() {
        return XToolkit.getCorrectXIDString(getClass().getName());
    }
    protected void initClientLeader() {
        XToolkit.awtLock();
        try {
            if (wm_client_leader == null) {
                wm_client_leader = XAtom.get("WM_CLIENT_LEADER");
            }
            wm_client_leader.setWindowProperty(this, getXAWTRootWindow());
        } finally {
            XToolkit.awtUnlock();
        }
    }
    static XRootWindow getXAWTRootWindow() {
        return XRootWindow.getInstance();
    }
    void destroy() {
        XToolkit.awtLock();
        try {
            if (hints != null) {
                XlibWrapper.XFree(hints.pData);
                hints = null;
            }
            XToolkit.removeFromWinMap(getWindow(), this);
            XlibWrapper.XDestroyWindow(XToolkit.getDisplay(), getWindow());
            if (XPropertyCache.isCachingSupported()) {
                XPropertyCache.clearCache(window);
            }
            window = -1;
            if( !isDisposed() ) {
                setDisposed( true );
            }
            XAwtState.getGrabWindow(); 
        } finally {
            XToolkit.awtUnlock();
        }
    }
    void flush() {
        XToolkit.awtLock();
        try {
            XlibWrapper.XFlush(XToolkit.getDisplay());
        } finally {
            XToolkit.awtUnlock();
        }
    }
    public final void setWMHints(XWMHints hints) {
        XToolkit.awtLock();
        try {
            XlibWrapper.XSetWMHints(XToolkit.getDisplay(), getWindow(), hints.pData);
        } finally {
            XToolkit.awtUnlock();
        }
    }
    public XWMHints getWMHints() {
        if (wmHints == null) {
            wmHints = new XWMHints(XlibWrapper.XAllocWMHints());
        }
        return wmHints;
    }
    public XSizeHints getHints() {
        if (hints == null) {
            long p_hints = XlibWrapper.XAllocSizeHints();
            hints = new XSizeHints(p_hints);
        }
        return hints;
    }
    public void setSizeHints(long flags, int x, int y, int width, int height) {
        if (insLog.isLoggable(PlatformLogger.FINER)) insLog.finer("Setting hints, flags " + XlibWrapper.hintsToString(flags));
        XToolkit.awtLock();
        try {
            XSizeHints hints = getHints();
            if ((flags & XUtilConstants.PPosition) != 0) {
                hints.set_x(x);
                hints.set_y(y);
            }
            if ((flags & XUtilConstants.PSize) != 0) {
                hints.set_width(width);
                hints.set_height(height);
            } else if ((hints.get_flags() & XUtilConstants.PSize) != 0) {
                flags |= XUtilConstants.PSize;
            }
            if ((flags & XUtilConstants.PMinSize) != 0) {
                hints.set_min_width(width);
                hints.set_min_height(height);
            } else if ((hints.get_flags() & XUtilConstants.PMinSize) != 0) {
                flags |= XUtilConstants.PMinSize;
            }
            if ((flags & XUtilConstants.PMaxSize) != 0) {
                if (maxBounds != null) {
                    if (maxBounds.width != Integer.MAX_VALUE) {
                        hints.set_max_width(maxBounds.width);
                    } else {
                        hints.set_max_width(XToolkit.getDefaultScreenWidth());
                    }
                    if (maxBounds.height != Integer.MAX_VALUE) {
                        hints.set_max_height(maxBounds.height);
                    } else {
                        hints.set_max_height(XToolkit.getDefaultScreenHeight());
                    }
                } else {
                    hints.set_max_width(width);
                    hints.set_max_height(height);
                }
            } else if ((hints.get_flags() & XUtilConstants.PMaxSize) != 0) {
                flags |= XUtilConstants.PMaxSize;
                if (maxBounds != null) {
                    if (maxBounds.width != Integer.MAX_VALUE) {
                        hints.set_max_width(maxBounds.width);
                    } else {
                        hints.set_max_width(XToolkit.getDefaultScreenWidth());
                    }
                    if (maxBounds.height != Integer.MAX_VALUE) {
                        hints.set_max_height(maxBounds.height);
                    } else {
                        hints.set_max_height(XToolkit.getDefaultScreenHeight());
                    }
                } else {
                }
            }
            flags |= XUtilConstants.PWinGravity;
            hints.set_flags(flags);
            hints.set_win_gravity((int)XConstants.NorthWestGravity);
            if (insLog.isLoggable(PlatformLogger.FINER)) insLog.finer("Setting hints, resulted flags " + XlibWrapper.hintsToString(flags) +
                                                             ", values " + hints);
            XlibWrapper.XSetWMNormalHints(XToolkit.getDisplay(), getWindow(), hints.pData);
        } finally {
            XToolkit.awtUnlock();
        }
    }
    public boolean isMinSizeSet() {
        XSizeHints hints = getHints();
        long flags = hints.get_flags();
        return ((flags & XUtilConstants.PMinSize) == XUtilConstants.PMinSize);
    }
    Object getStateLock() {
        return state_lock;
    }
    public long getWindow() {
        return window;
    }
    public long getContentWindow() {
        return window;
    }
    public XBaseWindow getContentXWindow() {
        return XToolkit.windowToXWindow(getContentWindow());
    }
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }
    public Dimension getSize() {
        return new Dimension(width, height);
    }
    public void toFront() {
        XToolkit.awtLock();
        try {
            XlibWrapper.XRaiseWindow(XToolkit.getDisplay(), getWindow());
        } finally {
            XToolkit.awtUnlock();
        }
    }
    public void xRequestFocus(long time) {
        XToolkit.awtLock();
        try {
            if (focusLog.isLoggable(PlatformLogger.FINER)) focusLog.finer("XSetInputFocus on " + Long.toHexString(getWindow()) + " with time " + time);
            XlibWrapper.XSetInputFocus2(XToolkit.getDisplay(), getWindow(), time);
        } finally {
            XToolkit.awtUnlock();
        }
    }
    public void xRequestFocus() {
        XToolkit.awtLock();
        try {
            if (focusLog.isLoggable(PlatformLogger.FINER)) focusLog.finer("XSetInputFocus on " + Long.toHexString(getWindow()));
             XlibWrapper.XSetInputFocus(XToolkit.getDisplay(), getWindow());
        } finally {
            XToolkit.awtUnlock();
        }
    }
    public static long xGetInputFocus() {
        XToolkit.awtLock();
        try {
            return XlibWrapper.XGetInputFocus(XToolkit.getDisplay());
        } finally {
            XToolkit.awtUnlock();
        }
    }
    public void xSetVisible(boolean visible) {
        if (log.isLoggable(PlatformLogger.FINE)) log.fine("Setting visible on " + this + " to " + visible);
        XToolkit.awtLock();
        try {
            this.visible = visible;
            if (visible) {
                XlibWrapper.XMapWindow(XToolkit.getDisplay(), getWindow());
            }
            else {
                XlibWrapper.XUnmapWindow(XToolkit.getDisplay(), getWindow());
            }
            XlibWrapper.XFlush(XToolkit.getDisplay());
        } finally {
            XToolkit.awtUnlock();
        }
    }
    boolean isMapped() {
        return mapped;
    }
    void updateWMName() {
        String name = getWMName();
        XToolkit.awtLock();
        try {
            if (name == null) {
                name = " ";
            }
            XAtom nameAtom = XAtom.get(XAtom.XA_WM_NAME);
            nameAtom.setProperty(getWindow(), name);
            XAtom netNameAtom = XAtom.get("_NET_WM_NAME");
            netNameAtom.setPropertyUTF8(getWindow(), name);
        } finally {
            XToolkit.awtUnlock();
        }
    }
    void setWMClass(String[] cl) {
        if (cl.length != 2) {
            throw new IllegalArgumentException("WM_CLASS_NAME consists of exactly two strings");
        }
        XToolkit.awtLock();
        try {
            XAtom xa = XAtom.get(XAtom.XA_WM_CLASS);
            xa.setProperty8(getWindow(), cl[0] + '\0' + cl[1]);
        } finally {
            XToolkit.awtUnlock();
        }
    }
    boolean isVisible() {
        return visible;
    }
    static long getScreenOfWindow(long window) {
        XToolkit.awtLock();
        try {
            return XlibWrapper.getScreenOfWindow(XToolkit.getDisplay(), window);
        } finally {
            XToolkit.awtUnlock();
        }
    }
    long getScreenNumber() {
        XToolkit.awtLock();
        try {
            return XlibWrapper.XScreenNumberOfScreen(getScreen());
        } finally {
            XToolkit.awtUnlock();
        }
    }
    long getScreen() {
        if (screen == -1) { 
            screen = getScreenOfWindow(window);
        }
        return screen;
    }
    public void xSetBounds(Rectangle bounds) {
        xSetBounds(bounds.x, bounds.y, bounds.width, bounds.height);
    }
    public void xSetBounds(int x, int y, int width, int height) {
        if (getWindow() == 0) {
            insLog.warning("Attempt to resize uncreated window");
            throw new IllegalStateException("Attempt to resize uncreated window");
        }
        insLog.fine("Setting bounds on " + this + " to (" + x + ", " + y + "), " + width + "x" + height);
        width = Math.max(MIN_SIZE, width);
        height = Math.max(MIN_SIZE, height);
        XToolkit.awtLock();
        try {
             XlibWrapper.XMoveResizeWindow(XToolkit.getDisplay(), getWindow(), x,y,width,height);
        } finally {
            XToolkit.awtUnlock();
        }
    }
    static Point toOtherWindow(long src, long dst, int x, int y) {
        Point rpt = new Point(0, 0);
        XBaseWindow srcPeer = XToolkit.windowToXWindow(src);
        XBaseWindow dstPeer = XToolkit.windowToXWindow(dst);
        if (srcPeer != null && dstPeer != null) {
            rpt.x = x + srcPeer.getAbsoluteX() - dstPeer.getAbsoluteX();
            rpt.y = y + srcPeer.getAbsoluteY() - dstPeer.getAbsoluteY();
        } else if (dstPeer != null && XlibUtil.isRoot(src, dstPeer.getScreenNumber())) {
            rpt.x = x - dstPeer.getAbsoluteX();
            rpt.y = y - dstPeer.getAbsoluteY();
        } else if (srcPeer != null && XlibUtil.isRoot(dst, srcPeer.getScreenNumber())) {
            rpt.x = x + srcPeer.getAbsoluteX();
            rpt.y = y + srcPeer.getAbsoluteY();
        } else {
            rpt = XlibUtil.translateCoordinates(src, dst, new Point(x, y));
        }
        return rpt;
    }
    Rectangle toGlobal(Rectangle rec) {
        Point p = toGlobal(rec.getLocation());
        Rectangle newRec = new Rectangle(rec);
        if (p != null) {
            newRec.setLocation(p);
        }
        return newRec;
    }
    Point toGlobal(Point pt) {
        Point p = toGlobal(pt.x, pt.y);
        if (p != null) {
            return p;
        } else {
            return new Point(pt);
        }
    }
    Point toGlobal(int x, int y) {
        long root;
        XToolkit.awtLock();
        try {
            root = XlibWrapper.RootWindow(XToolkit.getDisplay(),
                    getScreenNumber());
        } finally {
            XToolkit.awtUnlock();
        }
        Point p = toOtherWindow(getContentWindow(), root, x, y);
        if (p != null) {
            return p;
        } else {
            return new Point(x, y);
        }
    }
    Point toLocal(Point pt) {
        Point p = toLocal(pt.x, pt.y);
        if (p != null) {
            return p;
        } else {
            return new Point(pt);
        }
    }
    Point toLocal(int x, int y) {
        long root;
        XToolkit.awtLock();
        try {
            root = XlibWrapper.RootWindow(XToolkit.getDisplay(),
                    getScreenNumber());
        } finally {
            XToolkit.awtUnlock();
        }
        Point p = toOtherWindow(root, getContentWindow(), x, y);
        if (p != null) {
            return p;
        } else {
            return new Point(x, y);
        }
    }
    public boolean grabInput() {
        grabLog.fine("Grab input on {0}", this);
        XToolkit.awtLock();
        try {
            if (XAwtState.getGrabWindow() == this &&
                XAwtState.isManualGrab())
            {
                grabLog.fine("    Already Grabbed");
                return true;
            }
            XBaseWindow prevGrabWindow = XAwtState.getGrabWindow();
            final int eventMask = (int) (XConstants.ButtonPressMask | XConstants.ButtonReleaseMask
                | XConstants.EnterWindowMask | XConstants.LeaveWindowMask | XConstants.PointerMotionMask
                | XConstants.ButtonMotionMask);
            final int ownerEvents = 1;
            if (!XToolkit.getSunAwtDisableGrab()) {
                int ptrGrab = XlibWrapper.XGrabPointer(XToolkit.getDisplay(),
                        getContentWindow(), ownerEvents, eventMask, XConstants.GrabModeAsync,
                        XConstants.GrabModeAsync, XConstants.None, (XWM.isMotif() ? XToolkit.arrowCursor : XConstants.None),
                        XConstants.CurrentTime);
                if (ptrGrab != XConstants.GrabSuccess) {
                    XlibWrapper.XUngrabPointer(XToolkit.getDisplay(), XConstants.CurrentTime);
                    XAwtState.setGrabWindow(null);
                    grabLog.fine("    Grab Failure - mouse");
                    return false;
                }
                int keyGrab = XlibWrapper.XGrabKeyboard(XToolkit.getDisplay(),
                        getContentWindow(), ownerEvents, XConstants.GrabModeAsync, XConstants.GrabModeAsync,
                        XConstants.CurrentTime);
                if (keyGrab != XConstants.GrabSuccess) {
                    XlibWrapper.XUngrabPointer(XToolkit.getDisplay(), XConstants.CurrentTime);
                    XlibWrapper.XUngrabKeyboard(XToolkit.getDisplay(), XConstants.CurrentTime);
                    XAwtState.setGrabWindow(null);
                    grabLog.fine("    Grab Failure - keyboard");
                    return false;
                }
            }
            if (prevGrabWindow != null) {
                prevGrabWindow.ungrabInputImpl();
            }
            XAwtState.setGrabWindow(this);
            grabLog.fine("    Grab - success");
            return true;
        } finally {
            XToolkit.awtUnlock();
        }
    }
    static void ungrabInput() {
        XToolkit.awtLock();
        try {
            XBaseWindow grabWindow = XAwtState.getGrabWindow();
            grabLog.fine("UnGrab input on {0}", grabWindow);
            if (grabWindow != null) {
                grabWindow.ungrabInputImpl();
                if (!XToolkit.getSunAwtDisableGrab()) {
                    XlibWrapper.XUngrabPointer(XToolkit.getDisplay(), XConstants.CurrentTime);
                    XlibWrapper.XUngrabKeyboard(XToolkit.getDisplay(), XConstants.CurrentTime);
                }
                XAwtState.setGrabWindow(null);
                XlibWrapper.XFlush(XToolkit.getDisplay());
            }
        } finally {
            XToolkit.awtUnlock();
        }
    }
    void ungrabInputImpl() {
    }
    static void checkSecurity() {
        if (XToolkit.isSecurityWarningEnabled() && XToolkit.isToolkitThread()) {
            StackTraceElement stack[] = (new Throwable()).getStackTrace();
            log.warning(stack[1] + ": Security violation: calling user code on toolkit thread");
        }
    }
    public Set<Long> getChildren() {
        synchronized (getStateLock()) {
            return new HashSet<Long>(children);
        }
    }
    public void handleMapNotifyEvent(XEvent xev) {
        mapped = true;
    }
    public void handleUnmapNotifyEvent(XEvent xev) {
        mapped = false;
    }
    public void handleReparentNotifyEvent(XEvent xev) {
        if (eventLog.isLoggable(PlatformLogger.FINER)) {
            XReparentEvent msg = xev.get_xreparent();
            eventLog.finer(msg.toString());
        }
    }
    public void handlePropertyNotify(XEvent xev) {
        XPropertyEvent msg = xev.get_xproperty();
        if (XPropertyCache.isCachingSupported()) {
            XPropertyCache.clearCache(window, XAtom.get(msg.get_atom()));
        }
        if (eventLog.isLoggable(PlatformLogger.FINER)) {
            eventLog.finer("{0}", msg);
        }
    }
    public void handleDestroyNotify(XEvent xev) {
        XAnyEvent xany = xev.get_xany();
        if (xany.get_window() == getWindow()) {
            XToolkit.removeFromWinMap(getWindow(), this);
            if (XPropertyCache.isCachingSupported()) {
                XPropertyCache.clearCache(getWindow());
            }
        }
        if (xany.get_window() != getWindow()) {
            synchronized (getStateLock()) {
                children.remove(xany.get_window());
            }
        }
    }
    public void handleCreateNotify(XEvent xev) {
        XAnyEvent xany = xev.get_xany();
        if (xany.get_window() != getWindow()) {
            synchronized (getStateLock()) {
                children.add(xany.get_window());
            }
        }
    }
    public void handleClientMessage(XEvent xev) {
        if (eventLog.isLoggable(PlatformLogger.FINER)) {
            XClientMessageEvent msg = xev.get_xclient();
            eventLog.finer(msg.toString());
        }
    }
    public void handleVisibilityEvent(XEvent xev) {
    }
    public void handleKeyPress(XEvent xev) {
    }
    public void handleKeyRelease(XEvent xev) {
    }
    public void handleExposeEvent(XEvent xev) {
    }
    public void handleButtonPressRelease(XEvent xev) {
        XButtonEvent xbe = xev.get_xbutton();
        if (xbe.get_button() > SunToolkit.MAX_BUTTONS_SUPPORTED) {
            return;
        }
        int buttonState = 0;
        final int buttonsNumber = ((SunToolkit)(Toolkit.getDefaultToolkit())).getNumberOfButtons();
        for (int i = 0; i<buttonsNumber; i++){
            buttonState |= (xbe.get_state() & XConstants.buttonsMask[i]);
        }
        switch (xev.get_type()) {
        case XConstants.ButtonPress:
            if (buttonState == 0) {
                XAwtState.setAutoGrabWindow(this);
            }
            break;
        case XConstants.ButtonRelease:
            if (isFullRelease(buttonState, xbe.get_button())) {
                XAwtState.setAutoGrabWindow(null);
            }
            break;
        }
    }
    public void handleMotionNotify(XEvent xev) {
    }
    public void handleXCrossingEvent(XEvent xev) {
    }
    public void handleConfigureNotifyEvent(XEvent xev) {
        XConfigureEvent xe = xev.get_xconfigure();
        insLog.finer("Configure, {0}", xe);
        x = xe.get_x();
        y = xe.get_y();
        width = xe.get_width();
        height = xe.get_height();
    }
    static boolean isFullRelease(int buttonState, int button) {
        final int buttonsNumber = ((SunToolkit)(Toolkit.getDefaultToolkit())).getNumberOfButtons();
        if (button < 0 || button > buttonsNumber) {
            return buttonState == 0;
        } else {
            return buttonState == XConstants.buttonsMask[button - 1];
        }
    }
    static boolean isGrabbedEvent(XEvent ev, XBaseWindow target) {
        switch (ev.get_type()) {
          case XConstants.ButtonPress:
          case XConstants.ButtonRelease:
          case XConstants.MotionNotify:
          case XConstants.KeyPress:
          case XConstants.KeyRelease:
              return true;
          case XConstants.LeaveNotify:
          case XConstants.EnterNotify:
              return (target instanceof XWindowPeer);
          default:
              return false;
        }
    }
    static void dispatchToWindow(XEvent ev) {
        XBaseWindow target = XAwtState.getGrabWindow();
        if (target == null || !isGrabbedEvent(ev, target)) {
            target = XToolkit.windowToXWindow(ev.get_xany().get_window());
        }
        if (target != null && target.checkInitialised()) {
            target.dispatchEvent(ev);
        }
    }
    public void dispatchEvent(XEvent xev) {
        if (eventLog.isLoggable(PlatformLogger.FINEST)) eventLog.finest(xev.toString());
        int type = xev.get_type();
        if (isDisposed()) {
            return;
        }
        switch (type)
        {
          case XConstants.VisibilityNotify:
              handleVisibilityEvent(xev);
              break;
          case XConstants.ClientMessage:
              handleClientMessage(xev);
              break;
          case XConstants.Expose :
          case XConstants.GraphicsExpose :
              handleExposeEvent(xev);
              break;
          case XConstants.ButtonPress:
          case XConstants.ButtonRelease:
              handleButtonPressRelease(xev);
              break;
          case XConstants.MotionNotify:
              handleMotionNotify(xev);
              break;
          case XConstants.KeyPress:
              handleKeyPress(xev);
              break;
          case XConstants.KeyRelease:
              handleKeyRelease(xev);
              break;
          case XConstants.EnterNotify:
          case XConstants.LeaveNotify:
              handleXCrossingEvent(xev);
              break;
          case XConstants.ConfigureNotify:
              handleConfigureNotifyEvent(xev);
              break;
          case XConstants.MapNotify:
              handleMapNotifyEvent(xev);
              break;
          case XConstants.UnmapNotify:
              handleUnmapNotifyEvent(xev);
              break;
          case XConstants.ReparentNotify:
              handleReparentNotifyEvent(xev);
              break;
          case XConstants.PropertyNotify:
              handlePropertyNotify(xev);
              break;
          case XConstants.DestroyNotify:
              handleDestroyNotify(xev);
              break;
          case XConstants.CreateNotify:
              handleCreateNotify(xev);
              break;
        }
    }
    protected boolean isEventDisabled(XEvent e) {
        return false;
    }
    int getX() {
        return x;
    }
    int getY() {
        return y;
    }
    int getWidth() {
        return width;
    }
    int getHeight() {
        return height;
    }
    void setDisposed(boolean d) {
        disposed = d;
    }
    boolean isDisposed() {
        return disposed;
    }
    public int getAbsoluteX() {
        XBaseWindow pw = getParentWindow();
        if (pw != null) {
            return pw.getAbsoluteX() + getX();
        } else {
            return getX();
        }
    }
    public int getAbsoluteY() {
        XBaseWindow pw = getParentWindow();
        if (pw != null) {
            return pw.getAbsoluteY() + getY();
        } else {
            return getY();
        }
    }
    public XBaseWindow getParentWindow() {
        return parentWindow;
    }
    public XWindowPeer getToplevelXWindow() {
        XBaseWindow bw = this;
        while (bw != null && !(bw instanceof XWindowPeer)) {
            bw = bw.getParentWindow();
        }
        return (XWindowPeer)bw;
    }
    public String toString() {
        return super.toString() + "(" + Long.toString(getWindow(), 16) + ")";
    }
    public boolean contains(int x, int y) {
        return x >= 0 && y >= 0 && x < getWidth() && y < getHeight();
    }
    public boolean containsGlobal(int x, int y) {
        return x >= getAbsoluteX() && y >= getAbsoluteY() && x < (getAbsoluteX()+getWidth()) && y < (getAbsoluteY()+getHeight());
    }
}
