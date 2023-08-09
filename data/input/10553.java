class WFramePeer extends WWindowPeer implements FramePeer {
    static {
        initIDs();
    }
    private static native void initIDs();
    public native void setState(int state);
    public native int getState();
    public void setExtendedState(int state) {
        AWTAccessor.getFrameAccessor().setExtendedState((Frame)target, state);
    }
    public int getExtendedState() {
        return AWTAccessor.getFrameAccessor().getExtendedState((Frame)target);
    }
    private native void setMaximizedBounds(int x, int y, int w, int h);
    private native void clearMaximizedBounds();
    private static final boolean keepOnMinimize = "true".equals(
        (String)java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction(
                "sun.awt.keepWorkingSetOnMinimize")));
    public void setMaximizedBounds(Rectangle b) {
        if (b == null) {
            clearMaximizedBounds();
        } else {
            Rectangle adjBounds = (Rectangle)b.clone();
            adjustMaximizedBounds(adjBounds);
            setMaximizedBounds(adjBounds.x, adjBounds.y, adjBounds.width, adjBounds.height);
        }
    }
    private void adjustMaximizedBounds(Rectangle b) {
        GraphicsConfiguration currentDevGC = getGraphicsConfiguration();
        GraphicsDevice primaryDev = GraphicsEnvironment
            .getLocalGraphicsEnvironment().getDefaultScreenDevice();
        GraphicsConfiguration primaryDevGC = primaryDev.getDefaultConfiguration();
        if (currentDevGC != null && currentDevGC != primaryDevGC) {
            Rectangle currentDevBounds = currentDevGC.getBounds();
            Rectangle primaryDevBounds = primaryDevGC.getBounds();
            boolean isCurrentDevLarger =
                ((currentDevBounds.width - primaryDevBounds.width > 0) ||
                 (currentDevBounds.height - primaryDevBounds.height > 0));
            if (isCurrentDevLarger) {
                b.width -= (currentDevBounds.width - primaryDevBounds.width);
                b.height -= (currentDevBounds.height - primaryDevBounds.height);
            }
        }
    }
    @Override
    public boolean updateGraphicsData(GraphicsConfiguration gc) {
        boolean result = super.updateGraphicsData(gc);
        Rectangle bounds = AWTAccessor.getFrameAccessor().
                               getMaximizedBounds((Frame)target);
        if (bounds != null) {
            setMaximizedBounds(bounds);
        }
        return result;
    }
    @Override
    boolean isTargetUndecorated() {
        return ((Frame)target).isUndecorated();
    }
    public void reshape(int x, int y, int width, int height) {
        if (((Frame)target).isUndecorated()) {
            super.reshape(x, y, width, height);
        } else {
            reshapeFrame(x, y, width, height);
        }
    }
    public Dimension getMinimumSize() {
        Dimension d = new Dimension();
        if (!((Frame)target).isUndecorated()) {
            d.setSize(getSysMinWidth(), getSysMinHeight());
        }
        if (((Frame)target).getMenuBar() != null) {
            d.height += getSysMenuHeight();
        }
        return d;
    }
    public void setMenuBar(MenuBar mb) {
        WMenuBarPeer mbPeer = (WMenuBarPeer) WToolkit.targetToPeer(mb);
        setMenuBar0(mbPeer);
        updateInsets(insets_);
    }
    private native void setMenuBar0(WMenuBarPeer mbPeer);
    WFramePeer(Frame target) {
        super(target);
        InputMethodManager imm = InputMethodManager.getInstance();
        String menuString = imm.getTriggerMenuString();
        if (menuString != null)
        {
          pSetIMMOption(menuString);
        }
    }
    native void createAwtFrame(WComponentPeer parent);
    void create(WComponentPeer parent) {
        preCreate(parent);
        createAwtFrame(parent);
    }
    void initialize() {
        super.initialize();
        Frame target = (Frame)this.target;
        if (target.getTitle() != null) {
            setTitle(target.getTitle());
        }
        setResizable(target.isResizable());
        setState(target.getExtendedState());
    }
    private native static int getSysMenuHeight();
    native void pSetIMMOption(String option);
    void notifyIMMOptionChange(){
      InputMethodManager.getInstance().notifyChangeRequest((Component)target);
    }
    public void setBoundsPrivate(int x, int y, int width, int height) {
        setBounds(x, y, width, height, SET_BOUNDS);
    }
    public Rectangle getBoundsPrivate() {
        return getBounds();
    }
}
