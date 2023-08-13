public abstract class GlobalCursorManager {
    class NativeUpdater implements Runnable {
        boolean pending = false;
        public void run() {
            boolean shouldUpdate = false;
            synchronized (this) {
                if (pending) {
                    pending = false;
                    shouldUpdate = true;
                }
            }
            if (shouldUpdate) {
                _updateCursor(false);
            }
        }
        public void postIfNotPending(Component heavy, InvocationEvent in) {
            boolean shouldPost = false;
            synchronized (this) {
                if (!pending) {
                    pending = shouldPost = true;
                }
            }
            if (shouldPost) {
                SunToolkit.postEvent(SunToolkit.targetToAppContext(heavy), in);
            }
        }
    }
    private final NativeUpdater nativeUpdater = new NativeUpdater();
    private long lastUpdateMillis;
    private final Object lastUpdateLock = new Object();
    public void updateCursorImmediately() {
        synchronized (nativeUpdater) {
            nativeUpdater.pending = false;
        }
        _updateCursor(false);
    }
    public void updateCursorImmediately(InputEvent e) {
        boolean shouldUpdate;
        synchronized (lastUpdateLock) {
            shouldUpdate = (e.getWhen() >= lastUpdateMillis);
        }
        if (shouldUpdate) {
            _updateCursor(true);
        }
    }
    public void updateCursorLater(Component heavy) {
        nativeUpdater.postIfNotPending(heavy, new InvocationEvent
            (Toolkit.getDefaultToolkit(), nativeUpdater));
    }
    protected GlobalCursorManager() { }
    protected abstract void setCursor(Component comp, Cursor cursor,
                                      boolean useCache);
    protected abstract void getCursorPos(Point p);
    protected abstract Component findComponentAt(Container con, int x, int y);
    protected abstract Point getLocationOnScreen(Component com);
    protected abstract Component findHeavyweightUnderCursor(boolean useCache);
    private void _updateCursor(boolean useCache) {
        synchronized (lastUpdateLock) {
            lastUpdateMillis = System.currentTimeMillis();
        }
        Point queryPos = null, p = null;
        Component comp;
        try {
            comp = findHeavyweightUnderCursor(useCache);
            if (comp == null) {
                updateCursorOutOfJava();
                return;
            }
            if (comp instanceof Window) {
                p = AWTAccessor.getComponentAccessor().getLocation(comp);
            } else if (comp instanceof Container) {
                p = getLocationOnScreen(comp);
            }
            if (p != null) {
                queryPos = new Point();
                getCursorPos(queryPos);
                Component c = findComponentAt((Container)comp,
                                              queryPos.x - p.x,
                                              queryPos.y - p.y);
                if (c != null) {
                    comp = c;
                }
            }
            setCursor(comp, AWTAccessor.getComponentAccessor().getCursor(comp), useCache);
        } catch (IllegalComponentStateException e) {
        }
    }
    protected void updateCursorOutOfJava() {
    }
}
