class WCanvasPeer extends WComponentPeer implements CanvasPeer {
    private boolean eraseBackground;
    WCanvasPeer(Component target) {
        super(target);
    }
    native void create(WComponentPeer parent);
    void initialize() {
        eraseBackground = !SunToolkit.getSunAwtNoerasebackground();
        boolean eraseBackgroundOnResize = SunToolkit.getSunAwtErasebackgroundonresize();
        if (!PaintEventDispatcher.getPaintEventDispatcher().
                shouldDoNativeBackgroundErase((Component)target)) {
            eraseBackground = false;
        }
        setNativeBackgroundErase(eraseBackground, eraseBackgroundOnResize);
        super.initialize();
        Color bg = ((Component)target).getBackground();
        if (bg != null) {
            setBackground(bg);
        }
    }
    public void paint(Graphics g) {
        Dimension d = ((Component)target).getSize();
        if (g instanceof Graphics2D ||
            g instanceof sun.awt.Graphics2Delegate) {
            g.clearRect(0, 0, d.width, d.height);
        } else {
            g.setColor(((Component)target).getBackground());
            g.fillRect(0, 0, d.width, d.height);
            g.setColor(((Component)target).getForeground());
        }
        super.paint(g);
    }
    public boolean shouldClearRectBeforePaint() {
        return eraseBackground;
    }
    void disableBackgroundErase() {
        eraseBackground = false;
        setNativeBackgroundErase(false, false);
    }
    private native void setNativeBackgroundErase(boolean doErase,
                                                 boolean doEraseOnResize);
    public GraphicsConfiguration getAppropriateGraphicsConfiguration(
            GraphicsConfiguration gc)
    {
        return gc;
    }
}
