public final class HeadlessToolkit extends ToolkitImpl {
    @Override
    public Dimension getBestCursorSize(int prefWidth, int prefHeight) throws HeadlessException {
        throw new HeadlessException();
    }
    @Override
    public ColorModel getColorModel() throws HeadlessException {
        throw new HeadlessException();
    }
    @Override
    public GraphicsFactory getGraphicsFactory() throws HeadlessException {
        throw new HeadlessException();
    }
    @Override
    public boolean getLockingKeyState(int keyCode) throws UnsupportedOperationException {
        throw new HeadlessException();
    }
    @Override
    public int getMaximumCursorColors() throws HeadlessException {
        throw new HeadlessException();
    }
    @Override
    public int getMenuShortcutKeyMask() throws HeadlessException {
        throw new HeadlessException();
    }
    @Override
    public Insets getScreenInsets(GraphicsConfiguration gc) throws HeadlessException {
        throw new HeadlessException();
    }
    @Override
    public int getScreenResolution() throws HeadlessException {
        throw new HeadlessException();
    }
    @Override
    public Dimension getScreenSize() throws HeadlessException {
        throw new HeadlessException();
    }
    @Override
    protected void init() {
        lockAWT();
        try {
            ComponentInternals.setComponentInternals(new ComponentInternalsImpl());
            desktopProperties = new HashMap<String, Object>();
            dispatchThread.start();
        } finally {
            unlockAWT();
        }
    }
    @Override
    public boolean isDynamicLayoutActive() throws HeadlessException {
        throw new HeadlessException();
    }
    @Override
    protected boolean isDynamicLayoutSet() throws HeadlessException {
        throw new HeadlessException();
    }
    @Override
    public boolean isFrameStateSupported(int state) throws HeadlessException {
        throw new HeadlessException();
    }
    @Override
    protected void loadSystemColors(int[] systemColors) throws HeadlessException {
        throw new HeadlessException();
    }
    @Override
    public Map<java.awt.font.TextAttribute, ?> mapInputMethodHighlight(
            InputMethodHighlight highlight) throws HeadlessException {
        throw new HeadlessException();
    }
    @Override
    Map<java.awt.font.TextAttribute, ?> mapInputMethodHighlightImpl(InputMethodHighlight highlight)
            throws HeadlessException {
        throw new HeadlessException();
    }
    @Override
    public void setDynamicLayout(boolean dynamic) throws HeadlessException {
        throw new HeadlessException();
    }
    @Override
    public void setLockingKeyState(int keyCode, boolean on) throws UnsupportedOperationException {
        throw new HeadlessException();
    }
}
