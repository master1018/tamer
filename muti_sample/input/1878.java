class WPanelPeer extends WCanvasPeer implements PanelPeer {
    public void paint(Graphics g) {
        super.paint(g);
        SunGraphicsCallback.PaintHeavyweightComponentsCallback.getInstance().
            runComponents(((Container)target).getComponents(), g,
                          SunGraphicsCallback.LIGHTWEIGHTS |
                          SunGraphicsCallback.HEAVYWEIGHTS);
    }
    public void print(Graphics g) {
        super.print(g);
        SunGraphicsCallback.PrintHeavyweightComponentsCallback.getInstance().
            runComponents(((Container)target).getComponents(), g,
                          SunGraphicsCallback.LIGHTWEIGHTS |
                          SunGraphicsCallback.HEAVYWEIGHTS);
    }
    public Insets getInsets() {
        return insets_;
    }
    Insets insets_;
    static {
        initIDs();
    }
    private static native void initIDs();
    WPanelPeer(Component target) {
        super(target);
    }
    void initialize() {
        super.initialize();
        insets_ = new Insets(0,0,0,0);
        Color c = ((Component)target).getBackground();
        if (c == null) {
            c = WColor.getDefaultColor(WColor.WINDOW_BKGND);
            ((Component)target).setBackground(c);
            setBackground(c);
        }
        c = ((Component)target).getForeground();
        if (c == null) {
            c = WColor.getDefaultColor(WColor.WINDOW_TEXT);
            ((Component)target).setForeground(c);
            setForeground(c);
        }
    }
    public Insets insets() {
        return getInsets();
    }
}
