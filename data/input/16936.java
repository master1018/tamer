class XCanvasPeer extends XComponentPeer implements CanvasPeer {
    private boolean eraseBackgroundDisabled;
    XCanvasPeer() {}
    XCanvasPeer(XCreateWindowParams params) {
        super(params);
    }
    XCanvasPeer(Component target) {
        super(target);
    }
    void preInit(XCreateWindowParams params) {
        super.preInit(params);
        if (SunToolkit.getSunAwtNoerasebackground()) {
            disableBackgroundErase();
        }
    }
    public GraphicsConfiguration getAppropriateGraphicsConfiguration(
                                    GraphicsConfiguration gc)
    {
        if (graphicsConfig == null || gc == null) {
            return gc;
        }
        int screenNum = ((X11GraphicsDevice)gc.getDevice()).getScreen();
        X11GraphicsConfig parentgc;
        int visual = graphicsConfig.getVisual();
        X11GraphicsDevice newDev = (X11GraphicsDevice) GraphicsEnvironment.
            getLocalGraphicsEnvironment().
            getScreenDevices()[screenNum];
        for (int i = 0; i < newDev.getNumConfigs(screenNum); i++) {
            if (visual == newDev.getConfigVisualId(i, screenNum)) {
                graphicsConfig = (X11GraphicsConfig)newDev.getConfigurations()[i];
                break;
            }
        }
        if (graphicsConfig == null) {
            graphicsConfig = (X11GraphicsConfig) GraphicsEnvironment.
                getLocalGraphicsEnvironment().
                getScreenDevices()[screenNum].
                getDefaultConfiguration();
        }
        return graphicsConfig;
    }
    protected boolean shouldFocusOnClick() {
        return true;
    }
    public void disableBackgroundErase() {
        eraseBackgroundDisabled = true;
    }
    protected boolean doEraseBackground() {
        return !eraseBackgroundDisabled;
    }
    public void setBackground(Color c) {
        boolean doRepaint = false;
        if( getPeerBackground() == null ||
           !getPeerBackground().equals( c ) ) {
            doRepaint = true;
        }
        super.setBackground(c);
        if( doRepaint ) {
            target.repaint();
        }
    }
}
