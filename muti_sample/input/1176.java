public class XEmbedChildProxyPeer implements ComponentPeer, XEventDispatcher{
    XEmbeddingContainer container;
    XEmbedChildProxy proxy;
    long handle;
    XEmbedChildProxyPeer(XEmbedChildProxy proxy) {
        this.container = proxy.getEmbeddingContainer();
        this.handle = proxy.getHandle();
        this.proxy = proxy;
        initDispatching();
    }
    void initDispatching() {
        XToolkit.awtLock();
        try {
            XToolkit.addEventDispatcher(handle, this);
            XlibWrapper.XSelectInput(XToolkit.getDisplay(), handle,
                    XConstants.StructureNotifyMask | XConstants.PropertyChangeMask);
        }
        finally {
            XToolkit.awtUnlock();
        }
        container.notifyChildEmbedded(handle);
    }
    public boolean isObscured() { return false; }
    public boolean canDetermineObscurity() { return false; }
    public void                 setVisible(boolean b) {
        if (!b) {
            XToolkit.awtLock();
            try {
                XlibWrapper.XUnmapWindow(XToolkit.getDisplay(), handle);
            }
            finally {
                XToolkit.awtUnlock();
            }
        } else {
            XToolkit.awtLock();
            try {
                XlibWrapper.XMapWindow(XToolkit.getDisplay(), handle);
            }
            finally {
                XToolkit.awtUnlock();
            }
        }
    }
    public void setEnabled(boolean b) {}
    public void paint(Graphics g) {}
    public void repaint(long tm, int x, int y, int width, int height) {}
    public void print(Graphics g) {}
    public void setBounds(int x, int y, int width, int height, int op) {
        XToolkit.awtLock();
        try {
            XlibWrapper.XMoveResizeWindow(XToolkit.getDisplay(), handle, x, y, width, height);
        }
        finally {
            XToolkit.awtUnlock();
        }
    }
    public void handleEvent(AWTEvent e) {
        switch (e.getID()) {
          case FocusEvent.FOCUS_GAINED:
              XKeyboardFocusManagerPeer.setCurrentNativeFocusOwner(proxy);
              container.focusGained(handle);
              break;
          case FocusEvent.FOCUS_LOST:
              XKeyboardFocusManagerPeer.setCurrentNativeFocusOwner(null);
              container.focusLost(handle);
              break;
          case KeyEvent.KEY_PRESSED:
          case KeyEvent.KEY_RELEASED:
              if (!((InputEvent)e).isConsumed()) {
                  container.forwardKeyEvent(handle, (KeyEvent)e);
              }
              break;
        }
    }
    public void                coalescePaintEvent(PaintEvent e) {}
    public Point                getLocationOnScreen() {
        XWindowAttributes attr = new XWindowAttributes();
        XToolkit.awtLock();
        try{
            XlibWrapper.XGetWindowAttributes(XToolkit.getDisplay(), handle, attr.pData);
            return new Point(attr.get_x(), attr.get_y());
        } finally {
            XToolkit.awtUnlock();
            attr.dispose();
        }
    }
    public Dimension            getPreferredSize() {
        XToolkit.awtLock();
        long p_hints = XlibWrapper.XAllocSizeHints();
        try {
            XSizeHints hints = new XSizeHints(p_hints);
            XlibWrapper.XGetWMNormalHints(XToolkit.getDisplay(), handle, p_hints, XlibWrapper.larg1);
            Dimension res = new Dimension(hints.get_width(), hints.get_height());
            return res;
        } finally {
            XlibWrapper.XFree(p_hints);
            XToolkit.awtUnlock();
        }
    }
    public Dimension            getMinimumSize() {
        XToolkit.awtLock();
        long p_hints = XlibWrapper.XAllocSizeHints();
        try {
            XSizeHints hints = new XSizeHints(p_hints);
            XlibWrapper.XGetWMNormalHints(XToolkit.getDisplay(), handle, p_hints, XlibWrapper.larg1);
            Dimension res = new Dimension(hints.get_min_width(), hints.get_min_height());
            return res;
        } finally {
            XlibWrapper.XFree(p_hints);
            XToolkit.awtUnlock();
        }
    }
    public ColorModel           getColorModel() { return null; }
    public Toolkit              getToolkit() { return Toolkit.getDefaultToolkit(); }
    public Graphics             getGraphics() { return null; }
    public FontMetrics          getFontMetrics(Font font) { return null; }
    public void         dispose() {
        container.detachChild(handle);
    }
    public void         setForeground(Color c) {}
    public void         setBackground(Color c) {}
    public void         setFont(Font f) {}
    public void                 updateCursorImmediately() {}
    void postEvent(AWTEvent event) {
        XToolkit.postEvent(XToolkit.targetToAppContext(proxy), event);
    }
    boolean simulateMotifRequestFocus(Component lightweightChild, boolean temporary,
                                      boolean focusedWindowChangeAllowed, long time)
    {
        if (lightweightChild == null) {
            lightweightChild = (Component)proxy;
        }
        Component currentOwner = XKeyboardFocusManagerPeer.getCurrentNativeFocusOwner();
        if (currentOwner != null && currentOwner.getPeer() == null) {
            currentOwner = null;
        }
        FocusEvent  fg = new FocusEvent(lightweightChild, FocusEvent.FOCUS_GAINED, false, currentOwner );
        FocusEvent fl = null;
        if (currentOwner != null) {
            fl = new FocusEvent(currentOwner, FocusEvent.FOCUS_LOST, false, lightweightChild);
        }
        if (fl != null) {
            postEvent(XComponentPeer.wrapInSequenced(fl));
        }
        postEvent(XComponentPeer.wrapInSequenced(fg));
        return true;
    }
    public boolean requestFocus(Component lightweightChild,
                                boolean temporary,
                                boolean focusedWindowChangeAllowed,
                                long time,
                                CausedFocusEvent.Cause cause)
    {
        int result = XKeyboardFocusManagerPeer
            .shouldNativelyFocusHeavyweight(proxy, lightweightChild,
                                            temporary, false, time, cause);
        switch (result) {
          case XKeyboardFocusManagerPeer.SNFH_FAILURE:
              return false;
          case XKeyboardFocusManagerPeer.SNFH_SUCCESS_PROCEED:
              Container parent = proxy.getParent();
              while (parent != null && !(parent instanceof Window)) {
                  parent = parent.getParent();
              }
              if (parent != null) {
                  Window parentWindow = (Window)parent;
                  if (!parentWindow.isFocused() && XKeyboardFocusManagerPeer.getCurrentNativeFocusedWindow() == parentWindow) {
                      return true;
                  }
              }
              return simulateMotifRequestFocus(lightweightChild, temporary, focusedWindowChangeAllowed, time);
          case XKeyboardFocusManagerPeer.SNFH_SUCCESS_HANDLED:
              return true;
        }
        return false;
    }
    public boolean              isFocusable() {
        return true;
    }
    public Image                createImage(ImageProducer producer) { return null; }
    public Image                createImage(int width, int height) { return null; }
    public VolatileImage        createVolatileImage(int width, int height) { return null; }
    public boolean              prepareImage(Image img, int w, int h, ImageObserver o) { return false; }
    public int                  checkImage(Image img, int w, int h, ImageObserver o) { return 0; }
    public GraphicsConfiguration getGraphicsConfiguration() { return null; }
    public boolean     handlesWheelScrolling() { return true; }
    public void createBuffers(int numBuffers, BufferCapabilities caps)
      throws AWTException { }
    public Image getBackBuffer() { return null; }
    public void flip(int x1, int y1, int x2, int y2, BufferCapabilities.FlipContents flipAction) {  }
    public void destroyBuffers() { }
    public void        layout() {}
    public Dimension            preferredSize() {
        return getPreferredSize();
    }
    public Dimension            minimumSize() {
        return getMinimumSize();
    }
    public void         show() {
        setVisible(true);
    }
    public void         hide() {
        setVisible(false);
    }
    public void         enable() {}
    public void         disable() {}
    public void reshape(int x, int y, int width, int height) {
        setBounds(x, y, width, height, SET_BOUNDS);
    }
    Window getTopLevel(Component comp) {
        while (comp != null && !(comp instanceof Window)) {
            comp = comp.getParent();
        }
        return (Window)comp;
    }
    void childResized() {
        XToolkit.postEvent(XToolkit.targetToAppContext(proxy), new ComponentEvent(proxy, ComponentEvent.COMPONENT_RESIZED));
        container.childResized(proxy);
    }
    void handlePropertyNotify(XEvent xev) {
        XPropertyEvent ev = xev.get_xproperty();
        if (ev.get_atom() == XAtom.XA_WM_NORMAL_HINTS) {
            childResized();
        }
    }
    void handleConfigureNotify(XEvent xev) {
        childResized();
    }
    public void dispatchEvent(XEvent xev) {
        int type = xev.get_type();
        switch (type) {
          case XConstants.PropertyNotify:
              handlePropertyNotify(xev);
              break;
          case XConstants.ConfigureNotify:
              handleConfigureNotify(xev);
              break;
        }
    }
    void requestXEmbedFocus() {
        postEvent(new InvocationEvent(proxy, new Runnable() {
                public void run() {
                    proxy.requestFocusInWindow();
                }
            }));
    }
    public void reparent(ContainerPeer newNativeParent) {
    }
    public boolean isReparentSupported() {
        return false;
    }
    public Rectangle getBounds() {
        XWindowAttributes attrs = new XWindowAttributes();
        XToolkit.awtLock();
        try {
            XlibWrapper.XGetWindowAttributes(XToolkit.getDisplay(), handle, attrs.pData);
            return new Rectangle(attrs.get_x(), attrs.get_y(), attrs.get_width(), attrs.get_height());
        } finally {
            XToolkit.awtUnlock();
            attrs.dispose();
        }
    }
    public void setBoundsOperation(int operation) {
    }
    public void applyShape(Region shape) {
    }
    public void setZOrder(ComponentPeer above) {
    }
    public boolean updateGraphicsData(GraphicsConfiguration gc) {
        return false;
    }
}
