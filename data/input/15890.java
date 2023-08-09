public abstract class EmbeddedFrame extends Frame
                          implements KeyEventDispatcher, PropertyChangeListener {
    private boolean isCursorAllowed = true;
    private static Field fieldPeer;
    private static Field currentCycleRoot;
    private boolean supportsXEmbed = false;
    private KeyboardFocusManager appletKFM;
    private static final long serialVersionUID = 2967042741780317130L;
    protected static final boolean FORWARD = true;
    protected static final boolean BACKWARD = false;
    public boolean supportsXEmbed() {
        return supportsXEmbed && SunToolkit.needsXEmbed();
    }
    protected EmbeddedFrame(boolean supportsXEmbed) {
        this((long)0, supportsXEmbed);
    }
    protected EmbeddedFrame() {
        this((long)0);
    }
    @Deprecated
    protected EmbeddedFrame(int handle) {
        this((long)handle);
    }
    protected EmbeddedFrame(long handle) {
        this(handle, false);
    }
    protected EmbeddedFrame(long handle, boolean supportsXEmbed) {
        this.supportsXEmbed = supportsXEmbed;
        registerListeners();
    }
    public Container getParent() {
        return null;
    }
    public void propertyChange(PropertyChangeEvent evt) {
        if (!evt.getPropertyName().equals("managingFocus")) {
            return;
        }
        if (evt.getNewValue() == Boolean.TRUE) {
            return;
        }
        removeTraversingOutListeners((KeyboardFocusManager)evt.getSource());
        appletKFM = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        if (isVisible()) {
            addTraversingOutListeners(appletKFM);
        }
    }
    private void addTraversingOutListeners(KeyboardFocusManager kfm) {
        kfm.addKeyEventDispatcher(this);
        kfm.addPropertyChangeListener("managingFocus", this);
    }
    private void removeTraversingOutListeners(KeyboardFocusManager kfm) {
        kfm.removeKeyEventDispatcher(this);
        kfm.removePropertyChangeListener("managingFocus", this);
    }
    public void registerListeners() {
        if (appletKFM != null) {
            removeTraversingOutListeners(appletKFM);
        }
        appletKFM = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        if (isVisible()) {
            addTraversingOutListeners(appletKFM);
        }
    }
    public void show() {
        if (appletKFM != null) {
            addTraversingOutListeners(appletKFM);
        }
        super.show();
    }
    public void hide() {
        if (appletKFM != null) {
            removeTraversingOutListeners(appletKFM);
        }
        super.hide();
    }
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (currentCycleRoot == null) {
            currentCycleRoot = (Field)AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    try {
                        Field unaccessibleRoot = KeyboardFocusManager.class.
                                                     getDeclaredField("currentFocusCycleRoot");
                        if (unaccessibleRoot != null) {
                            unaccessibleRoot.setAccessible(true);
                        }
                        return unaccessibleRoot;
                    } catch (NoSuchFieldException e1) {
                        assert false;
                    } catch (SecurityException e2) {
                        assert false;
                    }
                    return null;
                }
            });
        }
        Container currentRoot = null;
        if (currentCycleRoot != null) {
            try {
                currentRoot = (Container)currentCycleRoot.get(null);
            } catch (IllegalAccessException e3) {
                assert false;
            }
        }
        if (this != currentRoot) {
            return false;
        }
        if (e.getID() == KeyEvent.KEY_TYPED) {
            return false;
        }
        if (!getFocusTraversalKeysEnabled() || e.isConsumed()) {
            return false;
        }
        AWTKeyStroke stroke = AWTKeyStroke.getAWTKeyStrokeForEvent(e);
        Set toTest;
        Component currentFocused = e.getComponent();
        toTest = getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
        if (toTest.contains(stroke)) {
            Component last = getFocusTraversalPolicy().getLastComponent(this);
            if (currentFocused == last || last == null) {
                if (traverseOut(FORWARD)) {
                    e.consume();
                    return true;
                }
            }
        }
        toTest = getFocusTraversalKeys(KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
        if (toTest.contains(stroke)) {
            Component first = getFocusTraversalPolicy().getFirstComponent(this);
            if (currentFocused == first || first == null) {
                if (traverseOut(BACKWARD)) {
                    e.consume();
                    return true;
                }
            }
        }
        return false;
    }
    public boolean traverseIn(boolean direction) {
        Component comp = null;
        if (direction == FORWARD) {
            comp = getFocusTraversalPolicy().getFirstComponent(this);
        } else {
            comp = getFocusTraversalPolicy().getLastComponent(this);
        }
        if (comp != null) {
            AWTAccessor.getKeyboardFocusManagerAccessor().setMostRecentFocusOwner(this, comp);
            synthesizeWindowActivation(true);
        }
        return (null != comp);
    }
    protected boolean traverseOut(boolean direction) {
        return false;
    }
    public void setTitle(String title) {}
    public void setIconImage(Image image) {}
    public void setIconImages(java.util.List<? extends Image> icons) {}
    public void setMenuBar(MenuBar mb) {}
    public void setResizable(boolean resizable) {}
    public void remove(MenuComponent m) {}
    public boolean isResizable() {
        return true;
    }
    public void addNotify() {
        synchronized (getTreeLock()) {
            if (getPeer() == null) {
                setPeer(new NullEmbeddedFramePeer());
            }
            super.addNotify();
        }
    }
    public void setCursorAllowed(boolean isCursorAllowed) {
        this.isCursorAllowed = isCursorAllowed;
        getPeer().updateCursorImmediately();
    }
    public boolean isCursorAllowed() {
        return isCursorAllowed;
    }
    public Cursor getCursor() {
        return (isCursorAllowed)
            ? super.getCursor()
            : Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
    }
    protected  void setPeer(final ComponentPeer p){
        if (fieldPeer == null) {
            fieldPeer = (Field)AccessController.doPrivileged(new PrivilegedAction() {
                    public Object run() {
                        try {
                            Field lnkPeer = Component.class.getDeclaredField("peer");
                            if (lnkPeer != null) {
                                lnkPeer.setAccessible(true);
                            }
                            return lnkPeer;
                        } catch (NoSuchFieldException e) {
                            assert false;
                        } catch (SecurityException e) {
                            assert false;
                        }
                        return null;
                    }
                });
        }
        try{
            if (fieldPeer !=null){
                fieldPeer.set(EmbeddedFrame.this, p);
            }
        } catch (IllegalAccessException e) {
            assert false;
        }
    };  
    public void synthesizeWindowActivation(boolean doActivate) {}
    protected void setLocationPrivate(int x, int y) {
        Dimension size = getSize();
        setBoundsPrivate(x, y, size.width, size.height);
    }
    protected Point getLocationPrivate() {
        Rectangle bounds = getBoundsPrivate();
        return new Point(bounds.x, bounds.y);
    }
    protected void setBoundsPrivate(int x, int y, int width, int height) {
        final FramePeer peer = (FramePeer)getPeer();
        if (peer != null) {
            peer.setBoundsPrivate(x, y, width, height);
        }
    }
    protected Rectangle getBoundsPrivate() {
        final FramePeer peer = (FramePeer)getPeer();
        if (peer != null) {
            return peer.getBoundsPrivate();
        }
        else {
            return getBounds();
        }
    }
    public void toFront() {}
    public void toBack() {}
    public abstract void registerAccelerator(AWTKeyStroke stroke);
    public abstract void unregisterAccelerator(AWTKeyStroke stroke);
    public static Applet getAppletIfAncestorOf(Component comp) {
        Container parent = comp.getParent();
        Applet applet = null;
        while (parent != null && !(parent instanceof EmbeddedFrame)) {
            if (parent instanceof Applet) {
                applet = (Applet)parent;
            }
            parent = parent.getParent();
        }
        return parent == null ? null : applet;
    }
    public void notifyModalBlocked(Dialog blocker, boolean blocked) {
    }
    private static class NullEmbeddedFramePeer
        extends NullComponentPeer implements FramePeer {
        public void setTitle(String title) {}
        public void setIconImage(Image im) {}
        public void updateIconImages() {}
        public void setMenuBar(MenuBar mb) {}
        public void setResizable(boolean resizeable) {}
        public void setState(int state) {}
        public int getState() { return Frame.NORMAL; }
        public void setMaximizedBounds(Rectangle b) {}
        public void toFront() {}
        public void toBack() {}
        public void updateFocusableWindowState() {}
        public void updateAlwaysOnTop() {}
        public void setAlwaysOnTop(boolean alwaysOnTop) {}
        public Component getGlobalHeavyweightFocusOwner() { return null; }
        public void setBoundsPrivate(int x, int y, int width, int height) {
            setBounds(x, y, width, height, SET_BOUNDS);
        }
        public Rectangle getBoundsPrivate() {
            return getBounds();
        }
        public void setModalBlocked(Dialog blocker, boolean blocked) {}
        public void restack() {
            throw new UnsupportedOperationException();
        }
        public boolean isRestackSupported() {
            return false;
        }
        public boolean requestWindowFocus() {
            return false;
        }
        public void updateMinimumSize() {
        }
        public void setOpacity(float opacity) {
        }
        public void setOpaque(boolean isOpaque) {
        }
        public void updateWindow() {
        }
        public void repositionSecurityWarning() {
        }
     }
} 
