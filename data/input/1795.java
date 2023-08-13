class XRootWindow extends XBaseWindow {
    private static XRootWindow xawtRootWindow = null;
    static XRootWindow getInstance() {
        XToolkit.awtLock();
        try {
            if (xawtRootWindow == null) {
                xawtRootWindow = new XRootWindow();
                xawtRootWindow.init(xawtRootWindow.getDelayedParams().delete(DELAYED));
            }
            return xawtRootWindow;
        } finally {
            XToolkit.awtUnlock();
        }
    }
    private XRootWindow() {
        super(new XCreateWindowParams(new Object[] {DELAYED, Boolean.TRUE}));
    }
    public void postInit(XCreateWindowParams params){
        super.postInit(params);
        setWMClass(getWMClass());
    }
    protected String getWMName() {
        return XToolkit.getAWTAppClassName();
    }
    protected String[] getWMClass() {
        return new String[] {XToolkit.getAWTAppClassName(), XToolkit.getAWTAppClassName()};
    }
    private static long getXRootWindow() {
        return getXAWTRootWindow().getWindow();
    }
}
