public abstract class Toolkit {
    private static final String RECOURCE_PATH = "org.apache.harmony.awt.resources.AWTProperties"; 
    private static final ResourceBundle properties = loadResources(RECOURCE_PATH);
    Dispatcher dispatcher;
    private EventQueueCore systemEventQueueCore;
    EventDispatchThread dispatchThread;
    NativeEventThread nativeThread;
    protected AWTEventsManager awtEventsManager;
    private class AWTTreeLock {
    }
    final Object awtTreeLock = new AWTTreeLock();
    private final Synchronizer synchronizer = ContextStorage.getSynchronizer();
    final ShutdownWatchdog shutdownWatchdog = new ShutdownWatchdog();
    final AutoNumber autoNumber = new AutoNumber();
    final AWTEvent.EventTypeLookup eventTypeLookup = new AWTEvent.EventTypeLookup();
    private boolean bDynamicLayoutSet = true;
    private final HashSet<String> userPropSet = new HashSet<String>();
    protected Map<String, Object> desktopProperties;
    protected PropertyChangeSupport desktopPropsSupport;
    private Object recentNativeWindowComponent;
    private WTK wtk;
    protected final class ComponentInternalsImpl extends ComponentInternals {
        @Override
        public void shutdown() {
            dispatchThread.shutdown();
        }
        @Override
        public void setDesktopProperty(String name, Object value) {
            Toolkit.this.setDesktopProperty(name, value);
        }
    }
    static void checkHeadless() throws HeadlessException {
        if (GraphicsEnvironment.getLocalGraphicsEnvironment().isHeadlessInstance())
            throw new HeadlessException();
    }
    final void lockAWT() {
        synchronizer.lock();
    }
    static final void staticLockAWT() {
        ContextStorage.getSynchronizer().lock();
    }
    final void unlockAWT() {
        synchronizer.unlock();
    }
    static final void staticUnlockAWT() {
        ContextStorage.getSynchronizer().unlock();
    }
    final void unsafeInvokeAndWait(Runnable runnable) throws InterruptedException,
            InvocationTargetException {
        synchronizer.storeStateAndFree();
        try {
            EventQueue.invokeAndWait(runnable);
        } finally {
            synchronizer.lockAndRestoreState();
        }
    }
    final Synchronizer getSynchronizer() {
        return synchronizer;
    }
    final WTK getWTK() {
        return wtk;
    }
    public static String getProperty(String propName, String defVal) {
        if (propName == null) {
            throw new NullPointerException(Messages.getString("awt.7D")); 
        }
        staticLockAWT();
        try {
            String retVal = null;
            if (properties != null) {
                try {
                    retVal = properties.getString(propName);
                } catch (MissingResourceException e) {
                } catch (ClassCastException e) {
                }
            }
            return (retVal == null) ? defVal : retVal;
        } finally {
            staticUnlockAWT();
        }
    }
    public static Toolkit getDefaultToolkit() {
        synchronized (ContextStorage.getContextLock()) {
            if (ContextStorage.shutdownPending()) {
                return null;
            }
            Toolkit defToolkit = ContextStorage.getDefaultToolkit();
            if (defToolkit != null) {
                return defToolkit;
            }
            staticLockAWT();
            try {
                defToolkit = GraphicsEnvironment.isHeadless() ? new HeadlessToolkit()
                        : new ToolkitImpl();
                ContextStorage.setDefaultToolkit(defToolkit);
                return defToolkit;
            } finally {
                staticUnlockAWT();
            }
        }
    }
    Font getDefaultFont() {
        return wtk.getSystemProperties().getDefaultFont();
    }
    private static ResourceBundle loadResources(String path) {
        try {
            return ResourceBundle.getBundle(path);
        } catch (MissingResourceException e) {
            return null;
        }
    }
    private static String getWTKClassName() {
        return "com.android.internal.awt.AndroidWTK";
    }
    Component getComponentById(long id) {
        if (id == 0) {
            return null;
        }
        return null;
    }
    public GraphicsFactory getGraphicsFactory() {
        return wtk.getGraphicsFactory();
    }
    public Toolkit() {
        init();
    }
    protected void init() {
        lockAWT();
        try {
            ComponentInternals.setComponentInternals(new ComponentInternalsImpl());
            new EventQueue(this); 
            dispatcher = new Dispatcher(this);
            final String className = getWTKClassName();
            desktopProperties = new HashMap<String, Object>();
            desktopPropsSupport = new PropertyChangeSupport(this);
            awtEventsManager = new AWTEventsManager();
            dispatchThread = new EventDispatchThread(this, dispatcher);
            nativeThread = new NativeEventThread();
            NativeEventThread.Init init = new NativeEventThread.Init() {
                public WTK init() {
                    wtk = createWTK(className);
                    wtk.getNativeEventQueue().setShutdownWatchdog(shutdownWatchdog);
                    synchronizer.setEnvironment(wtk, dispatchThread);
                    ContextStorage.setWTK(wtk);
                    return wtk;
                }
            };
            nativeThread.start(init);
            dispatchThread.start();
            wtk.getNativeEventQueue().awake();
        } finally {
            unlockAWT();
        }
    }
    public abstract void sync();
    public abstract int checkImage(Image a0, int a1, int a2, ImageObserver a3);
    public abstract Image createImage(ImageProducer a0);
    public abstract Image createImage(byte[] a0, int a1, int a2);
    public abstract Image createImage(URL a0);
    public abstract Image createImage(String a0);
    public abstract ColorModel getColorModel() throws HeadlessException;
    @Deprecated
    public abstract FontMetrics getFontMetrics(Font font);
    public abstract boolean prepareImage(Image a0, int a1, int a2, ImageObserver a3);
    public abstract void beep();
    @Deprecated
    public abstract String[] getFontList();
    @Deprecated
    protected abstract FontPeer getFontPeer(String a0, int a1);
    public abstract Image getImage(String a0);
    public abstract Image getImage(URL a0);
    public abstract int getScreenResolution() throws HeadlessException;
    public abstract Dimension getScreenSize() throws HeadlessException;
    protected abstract EventQueue getSystemEventQueueImpl();
    public abstract Map<java.awt.font.TextAttribute, ?> mapInputMethodHighlight(
            InputMethodHighlight highlight) throws HeadlessException;
    Map<java.awt.font.TextAttribute, ?> mapInputMethodHighlightImpl(InputMethodHighlight highlight)
            throws HeadlessException {
        HashMap<java.awt.font.TextAttribute, ?> map = new HashMap<java.awt.font.TextAttribute, Object>();
        wtk.getSystemProperties().mapInputMethodHighlight(highlight, map);
        return Collections.<java.awt.font.TextAttribute, Object> unmodifiableMap(map);
    }
    public void addPropertyChangeListener(String propName, PropertyChangeListener l) {
        lockAWT();
        try {
            if (desktopProperties.isEmpty()) {
                initializeDesktopProperties();
            }
        } finally {
            unlockAWT();
        }
        if (l != null) { 
            desktopPropsSupport.addPropertyChangeListener(propName, l);
        }
    }
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return desktopPropsSupport.getPropertyChangeListeners();
    }
    public PropertyChangeListener[] getPropertyChangeListeners(String propName) {
        return desktopPropsSupport.getPropertyChangeListeners(propName);
    }
    public void removePropertyChangeListener(String propName, PropertyChangeListener l) {
        desktopPropsSupport.removePropertyChangeListener(propName, l);
    }
    public Cursor createCustomCursor(Image img, Point hotSpot, String name)
            throws IndexOutOfBoundsException, HeadlessException {
        lockAWT();
        try {
            int w = img.getWidth(null), x = hotSpot.x;
            int h = img.getHeight(null), y = hotSpot.y;
            if (x < 0 || x >= w || y < 0 || y >= h) {
                throw new IndexOutOfBoundsException(Messages.getString("awt.7E")); 
            }
            return new Cursor(name, img, hotSpot);
        } finally {
            unlockAWT();
        }
    }
    public Dimension getBestCursorSize(int prefWidth, int prefHeight) throws HeadlessException {
        lockAWT();
        try {
            return wtk.getCursorFactory().getBestCursorSize(prefWidth, prefHeight);
        } finally {
            unlockAWT();
        }
    }
    public final Object getDesktopProperty(String propName) {
        lockAWT();
        try {
            if (desktopProperties.isEmpty()) {
                initializeDesktopProperties();
            }
            if (propName.equals("awt.dynamicLayoutSupported")) { 
                return Boolean.valueOf(isDynamicLayoutActive());
            }
            Object val = desktopProperties.get(propName);
            if (val == null) {
                val = lazilyLoadDesktopProperty(propName);
            }
            return val;
        } finally {
            unlockAWT();
        }
    }
    public boolean getLockingKeyState(int a0) throws UnsupportedOperationException,
            org.apache.harmony.luni.util.NotImplementedException {
        lockAWT();
        try {
        } finally {
            unlockAWT();
        }
        if (true) {
            throw new RuntimeException("Method is not implemented"); 
        }
        return true;
    }
    public int getMaximumCursorColors() throws HeadlessException {
        lockAWT();
        try {
            return wtk.getCursorFactory().getMaximumCursorColors();
        } finally {
            unlockAWT();
        }
    }
    public int getMenuShortcutKeyMask() throws HeadlessException {
        lockAWT();
        try {
            return InputEvent.CTRL_MASK;
        } finally {
            unlockAWT();
        }
    }
    public Insets getScreenInsets(GraphicsConfiguration gc) throws HeadlessException {
        if (gc == null) {
            throw new NullPointerException();
        }
        lockAWT();
        try {
            return new Insets(0, 0, 0, 0); 
        } finally {
            unlockAWT();
        }
    }
    public final EventQueue getSystemEventQueue() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkAwtEventQueueAccess();
        }
        return getSystemEventQueueImpl();
    }
    EventQueueCore getSystemEventQueueCore() {
        return systemEventQueueCore;
    }
    void setSystemEventQueueCore(EventQueueCore core) {
        systemEventQueueCore = core;
    }
    protected void initializeDesktopProperties() {
        lockAWT();
        try {
            wtk.getSystemProperties().init(desktopProperties);
        } finally {
            unlockAWT();
        }
    }
    public boolean isDynamicLayoutActive() throws HeadlessException {
        lockAWT();
        try {
            return true;
        } finally {
            unlockAWT();
        }
    }
    protected boolean isDynamicLayoutSet() throws HeadlessException {
        lockAWT();
        try {
            return bDynamicLayoutSet;
        } finally {
            unlockAWT();
        }
    }
    public boolean isFrameStateSupported(int state) throws HeadlessException {
        lockAWT();
        try {
            return wtk.getWindowFactory().isWindowStateSupported(state);
        } finally {
            unlockAWT();
        }
    }
    protected Object lazilyLoadDesktopProperty(String propName) {
        return null;
    }
    protected void loadSystemColors(int[] colors) throws HeadlessException {
        lockAWT();
        try {
        } finally {
            unlockAWT();
        }
    }
    protected final void setDesktopProperty(String propName, Object value) {
        Object oldVal;
        lockAWT();
        try {
            oldVal = getDesktopProperty(propName);
            userPropSet.add(propName);
            desktopProperties.put(propName, value);
        } finally {
            unlockAWT();
        }
        desktopPropsSupport.firePropertyChange(propName, oldVal, value);
    }
    public void setDynamicLayout(boolean dynamic) throws HeadlessException {
        lockAWT();
        try {
            bDynamicLayoutSet = dynamic;
        } finally {
            unlockAWT();
        }
    }
    public void setLockingKeyState(int a0, boolean a1) throws UnsupportedOperationException,
            org.apache.harmony.luni.util.NotImplementedException {
        lockAWT();
        try {
        } finally {
            unlockAWT();
        }
        if (true) {
            throw new RuntimeException("Method is not implemented"); 
        }
        return;
    }
    void onQueueEmpty() {
        throw new RuntimeException("Not implemented!");
    }
    private WTK createWTK(String clsName) {
        WTK newWTK = null;
        try {
            newWTK = (WTK)Class.forName(clsName).newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return newWTK;
    }
    boolean onWindowCreated(long winId) {
        return false;
    }
    NativeEventQueue getNativeEventQueue() {
        return wtk.getNativeEventQueue();
    }
    NativeCursor createNativeCursor(int type) {
        return wtk.getCursorFactory().getCursor(type);
    }
    NativeCursor createCustomNativeCursor(Image img, Point hotSpot, String name) {
        return wtk.getCursorFactory().createCustomCursor(img, hotSpot.x, hotSpot.y);
    }
    public void addAWTEventListener(AWTEventListener listener, long eventMask) {
        lockAWT();
        try {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkPermission(awtEventsManager.permission);
            }
            awtEventsManager.addAWTEventListener(listener, eventMask);
        } finally {
            unlockAWT();
        }
    }
    public void removeAWTEventListener(AWTEventListener listener) {
        lockAWT();
        try {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkPermission(awtEventsManager.permission);
            }
            awtEventsManager.removeAWTEventListener(listener);
        } finally {
            unlockAWT();
        }
    }
    public AWTEventListener[] getAWTEventListeners() {
        lockAWT();
        try {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkPermission(awtEventsManager.permission);
            }
            return awtEventsManager.getAWTEventListeners();
        } finally {
            unlockAWT();
        }
    }
    public AWTEventListener[] getAWTEventListeners(long eventMask) {
        lockAWT();
        try {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkPermission(awtEventsManager.permission);
            }
            return awtEventsManager.getAWTEventListeners(eventMask);
        } finally {
            unlockAWT();
        }
    }
    void dispatchAWTEvent(AWTEvent event) {
        awtEventsManager.dispatchAWTEvent(event);
    }
    final class AWTEventsManager {
        AWTPermission permission = new AWTPermission("listenToAllAWTEvents"); 
        private final AWTListenerList<AWTEventListenerProxy> listeners = new AWTListenerList<AWTEventListenerProxy>();
        void addAWTEventListener(AWTEventListener listener, long eventMask) {
            if (listener != null) {
                listeners.addUserListener(new AWTEventListenerProxy(eventMask, listener));
            }
        }
        void removeAWTEventListener(AWTEventListener listener) {
            if (listener != null) {
                for (AWTEventListenerProxy proxy : listeners.getUserListeners()) {
                    if (listener == proxy.getListener()) {
                        listeners.removeUserListener(proxy);
                        return;
                    }
                }
            }
        }
        AWTEventListener[] getAWTEventListeners() {
            HashSet<EventListener> listenersSet = new HashSet<EventListener>();
            for (AWTEventListenerProxy proxy : listeners.getUserListeners()) {
                listenersSet.add(proxy.getListener());
            }
            return listenersSet.toArray(new AWTEventListener[listenersSet.size()]);
        }
        AWTEventListener[] getAWTEventListeners(long eventMask) {
            HashSet<EventListener> listenersSet = new HashSet<EventListener>();
            for (AWTEventListenerProxy proxy : listeners.getUserListeners()) {
                if ((proxy.getEventMask() & eventMask) == eventMask) {
                    listenersSet.add(proxy.getListener());
                }
            }
            return listenersSet.toArray(new AWTEventListener[listenersSet.size()]);
        }
        void dispatchAWTEvent(AWTEvent event) {
            AWTEvent.EventDescriptor descriptor = eventTypeLookup.getEventDescriptor(event);
            if (descriptor == null) {
                return;
            }
            for (AWTEventListenerProxy proxy : listeners.getUserListeners()) {
                if ((proxy.getEventMask() & descriptor.eventMask) != 0) {
                    proxy.eventDispatched(event);
                }
            }
        }
    }
    static final class AutoNumber {
        int nextComponent = 0;
        int nextCanvas = 0;
        int nextPanel = 0;
        int nextWindow = 0;
        int nextFrame = 0;
        int nextDialog = 0;
        int nextButton = 0;
        int nextMenuComponent = 0;
        int nextLabel = 0;
        int nextCheckBox = 0;
        int nextScrollbar = 0;
        int nextScrollPane = 0;
        int nextList = 0;
        int nextChoice = 0;
        int nextFileDialog = 0;
        int nextTextArea = 0;
        int nextTextField = 0;
    }
    private class Lock {
    }
    private final Object lock = new Lock();
}
