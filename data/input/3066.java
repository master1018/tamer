class XWindowPeer extends XPanelPeer implements WindowPeer,
                                                DisplayChangedListener {
    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.XWindowPeer");
    private static final PlatformLogger focusLog = PlatformLogger.getLogger("sun.awt.X11.focus.XWindowPeer");
    private static final PlatformLogger insLog = PlatformLogger.getLogger("sun.awt.X11.insets.XWindowPeer");
    private static final PlatformLogger grabLog = PlatformLogger.getLogger("sun.awt.X11.grab.XWindowPeer");
    private static final PlatformLogger iconLog = PlatformLogger.getLogger("sun.awt.X11.icon.XWindowPeer");
    private static Set<XWindowPeer> windows = new HashSet<XWindowPeer>();
    private boolean cachedFocusableWindow;
    XWarningWindow warningWindow;
    private boolean alwaysOnTop;
    private boolean locationByPlatform;
    Dialog modalBlocker;
    boolean delayedModalBlocking = false;
    Dimension targetMinimumSize = null;
    private XWindowPeer ownerPeer;
    protected XWindowPeer prevTransientFor, nextTransientFor;
    private XWindowPeer curRealTransientFor;
    private boolean grab = false; 
    private boolean isMapped = false; 
    private boolean mustControlStackPosition = false; 
    private XEventDispatcher rootPropertyEventDispatcher = null;
    private static final AtomicBoolean isStartupNotificationRemoved = new AtomicBoolean();
    private boolean isUnhiding = false;             
    private boolean isBeforeFirstMapNotify = false; 
    private Window.Type windowType = Window.Type.NORMAL;
    public final Window.Type getWindowType() {
        return windowType;
    }
    protected Vector <ToplevelStateListener> toplevelStateListeners = new Vector<ToplevelStateListener>();
    XWindowPeer(XCreateWindowParams params) {
        super(params.putIfNull(PARENT_WINDOW, Long.valueOf(0)));
    }
    XWindowPeer(Window target) {
        super(new XCreateWindowParams(new Object[] {
            TARGET, target,
            PARENT_WINDOW, Long.valueOf(0)}));
    }
    private static final int PREFERRED_SIZE_FOR_ICON = 128;
    private static final int MAXIMUM_BUFFER_LENGTH_NET_WM_ICON = (2<<15) - 1;
    void preInit(XCreateWindowParams params) {
        target = (Component)params.get(TARGET);
        windowType = ((Window)target).getType();
        params.put(REPARENTED,
                   Boolean.valueOf(isOverrideRedirect() || isSimpleWindow()));
        super.preInit(params);
        params.putIfNull(BIT_GRAVITY, Integer.valueOf(XConstants.NorthWestGravity));
        long eventMask = 0;
        if (params.containsKey(EVENT_MASK)) {
            eventMask = ((Long)params.get(EVENT_MASK));
        }
        eventMask |= XConstants.VisibilityChangeMask;
        params.put(EVENT_MASK, eventMask);
        XA_NET_WM_STATE = XAtom.get("_NET_WM_STATE");
        params.put(OVERRIDE_REDIRECT, Boolean.valueOf(isOverrideRedirect()));
        SunToolkit.awtLock();
        try {
            windows.add(this);
        } finally {
            SunToolkit.awtUnlock();
        }
        cachedFocusableWindow = isFocusableWindow();
        Font f = target.getFont();
        if (f == null) {
            f = XWindow.getDefaultFont();
            target.setFont(f);
        }
        Color c = target.getBackground();
        if (c == null) {
            Color background = SystemColor.window;
            target.setBackground(background);
        }
        c = target.getForeground();
        if (c == null) {
            target.setForeground(SystemColor.windowText);
        }
        alwaysOnTop = ((Window)target).isAlwaysOnTop() && ((Window)target).isAlwaysOnTopSupported();
        GraphicsConfiguration gc = getGraphicsConfiguration();
        ((X11GraphicsDevice)gc.getDevice()).addDisplayChangedListener(this);
    }
    protected String getWMName() {
        String name = target.getName();
        if (name == null || name.trim().equals("")) {
            name = " ";
        }
        return name;
    }
    void postInit(XCreateWindowParams params) {
        super.postInit(params);
        initWMProtocols();
        Window t_window = (Window)target;
        Window owner = t_window.getOwner();
        if (owner != null) {
            ownerPeer = (XWindowPeer)owner.getPeer();
            if (focusLog.isLoggable(PlatformLogger.FINER)) {
                focusLog.fine("Owner is " + owner);
                focusLog.fine("Owner peer is " + ownerPeer);
                focusLog.fine("Owner X window " + Long.toHexString(ownerPeer.getWindow()));
                focusLog.fine("Owner content X window " + Long.toHexString(ownerPeer.getContentWindow()));
            }
            long ownerWindow = ownerPeer.getWindow();
            if (ownerWindow != 0) {
                XToolkit.awtLock();
                try {
                    if (focusLog.isLoggable(PlatformLogger.FINE)) focusLog.fine("Setting transient on " + Long.toHexString(getWindow())
                                                                       + " for " + Long.toHexString(ownerWindow));
                    setToplevelTransientFor(this, ownerPeer, false, true);
                    XWMHints hints = getWMHints();
                    hints.set_flags(hints.get_flags() | (int)XUtilConstants.WindowGroupHint);
                    hints.set_window_group(ownerWindow);
                    XlibWrapper.XSetWMHints(XToolkit.getDisplay(), getWindow(), hints.pData);
                }
                finally {
                    XToolkit.awtUnlock();
                }
            }
        }
        if (((Window)target).getWarningString() != null) {
            if (!AWTAccessor.getWindowAccessor().isTrayIconWindow((Window)target)) {
                warningWindow = new XWarningWindow((Window)target, getWindow(), this);
            }
        }
        setSaveUnder(true);
        updateIconImages();
        updateShape();
        updateOpacity();
    }
    public void updateIconImages() {
        Window target = (Window)this.target;
        java.util.List<Image> iconImages = ((Window)target).getIconImages();
        XWindowPeer ownerPeer = getOwnerPeer();
        winAttr.icons = new ArrayList<XIconInfo>();
        if (iconImages.size() != 0) {
            winAttr.iconsInherited = false;
            for (Iterator<Image> i = iconImages.iterator(); i.hasNext(); ) {
                Image image = i.next();
                if (image == null) {
                    if (log.isLoggable(PlatformLogger.FINEST)) {
                        log.finest("XWindowPeer.updateIconImages: Skipping the image passed into Java because it's null.");
                    }
                    continue;
                }
                XIconInfo iconInfo;
                try {
                    iconInfo = new XIconInfo(image);
                } catch (Exception e){
                    if (log.isLoggable(PlatformLogger.FINEST)) {
                        log.finest("XWindowPeer.updateIconImages: Perhaps the image passed into Java is broken. Skipping this icon.");
                    }
                    continue;
                }
                if (iconInfo.isValid()) {
                    winAttr.icons.add(iconInfo);
                }
            }
        }
        winAttr.icons = normalizeIconImages(winAttr.icons);
        if (winAttr.icons.size() == 0) {
            if (ownerPeer != null) {
                winAttr.iconsInherited = true;
                winAttr.icons = ownerPeer.getIconInfo();
            } else {
                winAttr.iconsInherited = false;
                winAttr.icons = getDefaultIconInfo();
            }
        }
        recursivelySetIcon(winAttr.icons);
    }
    static java.util.List<XIconInfo> normalizeIconImages(java.util.List<XIconInfo> icons) {
        java.util.List<XIconInfo> result = new ArrayList<XIconInfo>();
        int totalLength = 0;
        boolean haveLargeIcon = false;
        for (XIconInfo icon : icons) {
            int width = icon.getWidth();
            int height = icon.getHeight();
            int length = icon.getRawLength();
            if (width > PREFERRED_SIZE_FOR_ICON || height > PREFERRED_SIZE_FOR_ICON) {
                if (haveLargeIcon) {
                    continue;
                }
                int scaledWidth = width;
                int scaledHeight = height;
                while (scaledWidth > PREFERRED_SIZE_FOR_ICON ||
                       scaledHeight > PREFERRED_SIZE_FOR_ICON) {
                    scaledWidth = scaledWidth / 2;
                    scaledHeight = scaledHeight / 2;
                }
                icon.setScaledSize(scaledWidth, scaledHeight);
                length = icon.getRawLength();
            }
            if (totalLength + length <= MAXIMUM_BUFFER_LENGTH_NET_WM_ICON) {
                totalLength += length;
                result.add(icon);
                if (width > PREFERRED_SIZE_FOR_ICON || height > PREFERRED_SIZE_FOR_ICON) {
                    haveLargeIcon = true;
                }
            }
        }
        if (iconLog.isLoggable(PlatformLogger.FINEST)) {
            iconLog.finest(">>> Length_ of buffer of icons data: " + totalLength +
                           ", maximum length: " + MAXIMUM_BUFFER_LENGTH_NET_WM_ICON);
        }
        return result;
    }
    static void dumpIcons(java.util.List<XIconInfo> icons) {
        if (iconLog.isLoggable(PlatformLogger.FINEST)) {
            iconLog.finest(">>> Sizes of icon images:");
            for (Iterator<XIconInfo> i = icons.iterator(); i.hasNext(); ) {
                iconLog.finest("    {0}", i.next());
            }
        }
    }
    public void recursivelySetIcon(java.util.List<XIconInfo> icons) {
        dumpIcons(winAttr.icons);
        setIconHints(icons);
        Window target = (Window)this.target;
        Window[] children = target.getOwnedWindows();
        int cnt = children.length;
        for (int i = 0; i < cnt; i++) {
            ComponentPeer childPeer = children[i].getPeer();
            if (childPeer != null && childPeer instanceof XWindowPeer) {
                if (((XWindowPeer)childPeer).winAttr.iconsInherited) {
                    ((XWindowPeer)childPeer).winAttr.icons = icons;
                    ((XWindowPeer)childPeer).recursivelySetIcon(icons);
                }
            }
        }
    }
    java.util.List<XIconInfo> getIconInfo() {
        return winAttr.icons;
    }
    void setIconHints(java.util.List<XIconInfo> icons) {
    }
    private static ArrayList<XIconInfo> defaultIconInfo;
    protected synchronized static java.util.List<XIconInfo> getDefaultIconInfo() {
        if (defaultIconInfo == null) {
            defaultIconInfo = new ArrayList<XIconInfo>();
            if (XlibWrapper.dataModel == 32) {
                defaultIconInfo.add(new XIconInfo(XAWTIcon32_java_icon16_png.java_icon16_png));
                defaultIconInfo.add(new XIconInfo(XAWTIcon32_java_icon24_png.java_icon24_png));
                defaultIconInfo.add(new XIconInfo(XAWTIcon32_java_icon32_png.java_icon32_png));
                defaultIconInfo.add(new XIconInfo(XAWTIcon32_java_icon48_png.java_icon48_png));
            } else {
                defaultIconInfo.add(new XIconInfo(XAWTIcon64_java_icon16_png.java_icon16_png));
                defaultIconInfo.add(new XIconInfo(XAWTIcon64_java_icon24_png.java_icon24_png));
                defaultIconInfo.add(new XIconInfo(XAWTIcon64_java_icon32_png.java_icon32_png));
                defaultIconInfo.add(new XIconInfo(XAWTIcon64_java_icon48_png.java_icon48_png));
            }
        }
        return defaultIconInfo;
    }
    private void updateShape() {
        Shape shape = AWTAccessor.getWindowAccessor().getShape((Window)target);
        if (shape != null) {
            applyShape(Region.getInstance(shape, null));
        }
    }
    private void updateOpacity() {
        float opacity = AWTAccessor.getWindowAccessor().getOpacity((Window)target);
        if (opacity < 1.0f) {
            setOpacity(opacity);
        }
    }
    public void updateMinimumSize() {
        targetMinimumSize = (((Component)target).isMinimumSizeSet()) ?
            ((Component)target).getMinimumSize() : null;
    }
    public Dimension getTargetMinimumSize() {
        return (targetMinimumSize == null) ? null : new Dimension(targetMinimumSize);
    }
    public XWindowPeer getOwnerPeer() {
        return ownerPeer;
    }
    public void setBounds(int x, int y, int width, int height, int op) {
        XToolkit.awtLock();
        try {
            Rectangle oldBounds = getBounds();
            super.setBounds(x, y, width, height, op);
            Rectangle bounds = getBounds();
            XSizeHints hints = getHints();
            setSizeHints(hints.get_flags() | XUtilConstants.PPosition | XUtilConstants.PSize,
                             bounds.x, bounds.y, bounds.width, bounds.height);
            XWM.setMotifDecor(this, false, 0, 0);
            XNETProtocol protocol = XWM.getWM().getNETProtocol();
            if (protocol != null && protocol.active()) {
                XAtomList net_wm_state = getNETWMState();
                net_wm_state.add(protocol.XA_NET_WM_STATE_SKIP_TASKBAR);
                setNETWMState(net_wm_state);
            }
            boolean isResized = !bounds.getSize().equals(oldBounds.getSize());
            boolean isMoved = !bounds.getLocation().equals(oldBounds.getLocation());
            if (isMoved || isResized) {
                repositionSecurityWarning();
            }
            if (isResized) {
                postEventToEventQueue(new ComponentEvent(getEventSource(), ComponentEvent.COMPONENT_RESIZED));
            }
            if (isMoved) {
                postEventToEventQueue(new ComponentEvent(getEventSource(), ComponentEvent.COMPONENT_MOVED));
            }
        } finally {
            XToolkit.awtUnlock();
        }
    }
    void updateFocusability() {
        updateFocusableWindowState();
        XToolkit.awtLock();
        try {
            XWMHints hints = getWMHints();
            hints.set_flags(hints.get_flags() | (int)XUtilConstants.InputHint);
            hints.set_input(false);
            XlibWrapper.XSetWMHints(XToolkit.getDisplay(), getWindow(), hints.pData);
        }
        finally {
            XToolkit.awtUnlock();
        }
    }
    public Insets getInsets() {
        return new Insets(0, 0, 0, 0);
    }
    public void handleIconify() {
        postEvent(new WindowEvent((Window)target, WindowEvent.WINDOW_ICONIFIED));
    }
    public void handleDeiconify() {
        postEvent(new WindowEvent((Window)target, WindowEvent.WINDOW_DEICONIFIED));
    }
    public void handleStateChange(int oldState, int newState) {
        postEvent(new WindowEvent((Window)target,
                                  WindowEvent.WINDOW_STATE_CHANGED,
                                  oldState, newState));
    }
    public Insets insets() {
        return getInsets();
    }
    boolean isAutoRequestFocus() {
        if (XToolkit.isToolkitThread()) {
            return AWTAccessor.getWindowAccessor().isAutoRequestFocus((Window)target);
        } else {
            return ((Window)target).isAutoRequestFocus();
        }
    }
    static XWindowPeer getNativeFocusedWindowPeer() {
        XBaseWindow baseWindow = XToolkit.windowToXWindow(xGetInputFocus());
        return (baseWindow instanceof XWindowPeer) ? (XWindowPeer)baseWindow :
               (baseWindow instanceof XFocusProxyWindow) ?
               ((XFocusProxyWindow)baseWindow).getOwner() : null;
    }
    static Window getNativeFocusedWindow() {
        XWindowPeer peer = getNativeFocusedWindowPeer();
        return peer != null ? (Window)peer.target : null;
    }
    boolean isFocusableWindow() {
        if (XToolkit.isToolkitThread() || SunToolkit.isAWTLockHeldByCurrentThread())
        {
            return cachedFocusableWindow;
        } else {
            return ((Window)target).isFocusableWindow();
        }
    }
    boolean isFocusedWindowModalBlocker() {
        return false;
    }
    long getFocusTargetWindow() {
        return getContentWindow();
    }
    boolean isNativelyNonFocusableWindow() {
        if (XToolkit.isToolkitThread() || SunToolkit.isAWTLockHeldByCurrentThread())
        {
            return isSimpleWindow() || !cachedFocusableWindow;
        } else {
            return isSimpleWindow() || !(((Window)target).isFocusableWindow());
        }
    }
    public void handleWindowFocusIn_Dispatch() {
        if (EventQueue.isDispatchThread()) {
            XKeyboardFocusManagerPeer.setCurrentNativeFocusedWindow((Window) target);
            WindowEvent we = new WindowEvent((Window)target, WindowEvent.WINDOW_GAINED_FOCUS);
            SunToolkit.setSystemGenerated(we);
            target.dispatchEvent(we);
        }
    }
    public void handleWindowFocusInSync(long serial) {
        WindowEvent we = new WindowEvent((Window)target, WindowEvent.WINDOW_GAINED_FOCUS);
        XKeyboardFocusManagerPeer.setCurrentNativeFocusedWindow((Window) target);
        sendEvent(we);
    }
    public void handleWindowFocusIn(long serial) {
        WindowEvent we = new WindowEvent((Window)target, WindowEvent.WINDOW_GAINED_FOCUS);
        XKeyboardFocusManagerPeer.setCurrentNativeFocusedWindow((Window) target);
        postEvent(wrapInSequenced((AWTEvent) we));
    }
    public void handleWindowFocusOut(Window oppositeWindow, long serial) {
        WindowEvent we = new WindowEvent((Window)target, WindowEvent.WINDOW_LOST_FOCUS, oppositeWindow);
        XKeyboardFocusManagerPeer.setCurrentNativeFocusedWindow(null);
        XKeyboardFocusManagerPeer.setCurrentNativeFocusOwner(null);
        postEvent(wrapInSequenced((AWTEvent) we));
    }
    public void handleWindowFocusOutSync(Window oppositeWindow, long serial) {
        WindowEvent we = new WindowEvent((Window)target, WindowEvent.WINDOW_LOST_FOCUS, oppositeWindow);
        XKeyboardFocusManagerPeer.setCurrentNativeFocusedWindow(null);
        XKeyboardFocusManagerPeer.setCurrentNativeFocusOwner(null);
        sendEvent(we);
    }
    public void checkIfOnNewScreen(Rectangle newBounds) {
        if (!XToolkit.localEnv.runningXinerama()) {
            return;
        }
        if (log.isLoggable(PlatformLogger.FINEST)) {
            log.finest("XWindowPeer: Check if we've been moved to a new screen since we're running in Xinerama mode");
        }
        int area = newBounds.width * newBounds.height;
        int intAmt, vertAmt, horizAmt;
        int largestAmt = 0;
        int curScreenNum = ((X11GraphicsDevice)getGraphicsConfiguration().getDevice()).getScreen();
        int newScreenNum = 0;
        GraphicsDevice gds[] = XToolkit.localEnv.getScreenDevices();
        GraphicsConfiguration newGC = null;
        Rectangle screenBounds;
        for (int i = 0; i < gds.length; i++) {
            screenBounds = gds[i].getDefaultConfiguration().getBounds();
            if (newBounds.intersects(screenBounds)) {
                horizAmt = Math.min(newBounds.x + newBounds.width,
                                    screenBounds.x + screenBounds.width) -
                           Math.max(newBounds.x, screenBounds.x);
                vertAmt = Math.min(newBounds.y + newBounds.height,
                                   screenBounds.y + screenBounds.height)-
                          Math.max(newBounds.y, screenBounds.y);
                intAmt = horizAmt * vertAmt;
                if (intAmt == area) {
                    newScreenNum = i;
                    newGC = gds[i].getDefaultConfiguration();
                    break;
                }
                if (intAmt > largestAmt) {
                    largestAmt = intAmt;
                    newScreenNum = i;
                    newGC = gds[i].getDefaultConfiguration();
                }
            }
        }
        if (newScreenNum != curScreenNum) {
            if (log.isLoggable(PlatformLogger.FINEST)) {
                log.finest("XWindowPeer: Moved to a new screen");
            }
            executeDisplayChangedOnEDT(newGC);
        }
    }
    private void executeDisplayChangedOnEDT(final GraphicsConfiguration gc) {
        Runnable dc = new Runnable() {
            public void run() {
                AWTAccessor.getComponentAccessor().
                    setGraphicsConfiguration((Component)target, gc);
            }
        };
        SunToolkit.executeOnEventHandlerThread((Component)target, dc);
    }
    public void displayChanged() {
        executeDisplayChangedOnEDT(getGraphicsConfiguration());
    }
    public void paletteChanged() {
    }
    @Override
    public void handleConfigureNotifyEvent(XEvent xev) {
        XConfigureEvent xe = xev.get_xconfigure();
        checkIfOnNewScreen(new Rectangle(xe.get_x(),
                                         xe.get_y(),
                                         xe.get_width(),
                                         xe.get_height()));
        super.handleConfigureNotifyEvent(xev);
        repositionSecurityWarning();
    }
    final void requestXFocus(long time) {
        requestXFocus(time, true);
    }
    final void requestXFocus() {
        requestXFocus(0, false);
    }
    protected void requestXFocus(long time, boolean timeProvided) {
        if (focusLog.isLoggable(PlatformLogger.FINE)) focusLog.fine("Requesting window focus");
        requestWindowFocus(time, timeProvided);
    }
    public final boolean focusAllowedFor() {
        if (isNativelyNonFocusableWindow()) {
            return false;
        }
        if (isModalBlocked()) {
            return false;
        }
        return true;
    }
    public void handleFocusEvent(XEvent xev) {
        XFocusChangeEvent xfe = xev.get_xfocus();
        FocusEvent fe;
        focusLog.fine("{0}", xfe);
        if (isEventDisabled(xev)) {
            return;
        }
        if (xev.get_type() == XConstants.FocusIn)
        {
            if (focusAllowedFor()) {
                if (xfe.get_mode() == XConstants.NotifyNormal 
                    || xfe.get_mode() == XConstants.NotifyWhileGrabbed) 
                {
                    handleWindowFocusIn(xfe.get_serial());
                }
            }
        }
        else
        {
            if (xfe.get_mode() == XConstants.NotifyNormal 
                || xfe.get_mode() == XConstants.NotifyWhileGrabbed) 
            {
                if (!isNativelyNonFocusableWindow()) {
                    XWindowPeer oppositeXWindow = getNativeFocusedWindowPeer();
                    Object oppositeTarget = (oppositeXWindow!=null)? oppositeXWindow.getTarget() : null;
                    Window oppositeWindow = null;
                    if (oppositeTarget instanceof Window) {
                        oppositeWindow = (Window) oppositeTarget;
                    }
                    if (oppositeXWindow != null && oppositeXWindow.isNativelyNonFocusableWindow()) {
                        return;
                    }
                    if (this == oppositeXWindow) {
                        oppositeWindow = null;
                    } else if (oppositeXWindow instanceof XDecoratedPeer) {
                        if (((XDecoratedPeer) oppositeXWindow).actualFocusedWindow != null) {
                            oppositeXWindow = ((XDecoratedPeer) oppositeXWindow).actualFocusedWindow;
                            oppositeTarget = oppositeXWindow.getTarget();
                            if (oppositeTarget instanceof Window
                                && oppositeXWindow.isVisible()
                                && oppositeXWindow.isNativelyNonFocusableWindow())
                            {
                                oppositeWindow = ((Window) oppositeTarget);
                            }
                        }
                    }
                    handleWindowFocusOut(oppositeWindow, xfe.get_serial());
                }
            }
        }
    }
    void setSaveUnder(boolean state) {}
    public void toFront() {
        if (isOverrideRedirect() && mustControlStackPosition) {
            mustControlStackPosition = false;
            removeRootPropertyEventDispatcher();
        }
        if (isVisible()) {
            super.toFront();
            if (isFocusableWindow() && isAutoRequestFocus() &&
                !isModalBlocked() && !isWithdrawn())
            {
                requestInitialFocus();
            }
        } else {
            setVisible(true);
        }
    }
    public void toBack() {
        XToolkit.awtLock();
        try {
            if(!isOverrideRedirect()) {
                XlibWrapper.XLowerWindow(XToolkit.getDisplay(), getWindow());
            }else{
                lowerOverrideRedirect();
            }
        }
        finally {
            XToolkit.awtUnlock();
        }
    }
    private void lowerOverrideRedirect() {
        HashSet toplevels = new HashSet();
        long topl = 0, mytopl = 0;
        for (XWindowPeer xp : windows) {
            topl = getToplevelWindow( xp.getWindow() );
            if( xp.equals( this ) ) {
                mytopl = topl;
            }
            if( topl > 0 )
                toplevels.add( Long.valueOf( topl ) );
        }
        long laux,     wDesktop = -1, wBottom = -1;
        int  iMy = -1, iDesktop = -1, iBottom = -1;
        int i = 0;
        XQueryTree xqt = new XQueryTree(XToolkit.getDefaultRootWindow());
        try {
            if( xqt.execute() > 0 ) {
                int nchildren = xqt.get_nchildren();
                long children = xqt.get_children();
                for(i = 0; i < nchildren; i++) {
                    laux = Native.getWindow(children, i);
                    if( laux == mytopl ) {
                        iMy = i;
                    }else if( isDesktopWindow( laux ) ) {
                        iDesktop = i;
                        wDesktop = laux;
                    }else if(iBottom < 0 &&
                             toplevels.contains( Long.valueOf(laux) ) &&
                             laux != mytopl) {
                        iBottom = i;
                        wBottom = laux;
                    }
                }
            }
            if( (iMy < iBottom || iBottom < 0 )&& iDesktop < iMy)
                return; 
            long to_restack = Native.allocateLongArray(2);
            Native.putLong(to_restack, 0, wBottom);
            Native.putLong(to_restack, 1,  mytopl);
            XlibWrapper.XRestackWindows(XToolkit.getDisplay(), to_restack, 2);
            XlibWrapper.unsafe.freeMemory(to_restack);
            if( !mustControlStackPosition ) {
                mustControlStackPosition = true;
                addRootPropertyEventDispatcher();
            }
        } finally {
            xqt.dispose();
        }
    }
    private long getToplevelWindow( long w ) {
        long wi = w, ret, root;
        do {
            ret = wi;
            XQueryTree qt = new XQueryTree(wi);
            try {
                if (qt.execute() == 0) {
                    return 0;
                }
                root = qt.get_root();
                wi = qt.get_parent();
            } finally {
                qt.dispose();
            }
        } while (wi != root);
        return ret;
    }
    private static boolean isDesktopWindow( long wi ) {
        return XWM.getWM().isDesktopWindow( wi );
    }
    private void updateAlwaysOnTop() {
        log.fine("Promoting always-on-top state {0}", Boolean.valueOf(alwaysOnTop));
        XWM.getWM().setLayer(this,
                             alwaysOnTop ?
                             XLayerProtocol.LAYER_ALWAYS_ON_TOP :
                             XLayerProtocol.LAYER_NORMAL);
    }
    public void setAlwaysOnTop(boolean alwaysOnTop) {
        this.alwaysOnTop = alwaysOnTop;
        updateAlwaysOnTop();
    }
    boolean isLocationByPlatform() {
        return locationByPlatform;
    }
    private void promoteDefaultPosition() {
        this.locationByPlatform = ((Window)target).isLocationByPlatform();
        if (locationByPlatform) {
            XToolkit.awtLock();
            try {
                Rectangle bounds = getBounds();
                XSizeHints hints = getHints();
                setSizeHints(hints.get_flags() & ~(XUtilConstants.USPosition | XUtilConstants.PPosition),
                             bounds.x, bounds.y, bounds.width, bounds.height);
            } finally {
                XToolkit.awtUnlock();
            }
        }
    }
    public void setVisible(boolean vis) {
        if (!isVisible() && vis) {
            isBeforeFirstMapNotify = true;
            winAttr.initialFocus = isAutoRequestFocus();
            if (!winAttr.initialFocus) {
                suppressWmTakeFocus(true);
            }
        }
        updateFocusability();
        promoteDefaultPosition();
        if (!vis && warningWindow != null) {
            warningWindow.setSecurityWarningVisible(false, false);
        }
        super.setVisible(vis);
        if (!vis && !isWithdrawn()) {
            XToolkit.awtLock();
            try {
                XUnmapEvent unmap = new XUnmapEvent();
                unmap.set_window(window);
                unmap.set_event(XToolkit.getDefaultRootWindow());
                unmap.set_type((int)XConstants.UnmapNotify);
                unmap.set_from_configure(false);
                XlibWrapper.XSendEvent(XToolkit.getDisplay(), XToolkit.getDefaultRootWindow(),
                        false, XConstants.SubstructureNotifyMask | XConstants.SubstructureRedirectMask,
                        unmap.pData);
                unmap.dispose();
            }
            finally {
                XToolkit.awtUnlock();
            }
        }
        if (isOverrideRedirect() && vis) {
            updateChildrenSizes();
        }
        repositionSecurityWarning();
    }
    protected void suppressWmTakeFocus(boolean doSuppress) {
    }
    final boolean isSimpleWindow() {
        return !(target instanceof Frame || target instanceof Dialog);
    }
    boolean hasWarningWindow() {
        return ((Window)target).getWarningString() != null;
    }
    int getMenuBarHeight() {
        return 0;
    }
    void updateChildrenSizes() {
    }
    public void repositionSecurityWarning() {
        if (warningWindow != null) {
            AWTAccessor.ComponentAccessor compAccessor = AWTAccessor.getComponentAccessor();
            int x = compAccessor.getX(target);
            int y = compAccessor.getY(target);
            int width = compAccessor.getWidth(target);
            int height = compAccessor.getHeight(target);
            warningWindow.reposition(x, y, width, height);
        }
    }
    @Override
    protected void setMouseAbove(boolean above) {
        super.setMouseAbove(above);
        updateSecurityWarningVisibility();
    }
    @Override
    public void setFullScreenExclusiveModeState(boolean state) {
        super.setFullScreenExclusiveModeState(state);
        updateSecurityWarningVisibility();
    }
    public void updateSecurityWarningVisibility() {
        if (warningWindow == null) {
            return;
        }
        if (!isVisible()) {
            return; 
        }
        boolean show = false;
        if (!isFullScreenExclusiveMode()) {
            int state = getWMState();
            if (isVisible() && (state == XUtilConstants.NormalState || isSimpleWindow())) {
                if (XKeyboardFocusManagerPeer.getCurrentNativeFocusedWindow() ==
                        getTarget())
                {
                    show = true;
                }
                if (isMouseAbove() || warningWindow.isMouseAbove())
                {
                    show = true;
                }
            }
        }
        warningWindow.setSecurityWarningVisible(show, true);
    }
    boolean isOverrideRedirect() {
        return XWM.getWMID() == XWM.OPENLOOK_WM ||
            Window.Type.POPUP.equals(getWindowType());
    }
    final boolean isOLWMDecorBug() {
        return XWM.getWMID() == XWM.OPENLOOK_WM &&
            winAttr.nativeDecor == false;
    }
    public void dispose() {
        SunToolkit.awtLock();
        try {
            windows.remove(this);
        } finally {
            SunToolkit.awtUnlock();
        }
        if (warningWindow != null) {
            warningWindow.destroy();
        }
        removeRootPropertyEventDispatcher();
        mustControlStackPosition = false;
        super.dispose();
        if (isSimpleWindow()) {
            if (target == XKeyboardFocusManagerPeer.getCurrentNativeFocusedWindow()) {
                Window owner = getDecoratedOwner((Window)target);
                ((XWindowPeer)AWTAccessor.getComponentAccessor().getPeer(owner)).requestWindowFocus();
            }
        }
    }
    boolean isResizable() {
        return winAttr.isResizable;
    }
    public void handleVisibilityEvent(XEvent xev) {
        super.handleVisibilityEvent(xev);
        XVisibilityEvent ve = xev.get_xvisibility();
        winAttr.visibilityState = ve.get_state();
        repositionSecurityWarning();
    }
    void handleRootPropertyNotify(XEvent xev) {
        XPropertyEvent ev = xev.get_xproperty();
        if( mustControlStackPosition &&
            ev.get_atom() == XAtom.get("_NET_CLIENT_LIST_STACKING").getAtom()){
            if(isOverrideRedirect()) {
                toBack();
            }
        }
    }
    private void removeStartupNotification() {
        if (isStartupNotificationRemoved.getAndSet(true)) {
            return;
        }
        final String desktopStartupId = AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return XToolkit.getEnv("DESKTOP_STARTUP_ID");
            }
        });
        if (desktopStartupId == null) {
            return;
        }
        final StringBuilder messageBuilder = new StringBuilder("remove: ID=");
        messageBuilder.append('"');
        for (int i = 0; i < desktopStartupId.length(); i++) {
            if (desktopStartupId.charAt(i) == '"' || desktopStartupId.charAt(i) == '\\') {
                messageBuilder.append('\\');
            }
            messageBuilder.append(desktopStartupId.charAt(i));
        }
        messageBuilder.append('"');
        messageBuilder.append('\0');
        final byte[] message;
        try {
            message = messageBuilder.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException cannotHappen) {
            return;
        }
        XClientMessageEvent req = null;
        XToolkit.awtLock();
        try {
            final XAtom netStartupInfoBeginAtom = XAtom.get("_NET_STARTUP_INFO_BEGIN");
            final XAtom netStartupInfoAtom = XAtom.get("_NET_STARTUP_INFO");
            req = new XClientMessageEvent();
            req.set_type(XConstants.ClientMessage);
            req.set_window(getWindow());
            req.set_message_type(netStartupInfoBeginAtom.getAtom());
            req.set_format(8);
            for (int pos = 0; pos < message.length; pos += 20) {
                final int msglen = Math.min(message.length - pos, 20);
                int i = 0;
                for (; i < msglen; i++) {
                    XlibWrapper.unsafe.putByte(req.get_data() + i, message[pos + i]);
                }
                for (; i < 20; i++) {
                    XlibWrapper.unsafe.putByte(req.get_data() + i, (byte)0);
                }
                XlibWrapper.XSendEvent(XToolkit.getDisplay(),
                    XlibWrapper.RootWindow(XToolkit.getDisplay(), getScreenNumber()),
                    false,
                    XConstants.PropertyChangeMask,
                    req.pData);
                req.set_message_type(netStartupInfoAtom.getAtom());
            }
        } finally {
            XToolkit.awtUnlock();
            if (req != null) {
                req.dispose();
            }
        }
    }
    public void handleMapNotifyEvent(XEvent xev) {
        removeStartupNotification();
        isUnhiding |= isWMStateNetHidden();
        super.handleMapNotifyEvent(xev);
        if (!winAttr.initialFocus) {
            suppressWmTakeFocus(false); 
            XToolkit.awtLock();
            try {
                XlibWrapper.XRaiseWindow(XToolkit.getDisplay(), getWindow());
            } finally {
                XToolkit.awtUnlock();
            }
        }
        if (shouldFocusOnMapNotify()) {
            focusLog.fine("Automatically request focus on window");
            requestInitialFocus();
        }
        isUnhiding = false;
        isBeforeFirstMapNotify = false;
        updateAlwaysOnTop();
        synchronized (getStateLock()) {
            if (!isMapped) {
                isMapped = true;
            }
        }
    }
    public void handleUnmapNotifyEvent(XEvent xev) {
        super.handleUnmapNotifyEvent(xev);
        isUnhiding |= isWMStateNetHidden();
        synchronized (getStateLock()) {
            if (isMapped) {
                isMapped = false;
            }
        }
    }
    private boolean shouldFocusOnMapNotify() {
        boolean res = false;
        if (isBeforeFirstMapNotify) {
            res = (winAttr.initialFocus ||          
                   isFocusedWindowModalBlocker());
        } else {
            res = isUnhiding;                       
        }
        res = res &&
            isFocusableWindow() &&                  
            !isModalBlocked();                      
        return res;
    }
    protected boolean isWMStateNetHidden() {
        XNETProtocol protocol = XWM.getWM().getNETProtocol();
        return (protocol != null && protocol.isWMStateNetHidden(this));
    }
    protected void requestInitialFocus() {
        requestXFocus();
    }
    public void addToplevelStateListener(ToplevelStateListener l){
        toplevelStateListeners.add(l);
    }
    public void removeToplevelStateListener(ToplevelStateListener l){
        toplevelStateListeners.remove(l);
    }
    @Override
    protected void stateChanged(long time, int oldState, int newState) {
        updateTransientFor();
        for (ToplevelStateListener topLevelListenerTmp : toplevelStateListeners) {
            topLevelListenerTmp.stateChangedICCCM(oldState, newState);
        }
        updateSecurityWarningVisibility();
    }
    boolean isWithdrawn() {
        return getWMState() == XUtilConstants.WithdrawnState;
    }
    boolean hasDecorations(int decor) {
        if (!winAttr.nativeDecor) {
            return false;
        }
        else {
            int myDecor = winAttr.decorations;
            boolean hasBits = ((myDecor & decor) == decor);
            if ((myDecor & XWindowAttributesData.AWT_DECOR_ALL) != 0)
                return !hasBits;
            else
                return hasBits;
        }
    }
    void setReparented(boolean newValue) {
        super.setReparented(newValue);
        XToolkit.awtLock();
        try {
            if (isReparented() && delayedModalBlocking) {
                addToTransientFors((XDialogPeer) AWTAccessor.getComponentAccessor().getPeer(modalBlocker));
                delayedModalBlocking = false;
            }
        } finally {
            XToolkit.awtUnlock();
        }
    }
    static Vector<XWindowPeer> collectJavaToplevels() {
        Vector<XWindowPeer> javaToplevels = new Vector<XWindowPeer>();
        Vector<Long> v = new Vector<Long>();
        X11GraphicsEnvironment ge =
            (X11GraphicsEnvironment)GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gds = ge.getScreenDevices();
        if (!ge.runningXinerama() && (gds.length > 1)) {
            for (GraphicsDevice gd : gds) {
                int screen = ((X11GraphicsDevice)gd).getScreen();
                long rootWindow = XlibWrapper.RootWindow(XToolkit.getDisplay(), screen);
                v.add(rootWindow);
            }
        } else {
            v.add(XToolkit.getDefaultRootWindow());
        }
        final int windowsCount = windows.size();
        while ((v.size() > 0) && (javaToplevels.size() < windowsCount)) {
            long win = v.remove(0);
            XQueryTree qt = new XQueryTree(win);
            try {
                if (qt.execute() != 0) {
                    int nchildren = qt.get_nchildren();
                    long children = qt.get_children();
                    for (int i = 0; i < nchildren; i++) {
                        long child = Native.getWindow(children, i);
                        XBaseWindow childWindow = XToolkit.windowToXWindow(child);
                        if ((childWindow != null) && !(childWindow instanceof XWindowPeer)) {
                            continue;
                        } else {
                            v.add(child);
                        }
                        if (childWindow instanceof XWindowPeer) {
                            XWindowPeer np = (XWindowPeer)childWindow;
                            javaToplevels.add(np);
                            int k = 0;
                            XWindowPeer toCheck = javaToplevels.get(k);
                            while (toCheck != np) {
                                XWindowPeer toCheckOwnerPeer = toCheck.getOwnerPeer();
                                if (toCheckOwnerPeer == np) {
                                    javaToplevels.remove(k);
                                    javaToplevels.add(toCheck);
                                } else {
                                    k++;
                                }
                                toCheck = javaToplevels.get(k);
                            }
                        }
                    }
                }
            } finally {
                qt.dispose();
            }
        }
        return javaToplevels;
    }
    public void setModalBlocked(Dialog d, boolean blocked) {
        setModalBlocked(d, blocked, null);
    }
    public void setModalBlocked(Dialog d, boolean blocked,
                                Vector<XWindowPeer> javaToplevels)
    {
        XToolkit.awtLock();
        try {
            synchronized(getStateLock()) {
                XDialogPeer blockerPeer = (XDialogPeer) AWTAccessor.getComponentAccessor().getPeer(d);
                if (blocked) {
                    log.fine("{0} is blocked by {1}", this, blockerPeer);
                    modalBlocker = d;
                    if (isReparented() || XWM.isNonReparentingWM()) {
                        addToTransientFors(blockerPeer, javaToplevels);
                    } else {
                        delayedModalBlocking = true;
                    }
                } else {
                    if (d != modalBlocker) {
                        throw new IllegalStateException("Trying to unblock window blocked by another dialog");
                    }
                    modalBlocker = null;
                    if (isReparented() || XWM.isNonReparentingWM()) {
                        removeFromTransientFors();
                    } else {
                        delayedModalBlocking = false;
                    }
                }
                updateTransientFor();
            }
        } finally {
            XToolkit.awtUnlock();
        }
    }
    static void setToplevelTransientFor(XWindowPeer window, XWindowPeer transientForWindow,
                                                boolean updateChain, boolean allStates)
    {
        if ((window == null) || (transientForWindow == null)) {
            return;
        }
        if (updateChain) {
            window.prevTransientFor = transientForWindow;
            transientForWindow.nextTransientFor = window;
        }
        if (window.curRealTransientFor == transientForWindow) {
            return;
        }
        if (!allStates && (window.getWMState() != transientForWindow.getWMState())) {
            return;
        }
        if (window.getScreenNumber() != transientForWindow.getScreenNumber()) {
            return;
        }
        long bpw = window.getWindow();
        while (!XlibUtil.isToplevelWindow(bpw) && !XlibUtil.isXAWTToplevelWindow(bpw)) {
            bpw = XlibUtil.getParentWindow(bpw);
        }
        long tpw = transientForWindow.getWindow();
        while (!XlibUtil.isToplevelWindow(tpw) && !XlibUtil.isXAWTToplevelWindow(tpw)) {
            tpw = XlibUtil.getParentWindow(tpw);
        }
        XlibWrapper.XSetTransientFor(XToolkit.getDisplay(), bpw, tpw);
        window.curRealTransientFor = transientForWindow;
    }
    void updateTransientFor() {
        int state = getWMState();
        XWindowPeer p = prevTransientFor;
        while ((p != null) && ((p.getWMState() != state) || (p.getScreenNumber() != getScreenNumber()))) {
            p = p.prevTransientFor;
        }
        if (p != null) {
            setToplevelTransientFor(this, p, false, false);
        } else {
            restoreTransientFor(this);
        }
        XWindowPeer n = nextTransientFor;
        while ((n != null) && ((n.getWMState() != state) || (n.getScreenNumber() != getScreenNumber()))) {
            n = n.nextTransientFor;
        }
        if (n != null) {
            setToplevelTransientFor(n, this, false, false);
        }
    }
    private static void removeTransientForHint(XWindowPeer window) {
        XAtom XA_WM_TRANSIENT_FOR = XAtom.get(XAtom.XA_WM_TRANSIENT_FOR);
        long bpw = window.getWindow();
        while (!XlibUtil.isToplevelWindow(bpw) && !XlibUtil.isXAWTToplevelWindow(bpw)) {
            bpw = XlibUtil.getParentWindow(bpw);
        }
        XlibWrapper.XDeleteProperty(XToolkit.getDisplay(), bpw, XA_WM_TRANSIENT_FOR.getAtom());
        window.curRealTransientFor = null;
    }
    private void addToTransientFors(XDialogPeer blockerPeer) {
        addToTransientFors(blockerPeer, null);
    }
    private void addToTransientFors(XDialogPeer blockerPeer, Vector<XWindowPeer> javaToplevels)
    {
        XWindowPeer blockerChain = blockerPeer;
        while (blockerChain.prevTransientFor != null) {
            blockerChain = blockerChain.prevTransientFor;
        }
        XWindowPeer thisChain = this;
        while (thisChain.prevTransientFor != null) {
            thisChain = thisChain.prevTransientFor;
        }
        if (blockerChain == blockerPeer) {
            setToplevelTransientFor(blockerPeer, this, true, false);
        } else {
            if (javaToplevels == null) {
                javaToplevels = collectJavaToplevels();
            }
            XWindowPeer mergedChain = null;
            for (XWindowPeer w : javaToplevels) {
                XWindowPeer prevMergedChain = mergedChain;
                if (w == thisChain) {
                    if (thisChain == this) {
                        if (prevMergedChain != null) {
                            setToplevelTransientFor(this, prevMergedChain, true, false);
                        }
                        setToplevelTransientFor(blockerChain, this, true, false);
                        break;
                    } else {
                        mergedChain = thisChain;
                        thisChain = thisChain.nextTransientFor;
                    }
                } else if (w == blockerChain) {
                    mergedChain = blockerChain;
                    blockerChain = blockerChain.nextTransientFor;
                } else {
                    continue;
                }
                if (prevMergedChain == null) {
                    mergedChain.prevTransientFor = null;
                } else {
                    setToplevelTransientFor(mergedChain, prevMergedChain, true, false);
                    mergedChain.updateTransientFor();
                }
                if (blockerChain == blockerPeer) {
                    setToplevelTransientFor(thisChain, mergedChain, true, false);
                    setToplevelTransientFor(blockerChain, this, true, false);
                    break;
                }
            }
        }
        XToolkit.XSync();
    }
    static void restoreTransientFor(XWindowPeer window) {
        XWindowPeer ownerPeer = window.getOwnerPeer();
        if (ownerPeer != null) {
            setToplevelTransientFor(window, ownerPeer, false, true);
        } else {
            removeTransientForHint(window);
        }
    }
    private void removeFromTransientFors() {
        XWindowPeer thisChain = this;
        XWindowPeer otherChain = nextTransientFor;
        Set<XWindowPeer> thisChainBlockers = new HashSet<XWindowPeer>();
        thisChainBlockers.add(this);
        XWindowPeer chainToSplit = prevTransientFor;
        while (chainToSplit != null) {
            XWindowPeer blocker = (XWindowPeer) AWTAccessor.getComponentAccessor().getPeer(chainToSplit.modalBlocker);
            if (thisChainBlockers.contains(blocker)) {
                setToplevelTransientFor(thisChain, chainToSplit, true, false);
                thisChain = chainToSplit;
                thisChainBlockers.add(chainToSplit);
            } else {
                setToplevelTransientFor(otherChain, chainToSplit, true, false);
                otherChain = chainToSplit;
            }
            chainToSplit = chainToSplit.prevTransientFor;
        }
        restoreTransientFor(thisChain);
        thisChain.prevTransientFor = null;
        restoreTransientFor(otherChain);
        otherChain.prevTransientFor = null;
        nextTransientFor = null;
        XToolkit.XSync();
    }
    boolean isModalBlocked() {
        return modalBlocker != null;
    }
    static Window getDecoratedOwner(Window window) {
        while ((null != window) && !(window instanceof Frame || window instanceof Dialog)) {
            window = (Window) AWTAccessor.getComponentAccessor().getParent(window);
        }
        return window;
    }
    public boolean requestWindowFocus(XWindowPeer actualFocusedWindow) {
        setActualFocusedWindow(actualFocusedWindow);
        return requestWindowFocus();
    }
    public boolean requestWindowFocus() {
        return requestWindowFocus(0, false);
    }
    public boolean requestWindowFocus(long time, boolean timeProvided) {
        focusLog.fine("Request for window focus");
        Window ownerWindow  = XWindowPeer.getDecoratedOwner((Window)target);
        Window focusedWindow = XKeyboardFocusManagerPeer.getCurrentNativeFocusedWindow();
        Window activeWindow = XWindowPeer.getDecoratedOwner(focusedWindow);
        if (isWMStateNetHidden()) {
            focusLog.fine("The window is unmapped, so rejecting the request");
            return false;
        }
        if (activeWindow == ownerWindow) {
            focusLog.fine("Parent window is active - generating focus for this window");
            handleWindowFocusInSync(-1);
            return true;
        }
        focusLog.fine("Parent window is not active");
        XDecoratedPeer wpeer = (XDecoratedPeer)AWTAccessor.getComponentAccessor().getPeer(ownerWindow);
        if (wpeer != null && wpeer.requestWindowFocus(this, time, timeProvided)) {
            focusLog.fine("Parent window accepted focus request - generating focus for this window");
            return true;
        }
        focusLog.fine("Denied - parent window is not active and didn't accept focus request");
        return false;
    }
    void setActualFocusedWindow(XWindowPeer actualFocusedWindow) {
    }
    private void applyWindowType() {
        XNETProtocol protocol = XWM.getWM().getNETProtocol();
        if (protocol == null) {
            return;
        }
        XAtom typeAtom = null;
        switch (getWindowType())
        {
            case NORMAL:
                typeAtom = protocol.XA_NET_WM_WINDOW_TYPE_NORMAL;
                break;
            case UTILITY:
                typeAtom = protocol.XA_NET_WM_WINDOW_TYPE_UTILITY;
                break;
            case POPUP:
                typeAtom = protocol.XA_NET_WM_WINDOW_TYPE_POPUP_MENU;
                break;
        }
        if (typeAtom != null) {
            XAtomList wtype = new XAtomList();
            wtype.add(typeAtom);
            protocol.XA_NET_WM_WINDOW_TYPE.
                setAtomListProperty(getWindow(), wtype);
        } else {
            protocol.XA_NET_WM_WINDOW_TYPE.
                DeleteProperty(getWindow());
        }
    }
    @Override
    public void xSetVisible(boolean visible) {
        if (log.isLoggable(PlatformLogger.FINE)) log.fine("Setting visible on " + this + " to " + visible);
        XToolkit.awtLock();
        try {
            this.visible = visible;
            if (visible) {
                applyWindowType();
                XlibWrapper.XMapRaised(XToolkit.getDisplay(), getWindow());
            } else {
                XlibWrapper.XUnmapWindow(XToolkit.getDisplay(), getWindow());
            }
            XlibWrapper.XFlush(XToolkit.getDisplay());
        }
        finally {
            XToolkit.awtUnlock();
        }
    }
    private int dropTargetCount = 0;
    public void addDropTarget() {
        XToolkit.awtLock();
        try {
            if (dropTargetCount == 0) {
                long window = getWindow();
                if (window != 0) {
                    XDropTargetRegistry.getRegistry().registerDropSite(window);
                }
            }
            dropTargetCount++;
        } finally {
            XToolkit.awtUnlock();
        }
    }
    public void removeDropTarget() {
        XToolkit.awtLock();
        try {
            dropTargetCount--;
            if (dropTargetCount == 0) {
                long window = getWindow();
                if (window != 0) {
                    XDropTargetRegistry.getRegistry().unregisterDropSite(window);
                }
            }
        } finally {
            XToolkit.awtUnlock();
        }
    }
    void addRootPropertyEventDispatcher() {
        if( rootPropertyEventDispatcher == null ) {
            rootPropertyEventDispatcher = new XEventDispatcher() {
                public void dispatchEvent(XEvent ev) {
                    if( ev.get_type() == XConstants.PropertyNotify ) {
                        handleRootPropertyNotify( ev );
                    }
                }
            };
            XlibWrapper.XSelectInput( XToolkit.getDisplay(),
                                      XToolkit.getDefaultRootWindow(),
                                      XConstants.PropertyChangeMask);
            XToolkit.addEventDispatcher(XToolkit.getDefaultRootWindow(),
                                                rootPropertyEventDispatcher);
        }
    }
    void removeRootPropertyEventDispatcher() {
        if( rootPropertyEventDispatcher != null ) {
            XToolkit.removeEventDispatcher(XToolkit.getDefaultRootWindow(),
                                                rootPropertyEventDispatcher);
            rootPropertyEventDispatcher = null;
        }
    }
    public void updateFocusableWindowState() {
        cachedFocusableWindow = isFocusableWindow();
    }
    XAtom XA_NET_WM_STATE;
    XAtomList net_wm_state;
    public XAtomList getNETWMState() {
        if (net_wm_state == null) {
            net_wm_state = XA_NET_WM_STATE.getAtomListPropertyList(this);
        }
        return net_wm_state;
    }
    public void setNETWMState(XAtomList state) {
        net_wm_state = state;
        if (state != null) {
            XA_NET_WM_STATE.setAtomListProperty(this, state);
        }
    }
    public PropMwmHints getMWMHints() {
        if (mwm_hints == null) {
            mwm_hints = new PropMwmHints();
            if (!XWM.XA_MWM_HINTS.getAtomData(getWindow(), mwm_hints.pData, MWMConstants.PROP_MWM_HINTS_ELEMENTS)) {
                mwm_hints.zero();
            }
        }
        return mwm_hints;
    }
    public void setMWMHints(PropMwmHints hints) {
        mwm_hints = hints;
        if (hints != null) {
            XWM.XA_MWM_HINTS.setAtomData(getWindow(), mwm_hints.pData, MWMConstants.PROP_MWM_HINTS_ELEMENTS);
        }
    }
    protected void updateDropTarget() {
        XToolkit.awtLock();
        try {
            if (dropTargetCount > 0) {
                long window = getWindow();
                if (window != 0) {
                    XDropTargetRegistry.getRegistry().unregisterDropSite(window);
                    XDropTargetRegistry.getRegistry().registerDropSite(window);
                }
            }
        } finally {
            XToolkit.awtUnlock();
        }
    }
    public void setGrab(boolean grab) {
        this.grab = grab;
        if (grab) {
            pressTarget = this;
            grabInput();
        } else {
            ungrabInput();
        }
    }
    public boolean isGrabbed() {
        return grab && XAwtState.getGrabWindow() == this;
    }
    public void handleXCrossingEvent(XEvent xev) {
        XCrossingEvent xce = xev.get_xcrossing();
        if (grabLog.isLoggable(PlatformLogger.FINE)) {
            grabLog.fine("{0}, when grabbed {1}, contains {2}",
                         xce, isGrabbed(), containsGlobal(xce.get_x_root(), xce.get_y_root()));
        }
        if (isGrabbed()) {
            XBaseWindow target = XToolkit.windowToXWindow(xce.get_window());
            grabLog.finer("  -  Grab event target {0}", target);
            if (target != null && target != this) {
                target.dispatchEvent(xev);
                return;
            }
        }
        super.handleXCrossingEvent(xev);
    }
    public void handleMotionNotify(XEvent xev) {
        XMotionEvent xme = xev.get_xmotion();
        if (grabLog.isLoggable(PlatformLogger.FINE)) {
            grabLog.finer("{0}, when grabbed {1}, contains {2}",
                          xme, isGrabbed(), containsGlobal(xme.get_x_root(), xme.get_y_root()));
        }
        if (isGrabbed()) {
            boolean dragging = false;
            final int buttonsNumber = ((SunToolkit)(Toolkit.getDefaultToolkit())).getNumberOfButtons();
            for (int i = 0; i < buttonsNumber; i++){
                if ((i != 4) && (i != 5)){
                    dragging = dragging || ((xme.get_state() & XConstants.buttonsMask[i]) != 0);
                }
            }
            XBaseWindow target = XToolkit.windowToXWindow(xme.get_window());
            if (dragging && pressTarget != target) {
                target = pressTarget.isVisible() ? pressTarget : this;
                xme.set_window(target.getWindow());
                Point localCoord = target.toLocal(xme.get_x_root(), xme.get_y_root());
                xme.set_x(localCoord.x);
                xme.set_y(localCoord.y);
            }
            grabLog.finer("  -  Grab event target {0}", target);
            if (target != null) {
                if (target != getContentXWindow() && target != this) {
                    target.dispatchEvent(xev);
                    return;
                }
            }
            if (!containsGlobal(xme.get_x_root(), xme.get_y_root()) && !dragging) {
                return;
            }
        }
        super.handleMotionNotify(xev);
    }
    private XBaseWindow pressTarget = this;
    public void handleButtonPressRelease(XEvent xev) {
        XButtonEvent xbe = xev.get_xbutton();
        if (xbe.get_button() > SunToolkit.MAX_BUTTONS_SUPPORTED) {
            return;
        }
        if (grabLog.isLoggable(PlatformLogger.FINE)) {
            grabLog.fine("{0}, when grabbed {1}, contains {2} ({3}, {4}, {5}x{6})",
                         xbe, isGrabbed(), containsGlobal(xbe.get_x_root(), xbe.get_y_root()), getAbsoluteX(), getAbsoluteY(), getWidth(), getHeight());
        }
        if (isGrabbed()) {
            XBaseWindow target = XToolkit.windowToXWindow(xbe.get_window());
            try {
                grabLog.finer("  -  Grab event target {0} (press target {1})", target, pressTarget);
                if (xbe.get_type() == XConstants.ButtonPress
                    && xbe.get_button() == XConstants.buttons[0])
                {
                    pressTarget = target;
                } else if (xbe.get_type() == XConstants.ButtonRelease
                           && xbe.get_button() == XConstants.buttons[0]
                           && pressTarget != target)
                {
                    target = pressTarget.isVisible() ? pressTarget : this;
                    xbe.set_window(target.getWindow());
                    Point localCoord = target.toLocal(xbe.get_x_root(), xbe.get_y_root());
                    xbe.set_x(localCoord.x);
                    xbe.set_y(localCoord.y);
                    pressTarget = this;
                }
                if (target != null && target != getContentXWindow() && target != this) {
                    target.dispatchEvent(xev);
                    return;
                }
            } finally {
                if (target != null) {
                    if ((target == this || target == getContentXWindow()) && !containsGlobal(xbe.get_x_root(), xbe.get_y_root())) {
                        if (xbe.get_type() == XConstants.ButtonPress) {
                            grabLog.fine("Generating UngrabEvent on {0} because not inside of shell", this);
                            postEventToEventQueue(new sun.awt.UngrabEvent(getEventSource()));
                            return;
                        }
                    }
                    XWindowPeer toplevel = target.getToplevelXWindow();
                    if (toplevel != null) {
                        Window w = (Window)toplevel.target;
                        while (w != null && toplevel != this && !(toplevel instanceof XDialogPeer)) {
                            w = (Window) AWTAccessor.getComponentAccessor().getParent(w);
                            if (w != null) {
                                toplevel = (XWindowPeer) AWTAccessor.getComponentAccessor().getPeer(w);
                            }
                        }
                        if (w == null || (w != this.target && w instanceof Dialog)) {
                            grabLog.fine("Generating UngrabEvent on {0} because hierarchy ended", this);
                            postEventToEventQueue(new sun.awt.UngrabEvent(getEventSource()));
                        }
                    } else {
                        grabLog.fine("Generating UngrabEvent on {0} because toplevel is null", this);
                        postEventToEventQueue(new sun.awt.UngrabEvent(getEventSource()));
                        return;
                    }
                } else {
                    grabLog.fine("Generating UngrabEvent on because target is null {0}", this);
                    postEventToEventQueue(new sun.awt.UngrabEvent(getEventSource()));
                    return;
                }
            }
        }
        super.handleButtonPressRelease(xev);
    }
    public void print(Graphics g) {
        Shape shape = AWTAccessor.getWindowAccessor().getShape((Window)target);
        if (shape != null) {
            g.setClip(shape);
        }
        super.print(g);
    }
    @Override
    public void setOpacity(float opacity) {
        final long maxOpacity = 0xffffffffl;
        long iOpacity = (long)(opacity * maxOpacity);
        if (iOpacity < 0) {
            iOpacity = 0;
        }
        if (iOpacity > maxOpacity) {
            iOpacity = maxOpacity;
        }
        XAtom netWmWindowOpacityAtom = XAtom.get("_NET_WM_WINDOW_OPACITY");
        if (iOpacity == maxOpacity) {
            netWmWindowOpacityAtom.DeleteProperty(getWindow());
        } else {
            netWmWindowOpacityAtom.setCard32Property(getWindow(), iOpacity);
        }
    }
    @Override
    public void setOpaque(boolean isOpaque) {
    }
    @Override
    public void updateWindow() {
    }
}
