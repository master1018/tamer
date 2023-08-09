class XFramePeer extends XDecoratedPeer implements FramePeer {
    private static PlatformLogger log = PlatformLogger.getLogger("sun.awt.X11.XFramePeer");
    private static PlatformLogger stateLog = PlatformLogger.getLogger("sun.awt.X11.states");
    private static PlatformLogger insLog = PlatformLogger.getLogger("sun.awt.X11.insets.XFramePeer");
    XMenuBarPeer menubarPeer;
    MenuBar menubar;
    int state;
    private Boolean undecorated;
    private static final int MENUBAR_HEIGHT_IF_NO_MENUBAR = 0;
    private int lastAppliedMenubarHeight = MENUBAR_HEIGHT_IF_NO_MENUBAR;
    XFramePeer(Frame target) {
        super(target);
    }
    XFramePeer(XCreateWindowParams params) {
        super(params);
    }
    void preInit(XCreateWindowParams params) {
        super.preInit(params);
        Frame target = (Frame)(this.target);
        winAttr.initialState = target.getExtendedState();
        state = 0;
        undecorated = Boolean.valueOf(target.isUndecorated());
        winAttr.nativeDecor = !target.isUndecorated();
        if (winAttr.nativeDecor) {
            winAttr.decorations = winAttr.AWT_DECOR_ALL;
        } else {
            winAttr.decorations = winAttr.AWT_DECOR_NONE;
        }
        winAttr.functions = MWMConstants.MWM_FUNC_ALL;
        winAttr.isResizable = true; 
        winAttr.title = target.getTitle();
        winAttr.initialResizability = target.isResizable();
        if (log.isLoggable(PlatformLogger.FINE)) {
            log.fine("Frame''s initial attributes: decor {0}, resizable {1}, undecorated {2}, initial state {3}",
                     Integer.valueOf(winAttr.decorations), Boolean.valueOf(winAttr.initialResizability),
                     Boolean.valueOf(!winAttr.nativeDecor), Integer.valueOf(winAttr.initialState));
        }
    }
    void postInit(XCreateWindowParams params) {
        super.postInit(params);
        setupState(true);
    }
    @Override
    boolean isTargetUndecorated() {
        if (undecorated != null) {
            return undecorated.booleanValue();
        } else {
            return ((Frame)target).isUndecorated();
        }
    }
    void setupState(boolean onInit) {
        if (onInit) {
            state = winAttr.initialState;
        }
        if ((state & Frame.ICONIFIED) != 0) {
            setInitialState(XUtilConstants.IconicState);
        } else {
            setInitialState(XUtilConstants.NormalState);
        }
        setExtendedState(state);
    }
    public void setMenuBar(MenuBar mb) {
        XToolkit.awtLock();
        try {
            synchronized(getStateLock()) {
                if (mb == menubar) return;
                if (mb == null) {
                    if (menubar != null) {
                        menubarPeer.xSetVisible(false);
                        menubar = null;
                        menubarPeer.dispose();
                        menubarPeer = null;
                    }
                } else {
                    menubar = mb;
                    menubarPeer = (XMenuBarPeer) mb.getPeer();
                    if (menubarPeer != null) {
                        menubarPeer.init((Frame)target);
                    }
                }
            }
        } finally {
            XToolkit.awtUnlock();
        }
        reshapeMenubarPeer();
    }
    XMenuBarPeer getMenubarPeer() {
        return menubarPeer;
    }
    int getMenuBarHeight() {
        if (menubarPeer != null) {
            return menubarPeer.getDesiredHeight();
        } else {
            return MENUBAR_HEIGHT_IF_NO_MENUBAR;
        }
    }
    void updateChildrenSizes() {
        super.updateChildrenSizes();
        int height = getMenuBarHeight();
        XToolkit.awtLock();
        try {
            synchronized(getStateLock()) {
                int width = dimensions.getClientSize().width;
                if (menubarPeer != null) {
                    menubarPeer.reshape(0, 0, width, height);
                }
            }
        } finally {
            XToolkit.awtUnlock();
        }
    }
    final void reshapeMenubarPeer() {
        XToolkit.executeOnEventHandlerThread(
            target,
            new Runnable() {
                public void run() {
                    updateChildrenSizes();
                    boolean heightChanged = false;
                    int height = getMenuBarHeight();
                    synchronized(getStateLock()) {
                        if (height != lastAppliedMenubarHeight) {
                            lastAppliedMenubarHeight = height;
                            heightChanged = true;
                        }
                    }
                    if (heightChanged) {
                        target.invalidate();
                        target.validate();
                    }
                }
            }
        );
    }
    public void setMaximizedBounds(Rectangle b) {
        if (insLog.isLoggable(PlatformLogger.FINE)) insLog.fine("Setting maximized bounds to " + b);
        if (b == null) return;
        maxBounds = new Rectangle(b);
        XToolkit.awtLock();
        try {
            XSizeHints hints = getHints();
            hints.set_flags(hints.get_flags() | (int)XUtilConstants.PMaxSize);
            if (b.width != Integer.MAX_VALUE) {
                hints.set_max_width(b.width);
            } else {
                hints.set_max_width((int)XlibWrapper.DisplayWidth(XToolkit.getDisplay(), XlibWrapper.DefaultScreen(XToolkit.getDisplay())));
            }
            if (b.height != Integer.MAX_VALUE) {
                hints.set_max_height(b.height);
            } else {
                hints.set_max_height((int)XlibWrapper.DisplayHeight(XToolkit.getDisplay(), XlibWrapper.DefaultScreen(XToolkit.getDisplay())));
            }
            if (insLog.isLoggable(PlatformLogger.FINER)) insLog.finer("Setting hints, flags " + XlibWrapper.hintsToString(hints.get_flags()));
            XlibWrapper.XSetWMNormalHints(XToolkit.getDisplay(), window, hints.pData);
        } finally {
            XToolkit.awtUnlock();
        }
    }
    public int getState() {
        synchronized(getStateLock()) {
            return state;
        }
    }
    public void setState(int newState) {
        synchronized(getStateLock()) {
            if (!isShowing()) {
                stateLog.finer("Frame is not showing");
                state = newState;
                return;
            }
        }
        changeState(newState);
    }
    void changeState(int newState) {
        int changed = state ^ newState;
        int changeIconic = changed & Frame.ICONIFIED;
        boolean iconic = (newState & Frame.ICONIFIED) != 0;
        stateLog.finer("Changing state, old state {0}, new state {1}(iconic {2})",
                       Integer.valueOf(state), Integer.valueOf(newState), Boolean.valueOf(iconic));
        if (changeIconic != 0 && iconic) {
            if (stateLog.isLoggable(PlatformLogger.FINER)) stateLog.finer("Iconifying shell " + getShell() + ", this " + this + ", screen " + getScreenNumber());
            XToolkit.awtLock();
            try {
                int res = XlibWrapper.XIconifyWindow(XToolkit.getDisplay(), getShell(), getScreenNumber());
                if (stateLog.isLoggable(PlatformLogger.FINER)) stateLog.finer("XIconifyWindow returned " + res);
            }
            finally {
                XToolkit.awtUnlock();
            }
        }
        if ((changed & ~Frame.ICONIFIED) != 0) {
            setExtendedState(newState);
        }
        if (changeIconic != 0 && !iconic) {
            if (stateLog.isLoggable(PlatformLogger.FINER)) stateLog.finer("DeIconifying " + this);
            xSetVisible(true);
        }
    }
    void setExtendedState(int newState) {
        XWM.getWM().setExtendedState(this, newState);
    }
    public void handlePropertyNotify(XEvent xev) {
        super.handlePropertyNotify(xev);
        XPropertyEvent ev = xev.get_xproperty();
        log.finer("Property change {0}", ev);
        if (!XWM.getWM().isStateChange(this, ev)) {
            stateLog.finer("either not a state atom or state has not been changed");
            return;
        }
        final int newState = XWM.getWM().getState(this);
        int changed = state ^ newState;
        if (changed == 0) {
            stateLog.finer("State is the same: " + state);
            return;
        }
        int old_state = state;
        state = newState;
        AWTAccessor.getFrameAccessor().setExtendedState((Frame)target, state);
        if ((changed & Frame.ICONIFIED) != 0) {
            if ((state & Frame.ICONIFIED) != 0) {
                stateLog.finer("Iconified");
                handleIconify();
            } else {
                stateLog.finer("DeIconified");
                content.purgeIconifiedExposeEvents();
                handleDeiconify();
            }
        }
        handleStateChange(old_state, state);
    }
    public void handleStateChange(int oldState, int newState) {
        super.handleStateChange(oldState, newState);
        for (ToplevelStateListener topLevelListenerTmp : toplevelStateListeners) {
            topLevelListenerTmp.stateChangedJava(oldState, newState);
        }
    }
    public void setVisible(boolean vis) {
        if (vis) {
            setupState(false);
        } else {
            if ((state & Frame.MAXIMIZED_BOTH) != 0) {
                XWM.getWM().setExtendedState(this, state & ~Frame.MAXIMIZED_BOTH);
            }
        }
        super.setVisible(vis);
        if (vis && maxBounds != null) {
            setMaximizedBounds(maxBounds);
        }
    }
    void setInitialState(int wm_state) {
        XToolkit.awtLock();
        try {
            XWMHints hints = getWMHints();
            hints.set_flags((int)XUtilConstants.StateHint | hints.get_flags());
            hints.set_initial_state(wm_state);
            if (stateLog.isLoggable(PlatformLogger.FINE)) stateLog.fine("Setting initial WM state on " + this + " to " + wm_state);
            XlibWrapper.XSetWMHints(XToolkit.getDisplay(), getWindow(), hints.pData);
        }
        finally {
            XToolkit.awtUnlock();
        }
    }
    public void dispose() {
        if (menubarPeer != null) {
            menubarPeer.dispose();
        }
        super.dispose();
    }
    boolean isMaximized() {
        return (state & (Frame.MAXIMIZED_VERT  | Frame.MAXIMIZED_HORIZ)) != 0;
    }
    static final int CROSSHAIR_INSET = 5;
    static final int BUTTON_Y = CROSSHAIR_INSET + 1;
    static final int BUTTON_W = 17;
    static final int BUTTON_H = 17;
    static final int SYS_MENU_X = CROSSHAIR_INSET + 1;
    static final int SYS_MENU_CONTAINED_X = SYS_MENU_X + 5;
    static final int SYS_MENU_CONTAINED_Y = BUTTON_Y + 7;
    static final int SYS_MENU_CONTAINED_W = 8;
    static final int SYS_MENU_CONTAINED_H = 3;
    static final int MAXIMIZE_X_DIFF = CROSSHAIR_INSET + BUTTON_W;
    static final int MAXIMIZE_CONTAINED_X_DIFF = MAXIMIZE_X_DIFF - 5;
    static final int MAXIMIZE_CONTAINED_Y = BUTTON_Y + 5;
    static final int MAXIMIZE_CONTAINED_W = 8;
    static final int MAXIMIZE_CONTAINED_H = 8;
    static final int MINIMIZE_X_DIFF = MAXIMIZE_X_DIFF + BUTTON_W;
    static final int MINIMIZE_CONTAINED_X_DIFF = MINIMIZE_X_DIFF - 7;
    static final int MINIMIZE_CONTAINED_Y = BUTTON_Y + 7;
    static final int MINIMIZE_CONTAINED_W = 3;
    static final int MINIMIZE_CONTAINED_H = 3;
    static final int TITLE_X = SYS_MENU_X + BUTTON_W;
    static final int TITLE_W_DIFF = BUTTON_W * 3 + CROSSHAIR_INSET * 2 - 1;
    static final int TITLE_MID_Y = BUTTON_Y + (BUTTON_H / 2);
    static final int MENUBAR_X = CROSSHAIR_INSET + 1;
    static final int MENUBAR_Y = BUTTON_Y + BUTTON_H;
    static final int HORIZ_RESIZE_INSET = CROSSHAIR_INSET + BUTTON_H;
    static final int VERT_RESIZE_INSET = CROSSHAIR_INSET + BUTTON_W;
    public void print(Graphics g) {
        super.print(g);
        Frame f = (Frame)target;
        Insets finsets = f.getInsets();
        Dimension fsize = f.getSize();
        Color bg = f.getBackground();
        Color fg = f.getForeground();
        Color highlight = bg.brighter();
        Color shadow = bg.darker();
        if (hasDecorations(XWindowAttributesData.AWT_DECOR_BORDER)) {
            if (highlight.equals(Color.white)) {
                g.setColor(new Color(230, 230, 230));
            }
            else {
                g.setColor(highlight);
            }
            g.drawLine(0, 0, fsize.width, 0);
            g.drawLine(0, 1, fsize.width - 1, 1);
            g.drawLine(0, 0, 0, fsize.height);
            g.drawLine(1, 0, 1, fsize.height - 1);
            g.setColor(highlight);
            g.drawLine(CROSSHAIR_INSET + 1, fsize.height - CROSSHAIR_INSET,
                       fsize.width - CROSSHAIR_INSET,
                       fsize.height - CROSSHAIR_INSET);
            g.drawLine(fsize.width - CROSSHAIR_INSET, CROSSHAIR_INSET + 1,
                       fsize.width - CROSSHAIR_INSET,
                       fsize.height - CROSSHAIR_INSET);
            g.setColor(shadow);
            g.drawLine(1, fsize.height, fsize.width, fsize.height);
            g.drawLine(2, fsize.height - 1, fsize.width, fsize.height - 1);
            g.drawLine(fsize.width, 1, fsize.width, fsize.height);
            g.drawLine(fsize.width - 1, 2, fsize.width - 1, fsize.height);
            g.drawLine(CROSSHAIR_INSET, CROSSHAIR_INSET,
                       fsize.width - CROSSHAIR_INSET, CROSSHAIR_INSET);
            g.drawLine(CROSSHAIR_INSET, CROSSHAIR_INSET, CROSSHAIR_INSET,
                       fsize.height - CROSSHAIR_INSET);
        }
        if (hasDecorations(XWindowAttributesData.AWT_DECOR_TITLE)) {
            if (hasDecorations(XWindowAttributesData.AWT_DECOR_MENU)) {
                g.setColor(bg);
                g.fill3DRect(SYS_MENU_X, BUTTON_Y, BUTTON_W, BUTTON_H, true);
                g.fill3DRect(SYS_MENU_CONTAINED_X, SYS_MENU_CONTAINED_Y,
                             SYS_MENU_CONTAINED_W, SYS_MENU_CONTAINED_H, true);
            }
            g.fill3DRect(TITLE_X, BUTTON_Y, fsize.width - TITLE_W_DIFF, BUTTON_H,
                         true);
            if (hasDecorations(XWindowAttributesData.AWT_DECOR_MINIMIZE)) {
                g.fill3DRect(fsize.width - MINIMIZE_X_DIFF, BUTTON_Y, BUTTON_W,
                             BUTTON_H, true);
                g.fill3DRect(fsize.width - MINIMIZE_CONTAINED_X_DIFF,
                             MINIMIZE_CONTAINED_Y, MINIMIZE_CONTAINED_W,
                             MINIMIZE_CONTAINED_H, true);
            }
            if (hasDecorations(XWindowAttributesData.AWT_DECOR_MAXIMIZE)) {
                g.fill3DRect(fsize.width - MAXIMIZE_X_DIFF, BUTTON_Y, BUTTON_W,
                             BUTTON_H, true);
                g.fill3DRect(fsize.width - MAXIMIZE_CONTAINED_X_DIFF,
                             MAXIMIZE_CONTAINED_Y, MAXIMIZE_CONTAINED_W,
                             MAXIMIZE_CONTAINED_H, true);
            }
            g.setColor(fg);
            Font sysfont = new Font(Font.SANS_SERIF, Font.PLAIN, 10);
            g.setFont(sysfont);
            FontMetrics sysfm = g.getFontMetrics();
            String ftitle = f.getTitle();
            g.drawString(ftitle,
                         ((TITLE_X + TITLE_X + fsize.width - TITLE_W_DIFF) / 2) -
                         (sysfm.stringWidth(ftitle) / 2),
                         TITLE_MID_Y + sysfm.getMaxDescent());
        }
        if (f.isResizable() &&
            hasDecorations(XWindowAttributesData.AWT_DECOR_RESIZEH)) {
            g.setColor(shadow);
            g.drawLine(1, HORIZ_RESIZE_INSET, CROSSHAIR_INSET,
                       HORIZ_RESIZE_INSET);
            g.drawLine(VERT_RESIZE_INSET, 1, VERT_RESIZE_INSET, CROSSHAIR_INSET);
            g.drawLine(fsize.width - CROSSHAIR_INSET + 1, HORIZ_RESIZE_INSET,
                       fsize.width, HORIZ_RESIZE_INSET);
            g.drawLine(fsize.width - VERT_RESIZE_INSET - 1, 2,
                       fsize.width - VERT_RESIZE_INSET - 1, CROSSHAIR_INSET + 1);
            g.drawLine(1, fsize.height - HORIZ_RESIZE_INSET - 1,
                       CROSSHAIR_INSET, fsize.height - HORIZ_RESIZE_INSET - 1);
            g.drawLine(VERT_RESIZE_INSET, fsize.height - CROSSHAIR_INSET + 1,
                       VERT_RESIZE_INSET, fsize.height);
            g.drawLine(fsize.width - CROSSHAIR_INSET + 1,
                       fsize.height - HORIZ_RESIZE_INSET - 1, fsize.width,
                       fsize.height - HORIZ_RESIZE_INSET - 1);
            g.drawLine(fsize.width - VERT_RESIZE_INSET - 1,
                       fsize.height - CROSSHAIR_INSET + 1,
                       fsize.width - VERT_RESIZE_INSET - 1, fsize.height);
            g.setColor(highlight);
            g.drawLine(2, HORIZ_RESIZE_INSET + 1, CROSSHAIR_INSET,
                       HORIZ_RESIZE_INSET + 1);
            g.drawLine(VERT_RESIZE_INSET + 1, 2, VERT_RESIZE_INSET + 1,
                       CROSSHAIR_INSET);
            g.drawLine(fsize.width - CROSSHAIR_INSET + 1,
                       HORIZ_RESIZE_INSET + 1, fsize.width - 1,
                       HORIZ_RESIZE_INSET + 1);
            g.drawLine(fsize.width - VERT_RESIZE_INSET, 2,
                       fsize.width - VERT_RESIZE_INSET, CROSSHAIR_INSET);
            g.drawLine(2, fsize.height - HORIZ_RESIZE_INSET, CROSSHAIR_INSET,
                       fsize.height - HORIZ_RESIZE_INSET);
            g.drawLine(VERT_RESIZE_INSET + 1,
                       fsize.height - CROSSHAIR_INSET + 1,
                       VERT_RESIZE_INSET + 1, fsize.height - 1);
            g.drawLine(fsize.width - CROSSHAIR_INSET + 1,
                       fsize.height - HORIZ_RESIZE_INSET, fsize.width - 1,
                       fsize.height - HORIZ_RESIZE_INSET);
            g.drawLine(fsize.width - VERT_RESIZE_INSET,
                       fsize.height - CROSSHAIR_INSET + 1,
                       fsize.width - VERT_RESIZE_INSET, fsize.height - 1);
        }
        XMenuBarPeer peer = menubarPeer;
        if (peer != null) {
            Insets insets = getInsets();
            Graphics ng = g.create();
            int menubarX = 0;
            int menubarY = 0;
            if (hasDecorations(XWindowAttributesData.AWT_DECOR_BORDER)) {
                menubarX += CROSSHAIR_INSET + 1;
                    menubarY += CROSSHAIR_INSET + 1;
            }
            if (hasDecorations(XWindowAttributesData.AWT_DECOR_TITLE)) {
                menubarY += BUTTON_H;
            }
            try {
                ng.translate(menubarX, menubarY);
                peer.print(ng);
            } finally {
                ng.dispose();
            }
        }
    }
    public void setBoundsPrivate(int x, int y, int width, int height) {
        setBounds(x, y, width, height, SET_BOUNDS);
    }
    public Rectangle getBoundsPrivate() {
        return getBounds();
    }
}
