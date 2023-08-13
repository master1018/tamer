abstract class XDecoratedPeer extends XWindowPeer {
    private static final PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.XDecoratedPeer");
    private static final PlatformLogger insLog = PlatformLogger.getLogger("sun.awt.X11.insets.XDecoratedPeer");
    private static final PlatformLogger focusLog = PlatformLogger.getLogger("sun.awt.X11.focus.XDecoratedPeer");
    private static final PlatformLogger iconLog = PlatformLogger.getLogger("sun.awt.X11.icon.XDecoratedPeer");
    boolean configure_seen;
    boolean insets_corrected;
    XIconWindow iconWindow;
    WindowDimensions dimensions;
    XContentWindow content;
    Insets currentInsets;
    XFocusProxyWindow focusProxy;
    XDecoratedPeer(Window target) {
        super(target);
    }
    XDecoratedPeer(XCreateWindowParams params) {
        super(params);
    }
    public long getShell() {
        return window;
    }
    public long getContentWindow() {
        return (content == null) ? window : content.getWindow();
    }
    void preInit(XCreateWindowParams params) {
        super.preInit(params);
        winAttr.initialFocus = true;
        currentInsets = new Insets(0,0,0,0);
        applyGuessedInsets();
        Rectangle bounds = (Rectangle)params.get(BOUNDS);
        dimensions = new WindowDimensions(bounds, getRealInsets(), false);
        params.put(BOUNDS, dimensions.getClientRect());
        insLog.fine("Initial dimensions {0}", dimensions);
        Long eventMask = (Long)params.get(EVENT_MASK);
        params.add(EVENT_MASK, Long.valueOf(eventMask.longValue() & ~(XConstants.FocusChangeMask | XConstants.KeyPressMask | XConstants.KeyReleaseMask)));
    }
    void postInit(XCreateWindowParams params) {
        updateSizeHints(dimensions);
        super.postInit(params);
        initResizability();
        XWM.requestWMExtents(getWindow());
        content = XContentWindow.createContent(this);
        if (warningWindow != null) {
            warningWindow.toFront();
        }
        focusProxy = createFocusProxy();
    }
    void setIconHints(java.util.List<XIconInfo> icons) {
        if (!XWM.getWM().setNetWMIcon(this, icons)) {
            if (icons.size() > 0) {
                if (iconWindow == null) {
                    iconWindow = new XIconWindow(this);
                }
                iconWindow.setIconImages(icons);
            }
        }
    }
    public void updateMinimumSize() {
        super.updateMinimumSize();
        updateMinSizeHints();
    }
    private void updateMinSizeHints() {
        if (isResizable()) {
            Dimension minimumSize = getTargetMinimumSize();
            if (minimumSize != null) {
                Insets insets = getRealInsets();
                int minWidth = minimumSize.width - insets.left - insets.right;
                int minHeight = minimumSize.height - insets.top - insets.bottom;
                if (minWidth < 0) minWidth = 0;
                if (minHeight < 0) minHeight = 0;
                setSizeHints(XUtilConstants.PMinSize | (isLocationByPlatform()?0:(XUtilConstants.PPosition | XUtilConstants.USPosition)),
                             getX(), getY(), minWidth, minHeight);
                if (isVisible()) {
                    Rectangle bounds = getShellBounds();
                    int nw = (bounds.width < minWidth) ? minWidth : bounds.width;
                    int nh = (bounds.height < minHeight) ? minHeight : bounds.height;
                    if (nw != bounds.width || nh != bounds.height) {
                        setShellSize(new Rectangle(0, 0, nw, nh));
                    }
                }
            } else {
                boolean isMinSizeSet = isMinSizeSet();
                XWM.removeSizeHints(this, XUtilConstants.PMinSize);
                if (isMinSizeSet && isShowing() && XWM.needRemap(this)) {
                    xSetVisible(false);
                    XToolkit.XSync();
                    xSetVisible(true);
                }
            }
        }
    }
    XFocusProxyWindow createFocusProxy() {
        return new XFocusProxyWindow(this);
    }
    protected XAtomList getWMProtocols() {
        XAtomList protocols = super.getWMProtocols();
        protocols.add(wm_delete_window);
        protocols.add(wm_take_focus);
        return protocols;
    }
    public Graphics getGraphics() {
        AWTAccessor.ComponentAccessor compAccessor = AWTAccessor.getComponentAccessor();
        return getGraphics(content.surfaceData,
                           compAccessor.getForeground(target),
                           compAccessor.getBackground(target),
                           compAccessor.getFont(target));
    }
    public void setTitle(String title) {
        if (log.isLoggable(PlatformLogger.FINE)) log.fine("Title is " + title);
        winAttr.title = title;
        updateWMName();
    }
    protected String getWMName() {
        if (winAttr.title == null || winAttr.title.trim().equals("")) {
            return " ";
        } else {
            return winAttr.title;
        }
    }
    void updateWMName() {
        super.updateWMName();
        String name = getWMName();
        XToolkit.awtLock();
        try {
            if (name == null || name.trim().equals("")) {
                name = "Java";
            }
            XAtom iconNameAtom = XAtom.get(XAtom.XA_WM_ICON_NAME);
            iconNameAtom.setProperty(getWindow(), name);
            XAtom netIconNameAtom = XAtom.get("_NET_WM_ICON_NAME");
            netIconNameAtom.setPropertyUTF8(getWindow(), name);
        } finally {
            XToolkit.awtUnlock();
        }
    }
    public void handleIconify() {
        postEvent(new WindowEvent((Window)target, WindowEvent.WINDOW_ICONIFIED));
    }
    public void handleDeiconify() {
        postEvent(new WindowEvent((Window)target, WindowEvent.WINDOW_DEICONIFIED));
    }
    public void handleFocusEvent(XEvent xev) {
        super.handleFocusEvent(xev);
        XFocusChangeEvent xfe = xev.get_xfocus();
        focusLog.finer("Received focus event on shell: " + xfe);
   }
    protected boolean isInitialReshape() {
        return false;
    }
    private static Insets difference(Insets i1, Insets i2) {
        return new Insets(i1.top-i2.top, i1.left - i2.left, i1.bottom-i2.bottom, i1.right-i2.right);
    }
    private static boolean isNull(Insets i) {
        return (i == null) || ((i.left | i.top | i.right | i.bottom) == 0);
    }
    private static Insets copy(Insets i) {
        return new Insets(i.top, i.left, i.bottom, i.right);
    }
    private Insets wm_set_insets;
    private Insets getWMSetInsets(XAtom changedAtom) {
        if (isEmbedded()) {
            return null;
        }
        if (wm_set_insets != null) {
            return wm_set_insets;
        }
        if (changedAtom == null) {
            wm_set_insets = XWM.getInsetsFromExtents(getWindow());
        } else {
            wm_set_insets = XWM.getInsetsFromProp(getWindow(), changedAtom);
        }
        insLog.finer("FRAME_EXTENTS: {0}", wm_set_insets);
        if (wm_set_insets != null) {
            wm_set_insets = copy(wm_set_insets);
        }
        return wm_set_insets;
    }
    private void resetWMSetInsets() {
        wm_set_insets = null;
    }
    public void handlePropertyNotify(XEvent xev) {
        super.handlePropertyNotify(xev);
        XPropertyEvent ev = xev.get_xproperty();
        if (ev.get_atom() == XWM.XA_KDE_NET_WM_FRAME_STRUT.getAtom()
            || ev.get_atom() == XWM.XA_NET_FRAME_EXTENTS.getAtom())
        {
            getWMSetInsets(XAtom.get(ev.get_atom()));
        }
    }
    long reparent_serial = 0;
    public void handleReparentNotifyEvent(XEvent xev) {
        XReparentEvent  xe = xev.get_xreparent();
        if (insLog.isLoggable(PlatformLogger.FINE)) insLog.fine(xe.toString());
        reparent_serial = xe.get_serial();
        XToolkit.awtLock();
        try {
            long root = XlibWrapper.RootWindow(XToolkit.getDisplay(), getScreenNumber());
            if (isEmbedded()) {
                setReparented(true);
                insets_corrected = true;
                return;
            }
            Component t = (Component)target;
            if (getDecorations() == XWindowAttributesData.AWT_DECOR_NONE) {
                setReparented(true);
                insets_corrected = true;
                reshape(dimensions, SET_SIZE, false);
            } else if (xe.get_parent() == root) {
                configure_seen = false;
                insets_corrected = false;
                if (isVisible()) { 
                    XWM.getWM().unshadeKludge(this);
                    insLog.fine("- WM exited");
                } else {
                    insLog.fine(" - reparent due to hide");
                }
            } else { 
                setReparented(true);
                insets_corrected = false;
                Insets correctWM = getWMSetInsets(null);
                if (correctWM != null) {
                    insLog.finer("wm-provided insets {0}", correctWM);
                    Insets dimInsets = dimensions.getInsets();
                    if (correctWM.equals(dimInsets)) {
                        insLog.finer("Insets are the same as estimated - no additional reshapes necessary");
                        no_reparent_artifacts = true;
                        insets_corrected = true;
                        applyGuessedInsets();
                        return;
                    }
                } else {
                    correctWM = XWM.getWM().getInsets(this, xe.get_window(), xe.get_parent());
                    if (correctWM != null) {
                        insLog.finer("correctWM {0}", correctWM);
                    } else {
                        insLog.finer("correctWM insets are not available, waiting for configureNotify");
                    }
                }
                if (correctWM != null) {
                    handleCorrectInsets(correctWM);
                }
            }
        } finally {
            XToolkit.awtUnlock();
        }
    }
    protected void handleCorrectInsets(Insets correctWM) {
        XToolkit.awtLock();
        try {
            Insets correction = difference(correctWM, currentInsets);
            insLog.finest("Corrention {0}", correction);
            if (!isNull(correction)) {
                currentInsets = copy(correctWM);
                applyGuessedInsets();
                updateMinSizeHints();
            }
            if (insLog.isLoggable(PlatformLogger.FINER)) insLog.finer("Dimensions before reparent: " + dimensions);
            dimensions.setInsets(getRealInsets());
            insets_corrected = true;
            if (isMaximized()) {
                return;
            }
            if ((getHints().get_flags() & (XUtilConstants.USPosition | XUtilConstants.PPosition)) != 0) {
                reshape(dimensions, SET_BOUNDS, false);
            } else {
                reshape(dimensions, SET_SIZE, false);
            }
        } finally {
            XToolkit.awtUnlock();
        }
    }
    public void handleMoved(WindowDimensions dims) {
        Point loc = dims.getLocation();
        AWTAccessor.getComponentAccessor().setLocation((Component)target, loc.x, loc.y);
        postEvent(new ComponentEvent(target, ComponentEvent.COMPONENT_MOVED));
    }
    protected Insets guessInsets() {
        if (isEmbedded() || isTargetUndecorated()) {
            return new Insets(0, 0, 0, 0);
        } else {
            if (!isNull(currentInsets)) {
                return copy(currentInsets);
            } else {
                Insets res = getWMSetInsets(null);
                if (res == null) {
                    res = XWM.getWM().guessInsets(this);
                }
                return res;
            }
        }
    }
    private void applyGuessedInsets() {
        Insets guessed = guessInsets();
        currentInsets = copy(guessed);
    }
    public void revalidate() {
        XToolkit.executeOnEventHandlerThread(target, new Runnable() {
                public void run() {
                    target.invalidate();
                    target.validate();
                }
            });
    }
    Insets getRealInsets() {
        if (isNull(currentInsets)) {
            applyGuessedInsets();
        }
        return currentInsets;
    }
    public Insets getInsets() {
        Insets in = copy(getRealInsets());
        in.top += getMenuBarHeight();
        if (insLog.isLoggable(PlatformLogger.FINEST)) {
            insLog.finest("Get insets returns {0}", in);
        }
        return in;
    }
    boolean gravityBug() {
        return XWM.configureGravityBuggy();
    }
    int getInputMethodHeight() {
        return 0;
    }
    void updateSizeHints(WindowDimensions dims) {
        Rectangle rec = dims.getClientRect();
        checkShellRect(rec);
        updateSizeHints(rec.x, rec.y, rec.width, rec.height);
    }
    void updateSizeHints() {
        updateSizeHints(dimensions);
    }
    public void reshape(WindowDimensions newDimensions, int op,
                        boolean userReshape)
    {
        if (insLog.isLoggable(PlatformLogger.FINE)) {
            insLog.fine("Reshaping " + this + " to " + newDimensions + " op " + op + " user reshape " + userReshape);
        }
        if (userReshape) {
            Rectangle newBounds = newDimensions.getBounds();
            Insets insets = newDimensions.getInsets();
            if (newDimensions.isClientSizeSet()) {
                newBounds = new Rectangle(newBounds.x, newBounds.y,
                                          newBounds.width - insets.left - insets.right,
                                          newBounds.height - insets.top - insets.bottom);
            }
            newDimensions = new WindowDimensions(newBounds, insets, newDimensions.isClientSizeSet());
        }
        XToolkit.awtLock();
        try {
            if (!isReparented() || !isVisible()) {
                insLog.fine("- not reparented({0}) or not visible({1}), default reshape",
                           Boolean.valueOf(isReparented()), Boolean.valueOf(visible));
                Point oldLocation = getLocation();
                Point newLocation = new Point(AWTAccessor.getComponentAccessor().getX((Component)target),
                                              AWTAccessor.getComponentAccessor().getY((Component)target));
                if (!newLocation.equals(oldLocation)) {
                    handleMoved(newDimensions);
                }
                dimensions = new WindowDimensions(newDimensions);
                updateSizeHints(dimensions);
                Rectangle client = dimensions.getClientRect();
                checkShellRect(client);
                setShellBounds(client);
                if (content != null &&
                    !content.getSize().equals(newDimensions.getSize()))
                {
                    reconfigureContentWindow(newDimensions);
                }
                return;
            }
            int wm = XWM.getWMID();
            updateChildrenSizes();
            applyGuessedInsets();
            Rectangle shellRect = newDimensions.getClientRect();
            if (gravityBug()) {
                Insets in = newDimensions.getInsets();
                shellRect.translate(in.left, in.top);
            }
            if ((op & NO_EMBEDDED_CHECK) == 0 && isEmbedded()) {
                shellRect.setLocation(0, 0);
            }
            checkShellRectSize(shellRect);
            if (!isEmbedded()) {
                checkShellRectPos(shellRect);
            }
            op = op & ~NO_EMBEDDED_CHECK;
            if (op == SET_LOCATION) {
                setShellPosition(shellRect);
            } else if (isResizable()) {
                if (op == SET_BOUNDS) {
                    setShellBounds(shellRect);
                } else {
                    setShellSize(shellRect);
                }
            } else {
                XWM.setShellNotResizable(this, newDimensions, shellRect, true);
                if (op == SET_BOUNDS) {
                    setShellPosition(shellRect);
                }
            }
            reconfigureContentWindow(newDimensions);
        } finally {
            XToolkit.awtUnlock();
        }
    }
    private void reshape(int x, int y, int width, int height, int operation,
                         boolean userReshape)
    {
        Rectangle newRec;
        boolean setClient = false;
        WindowDimensions dims = new WindowDimensions(dimensions);
        switch (operation & (~NO_EMBEDDED_CHECK)) {
          case SET_LOCATION:
              dims.setLocation(x, y);
              break;
          case SET_SIZE:
              dims.setSize(width, height);
              break;
          case SET_CLIENT_SIZE: {
              Insets in = currentInsets;
              width -= in.left+in.right;
              height -= in.top+in.bottom;
              dims.setClientSize(width, height);
              break;
          }
          case SET_BOUNDS:
          default:
              dims.setLocation(x, y);
              dims.setSize(width, height);
              break;
        }
        if (insLog.isLoggable(PlatformLogger.FINE))
            insLog.fine("For the operation {0} new dimensions are {1}",
                        operationToString(operation), dims);
        reshape(dims, operation, userReshape);
    }
    abstract boolean isTargetUndecorated();
    public void setBounds(int x, int y, int width, int height, int op) {
        reshape(x, y, width, height, op, true);
        validateSurface();
    }
    void reconfigureContentWindow(WindowDimensions dims) {
        if (content == null) {
            insLog.fine("WARNING: Content window is null");
            return;
        }
        content.setContentBounds(dims);
    }
    boolean no_reparent_artifacts = false;
    public void handleConfigureNotifyEvent(XEvent xev) {
        assert (SunToolkit.isAWTLockHeldByCurrentThread());
        XConfigureEvent xe = xev.get_xconfigure();
        insLog.fine("Configure notify {0}", xe);
        if (isReparented()) {
            configure_seen = true;
        }
        if (!isMaximized()
            && (xe.get_serial() == reparent_serial || xe.get_window() != getShell())
            && !no_reparent_artifacts)
        {
            insLog.fine("- reparent artifact, skipping");
            return;
        }
        no_reparent_artifacts = false;
        if (!isVisible() && XWM.getWMID() != XWM.NO_WM) {
            insLog.fine(" - not visible, skipping");
            return;
        }
        int runningWM = XWM.getWMID();
        if (insLog.isLoggable(PlatformLogger.FINE)) {
            insLog.fine("reparented={0}, visible={1}, WM={2}, decorations={3}",
                        isReparented(), isVisible(), runningWM, getDecorations());
        }
        if (!isReparented() && isVisible() && runningWM != XWM.NO_WM
                &&  !XWM.isNonReparentingWM()
                && getDecorations() != XWindowAttributesData.AWT_DECOR_NONE) {
            insLog.fine("- visible but not reparented, skipping");
            return;
        }
        if (!insets_corrected && getDecorations() != XWindowAttributesData.AWT_DECOR_NONE) {
            long parent = XlibUtil.getParentWindow(window);
            Insets correctWM = (parent != -1) ? XWM.getWM().getInsets(this, window, parent) : null;
            if (insLog.isLoggable(PlatformLogger.FINER)) {
                if (correctWM != null) {
                    insLog.finer("Configure notify - insets : " + correctWM);
                } else {
                    insLog.finer("Configure notify - insets are still not available");
                }
            }
            if (correctWM != null) {
                handleCorrectInsets(correctWM);
            } else {
                insets_corrected = true;
            }
        }
        updateChildrenSizes();
        Rectangle targetBounds = AWTAccessor.getComponentAccessor().getBounds((Component)target);
        Point newLocation = targetBounds.getLocation();
        if (xe.get_send_event() || runningWM == XWM.NO_WM || XWM.isNonReparentingWM()) {
            newLocation = new Point(xe.get_x() - currentInsets.left, xe.get_y() - currentInsets.top);
        } else {
            switch (XWM.getWMID()) {
                case XWM.CDE_WM:
                case XWM.MOTIF_WM:
                case XWM.METACITY_WM:
                case XWM.SAWFISH_WM:
                {
                    Point xlocation = queryXLocation();
                    if (log.isLoggable(PlatformLogger.FINE)) log.fine("New X location: {0}", xlocation);
                    if (xlocation != null) {
                        newLocation = xlocation;
                    }
                    break;
                }
                default:
                    break;
            }
        }
        WindowDimensions newDimensions =
                new WindowDimensions(newLocation,
                new Dimension(xe.get_width(), xe.get_height()),
                copy(currentInsets),
                true);
        insLog.finer("Insets are {0}, new dimensions {1}",
                     currentInsets, newDimensions);
        checkIfOnNewScreen(newDimensions.getBounds());
        Point oldLocation = getLocation();
        dimensions = newDimensions;
        if (!newLocation.equals(oldLocation)) {
            handleMoved(newDimensions);
        }
        reconfigureContentWindow(newDimensions);
        updateChildrenSizes();
        repositionSecurityWarning();
    }
    private void checkShellRectSize(Rectangle shellRect) {
        shellRect.width = Math.max(MIN_SIZE, shellRect.width);
        shellRect.height = Math.max(MIN_SIZE, shellRect.height);
    }
    private void checkShellRectPos(Rectangle shellRect) {
        int wm = XWM.getWMID();
        if (wm == XWM.MOTIF_WM || wm == XWM.CDE_WM) {
            if (shellRect.x == 0 && shellRect.y == 0) {
                shellRect.x = shellRect.y = 1;
            }
        }
    }
    private void checkShellRect(Rectangle shellRect) {
        checkShellRectSize(shellRect);
        checkShellRectPos(shellRect);
    }
    public void setShellBounds(Rectangle rec) {
        if (insLog.isLoggable(PlatformLogger.FINE)) insLog.fine("Setting shell bounds on " +
                                                                this + " to " + rec);
        XToolkit.awtLock();
        try {
            updateSizeHints(rec.x, rec.y, rec.width, rec.height);
            XlibWrapper.XResizeWindow(XToolkit.getDisplay(), getShell(), rec.width, rec.height);
            XlibWrapper.XMoveWindow(XToolkit.getDisplay(), getShell(), rec.x, rec.y);
        }
        finally {
            XToolkit.awtUnlock();
        }
    }
    public void setShellSize(Rectangle rec) {
        if (insLog.isLoggable(PlatformLogger.FINE)) insLog.fine("Setting shell size on " +
                                                                this + " to " + rec);
        XToolkit.awtLock();
        try {
            updateSizeHints(rec.x, rec.y, rec.width, rec.height);
            XlibWrapper.XResizeWindow(XToolkit.getDisplay(), getShell(), rec.width, rec.height);
        }
        finally {
            XToolkit.awtUnlock();
        }
    }
    public void setShellPosition(Rectangle rec) {
        if (insLog.isLoggable(PlatformLogger.FINE)) insLog.fine("Setting shell position on " +
                                                                this + " to " + rec);
        XToolkit.awtLock();
        try {
            updateSizeHints(rec.x, rec.y, rec.width, rec.height);
            XlibWrapper.XMoveWindow(XToolkit.getDisplay(), getShell(), rec.x, rec.y);
        }
        finally {
            XToolkit.awtUnlock();
        }
    }
    void initResizability() {
        setResizable(winAttr.initialResizability);
    }
    public void setResizable(boolean resizable) {
        int fs = winAttr.functions;
        if (!isResizable() && resizable) {
            currentInsets = new Insets(0, 0, 0, 0);
            resetWMSetInsets();
            if (!isEmbedded()) {
                setReparented(false);
            }
            winAttr.isResizable = resizable;
            if ((fs & MWMConstants.MWM_FUNC_ALL) != 0) {
                fs &= ~(MWMConstants.MWM_FUNC_RESIZE | MWMConstants.MWM_FUNC_MAXIMIZE);
            } else {
                fs |= (MWMConstants.MWM_FUNC_RESIZE | MWMConstants.MWM_FUNC_MAXIMIZE);
            }
            winAttr.functions = fs;
            XWM.setShellResizable(this);
        } else if (isResizable() && !resizable) {
            currentInsets = new Insets(0, 0, 0, 0);
            resetWMSetInsets();
            if (!isEmbedded()) {
                setReparented(false);
            }
            winAttr.isResizable = resizable;
            if ((fs & MWMConstants.MWM_FUNC_ALL) != 0) {
                fs |= (MWMConstants.MWM_FUNC_RESIZE | MWMConstants.MWM_FUNC_MAXIMIZE);
            } else {
                fs &= ~(MWMConstants.MWM_FUNC_RESIZE | MWMConstants.MWM_FUNC_MAXIMIZE);
            }
            winAttr.functions = fs;
            XWM.setShellNotResizable(this, dimensions, dimensions.getBounds(), false);
        }
    }
    Rectangle getShellBounds() {
        return dimensions.getClientRect();
    }
    public Rectangle getBounds() {
        return dimensions.getBounds();
    }
    public Dimension getSize() {
        return dimensions.getSize();
    }
    public int getX() {
        return dimensions.getLocation().x;
    }
    public int getY() {
        return dimensions.getLocation().y;
    }
    public Point getLocation() {
        return dimensions.getLocation();
    }
    public int getAbsoluteX() {
        return dimensions.getScreenBounds().x;
    }
    public int getAbsoluteY() {
        return dimensions.getScreenBounds().y;
    }
    public int getWidth() {
        return getSize().width;
    }
    public int getHeight() {
        return getSize().height;
    }
    final public WindowDimensions getDimensions() {
        return dimensions;
    }
    public Point getLocationOnScreen() {
        XToolkit.awtLock();
        try {
            if (configure_seen) {
                return toGlobal(0,0);
            } else {
                Point location = target.getLocation();
                if (insLog.isLoggable(PlatformLogger.FINE))
                    insLog.fine("getLocationOnScreen {0} not reparented: {1} ",
                                this, location);
                return location;
            }
        } finally {
            XToolkit.awtUnlock();
        }
    }
    protected boolean isEventDisabled(XEvent e) {
        switch (e.get_type()) {
          case XConstants.ConfigureNotify:
              return true;
          case XConstants.EnterNotify:
          case XConstants.LeaveNotify:
              return true;
          default:
              return super.isEventDisabled(e);
        }
    }
    int getDecorations() {
        return winAttr.decorations;
    }
    int getFunctions() {
        return winAttr.functions;
    }
    public void setVisible(boolean vis) {
        log.finer("Setting {0} to visible {1}", this, Boolean.valueOf(vis));
        if (vis && !isVisible()) {
            XWM.setShellDecor(this);
            super.setVisible(vis);
            if (winAttr.isResizable) {
                XWM.removeSizeHints(this, XUtilConstants.PMaxSize);
                updateMinimumSize();
            }
        } else {
            super.setVisible(vis);
        }
    }
    protected void suppressWmTakeFocus(boolean doSuppress) {
        XAtomList protocols = getWMProtocols();
        if (doSuppress) {
            protocols.remove(wm_take_focus);
        } else {
            protocols.add(wm_take_focus);
        }
        wm_protocols.setAtomListProperty(this, protocols);
    }
    public void dispose() {
        if (content != null) {
            content.destroy();
        }
        focusProxy.destroy();
        if (iconWindow != null) {
            iconWindow.destroy();
        }
        super.dispose();
    }
    public void handleClientMessage(XEvent xev) {
        super.handleClientMessage(xev);
        XClientMessageEvent cl = xev.get_xclient();
        if ((wm_protocols != null) && (cl.get_message_type() == wm_protocols.getAtom())) {
            if (cl.get_data(0) == wm_delete_window.getAtom()) {
                handleQuit();
            } else if (cl.get_data(0) == wm_take_focus.getAtom()) {
                handleWmTakeFocus(cl);
            }
        }
    }
    private void handleWmTakeFocus(XClientMessageEvent cl) {
        focusLog.fine("WM_TAKE_FOCUS on {0}", this);
        requestWindowFocus(cl.get_data(1), true);
    }
    protected void requestXFocus(long time, boolean timeProvided) {
        if (focusProxy == null) {
            if (focusLog.isLoggable(PlatformLogger.FINE)) focusLog.warning("Focus proxy is null for " + this);
        } else {
            if (focusLog.isLoggable(PlatformLogger.FINE)) focusLog.fine("Requesting focus to proxy: " + focusProxy);
            if (timeProvided) {
                focusProxy.xRequestFocus(time);
            } else {
                focusProxy.xRequestFocus();
            }
        }
    }
    XFocusProxyWindow getFocusProxy() {
        return focusProxy;
    }
    public void handleQuit() {
        postEvent(new WindowEvent((Window)target, WindowEvent.WINDOW_CLOSING));
    }
    final void dumpMe() {
        System.err.println(">>> Peer: " + x + ", " + y + ", " + width + ", " + height);
    }
    final void dumpTarget() {
        AWTAccessor.ComponentAccessor compAccessor = AWTAccessor.getComponentAccessor();
        int getWidth = compAccessor.getWidth((Component)target);
        int getHeight = compAccessor.getHeight((Component)target);
        int getTargetX = compAccessor.getX((Component)target);
        int getTargetY = compAccessor.getY((Component)target);
        System.err.println(">>> Target: " + getTargetX + ", " + getTargetY + ", " + getWidth + ", " + getHeight);
    }
    final void dumpShell() {
        dumpWindow("Shell", getShell());
    }
    final void dumpContent() {
        dumpWindow("Content", getContentWindow());
    }
    final void dumpParent() {
        long parent = XlibUtil.getParentWindow(getShell());
        if (parent != 0)
        {
            dumpWindow("Parent", parent);
        }
        else
        {
            System.err.println(">>> NO PARENT");
        }
    }
    final void dumpWindow(String id, long window) {
        XWindowAttributes pattr = new XWindowAttributes();
        try {
            XToolkit.awtLock();
            try {
                int status =
                    XlibWrapper.XGetWindowAttributes(XToolkit.getDisplay(),
                                                     window, pattr.pData);
            }
            finally {
                XToolkit.awtUnlock();
            }
            System.err.println(">>>> " + id + ": " + pattr.get_x()
                               + ", " + pattr.get_y() + ", " + pattr.get_width()
                               + ", " + pattr.get_height());
        } finally {
            pattr.dispose();
        }
    }
    final void dumpAll() {
        dumpTarget();
        dumpMe();
        dumpParent();
        dumpShell();
        dumpContent();
    }
    boolean isMaximized() {
        return false;
    }
    @Override
    boolean isOverrideRedirect() {
        return Window.Type.POPUP.equals(getWindowType());
    }
    public boolean requestWindowFocus(long time, boolean timeProvided) {
        focusLog.fine("Request for decorated window focus");
        Window focusedWindow = XKeyboardFocusManagerPeer.getCurrentNativeFocusedWindow();
        Window activeWindow = XWindowPeer.getDecoratedOwner(focusedWindow);
        focusLog.finer("Current window is: active={0}, focused={1}",
                       Boolean.valueOf(target == activeWindow),
                       Boolean.valueOf(target == focusedWindow));
        XWindowPeer toFocus = this;
        while (toFocus.nextTransientFor != null) {
            toFocus = toFocus.nextTransientFor;
        }
        if (toFocus == null || !toFocus.focusAllowedFor()) {
            return false;
        }
        if (this == toFocus) {
            if (isWMStateNetHidden()) {
                focusLog.fine("The window is unmapped, so rejecting the request");
                return false;
            }
            if (target == activeWindow && target != focusedWindow) {
                focusLog.fine("Focus is on child window - transfering it back to the owner");
                handleWindowFocusInSync(-1);
                return true;
            }
            Window realNativeFocusedWindow = XWindowPeer.getNativeFocusedWindow();
            focusLog.finest("Real native focused window: " + realNativeFocusedWindow +
                            "\nKFM's focused window: " + focusedWindow);
            if (target == realNativeFocusedWindow) {
                focusLog.fine("The window is already natively focused.");
                return true;
            }
        }
        focusLog.fine("Requesting focus to " + (this == toFocus ? "this window" : toFocus));
        if (timeProvided) {
            toFocus.requestXFocus(time);
        } else {
            toFocus.requestXFocus();
        }
        return (this == toFocus);
    }
    XWindowPeer actualFocusedWindow = null;
    void setActualFocusedWindow(XWindowPeer actualFocusedWindow) {
        synchronized(getStateLock()) {
            this.actualFocusedWindow = actualFocusedWindow;
        }
    }
    boolean requestWindowFocus(XWindowPeer actualFocusedWindow,
                               long time, boolean timeProvided)
    {
        setActualFocusedWindow(actualFocusedWindow);
        return requestWindowFocus(time, timeProvided);
    }
    public void handleWindowFocusIn(long serial) {
        if (null == actualFocusedWindow) {
            super.handleWindowFocusIn(serial);
        } else {
            postEvent(new InvocationEvent(target, new Runnable() {
                public void run() {
                    XWindowPeer fw = null;
                    synchronized (getStateLock()) {
                        fw = actualFocusedWindow;
                        actualFocusedWindow = null;
                        if (null == fw || !fw.isVisible() || !fw.isFocusableWindow()) {
                            fw = XDecoratedPeer.this;
                        }
                    }
                    fw.handleWindowFocusIn_Dispatch();
                }
            }));
        }
    }
    public void handleWindowFocusOut(Window oppositeWindow, long serial) {
        Window actualFocusedWindow = XKeyboardFocusManagerPeer.getCurrentNativeFocusedWindow();
        if (actualFocusedWindow != null && actualFocusedWindow != target) {
            Window owner = XWindowPeer.getDecoratedOwner(actualFocusedWindow);
            if (owner != null && owner == target) {
                setActualFocusedWindow((XWindowPeer) AWTAccessor.getComponentAccessor().getPeer(actualFocusedWindow));
            }
        }
        super.handleWindowFocusOut(oppositeWindow, serial);
    }
    private Point queryXLocation()
    {
        return XlibUtil.translateCoordinates(
            getContentWindow(),
            XlibWrapper.RootWindow(XToolkit.getDisplay(), getScreenNumber()),
            new Point(0, 0));
    }
}
