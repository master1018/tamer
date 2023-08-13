public class X11GraphicsDevice
    extends GraphicsDevice
    implements DisplayChangedListener
{
    int screen;
    HashMap x11ProxyKeyMap = new HashMap();
    private static AWTPermission fullScreenExclusivePermission;
    private static Boolean xrandrExtSupported;
    private final Object configLock = new Object();
    private SunDisplayChanger topLevels = new SunDisplayChanger();
    private DisplayMode origDisplayMode;
    private boolean shutdownHookRegistered;
    public X11GraphicsDevice(int screennum) {
        this.screen = screennum;
    }
    private static native void initIDs();
    static {
        if (!GraphicsEnvironment.isHeadless()) {
            initIDs();
        }
    }
    public int getScreen() {
        return screen;
    }
    public Object getProxyKeyFor(SurfaceType st) {
        synchronized (x11ProxyKeyMap) {
            Object o = x11ProxyKeyMap.get(st);
            if (o == null) {
                o = new Object();
                x11ProxyKeyMap.put(st, o);
            }
            return o;
        }
    }
    public native long getDisplay();
    public int getType() {
        return TYPE_RASTER_SCREEN;
    }
    public String getIDstring() {
        return ":0."+screen;
    }
    GraphicsConfiguration[] configs;
    GraphicsConfiguration defaultConfig;
    HashSet doubleBufferVisuals;
    public GraphicsConfiguration[] getConfigurations() {
        if (configs == null) {
            synchronized (configLock) {
                makeConfigurations();
            }
        }
        return configs.clone();
    }
    private void makeConfigurations() {
        if (configs == null) {
            int i = 1;  
            int num = getNumConfigs(screen);
            GraphicsConfiguration[] ret = new GraphicsConfiguration[num];
            if (defaultConfig == null) {
                ret [0] = getDefaultConfiguration();
            }
            else {
                ret [0] = defaultConfig;
            }
            boolean glxSupported = X11GraphicsEnvironment.isGLXAvailable();
            boolean xrenderSupported = X11GraphicsEnvironment.isXRenderAvailable();
            boolean dbeSupported = isDBESupported();
            if (dbeSupported && doubleBufferVisuals == null) {
                doubleBufferVisuals = new HashSet();
                getDoubleBufferVisuals(screen);
            }
            for ( ; i < num; i++) {
                int visNum = getConfigVisualId(i, screen);
                int depth = getConfigDepth (i, screen);
                if (glxSupported) {
                    ret[i] = GLXGraphicsConfig.getConfig(this, visNum);
                }
                if (ret[i] == null) {
                    boolean doubleBuffer =
                        (dbeSupported &&
                         doubleBufferVisuals.contains(Integer.valueOf(visNum)));
                    if (xrenderSupported) {
                        ret[i] = XRGraphicsConfig.getConfig(this, visNum, depth,                                getConfigColormap(i, screen),
                                doubleBuffer);
                    } else {
                       ret[i] = X11GraphicsConfig.getConfig(this, visNum, depth,
                              getConfigColormap(i, screen),
                              doubleBuffer);
                    }
                }
            }
            configs = ret;
        }
    }
    public native int getNumConfigs(int screen);
    public native int getConfigVisualId (int index, int screen);
    public native int getConfigDepth (int index, int screen);
    public native int getConfigColormap (int index, int screen);
    public static native boolean isDBESupported();
    private void addDoubleBufferVisual(int visNum) {
        doubleBufferVisuals.add(Integer.valueOf(visNum));
    }
    private native void getDoubleBufferVisuals(int screen);
    public GraphicsConfiguration getDefaultConfiguration() {
        if (defaultConfig == null) {
            synchronized (configLock) {
                makeDefaultConfiguration();
            }
        }
        return defaultConfig;
    }
    private void makeDefaultConfiguration() {
        if (defaultConfig == null) {
            int visNum = getConfigVisualId(0, screen);
            if (X11GraphicsEnvironment.isGLXAvailable()) {
                defaultConfig = GLXGraphicsConfig.getConfig(this, visNum);
                if (X11GraphicsEnvironment.isGLXVerbose()) {
                    if (defaultConfig != null) {
                        System.out.print("OpenGL pipeline enabled");
                    } else {
                        System.out.print("Could not enable OpenGL pipeline");
                    }
                    System.out.println(" for default config on screen " +
                                       screen);
                }
            }
            if (defaultConfig == null) {
                int depth = getConfigDepth(0, screen);
                boolean doubleBuffer = false;
                if (isDBESupported() && doubleBufferVisuals == null) {
                    doubleBufferVisuals = new HashSet();
                    getDoubleBufferVisuals(screen);
                    doubleBuffer =
                        doubleBufferVisuals.contains(Integer.valueOf(visNum));
                }
                if (X11GraphicsEnvironment.isXRenderAvailable()) {
                    if (X11GraphicsEnvironment.isXRenderVerbose()) {
                        System.out.println("XRender pipeline enabled");
                    }
                    defaultConfig = XRGraphicsConfig.getConfig(this, visNum,
                            depth, getConfigColormap(0, screen),
                            doubleBuffer);
                } else {
                    defaultConfig = X11GraphicsConfig.getConfig(this, visNum,
                                        depth, getConfigColormap(0, screen),
                                        doubleBuffer);
                }
            }
        }
    }
    private static native void enterFullScreenExclusive(long window);
    private static native void exitFullScreenExclusive(long window);
    private static native boolean initXrandrExtension();
    private static native DisplayMode getCurrentDisplayMode(int screen);
    private static native void enumDisplayModes(int screen,
                                                ArrayList<DisplayMode> modes);
    private static native void configDisplayMode(int screen,
                                                 int width, int height,
                                                 int displayMode);
    private static native void resetNativeData(int screen);
    private static synchronized boolean isXrandrExtensionSupported() {
        if (xrandrExtSupported == null) {
            xrandrExtSupported =
                Boolean.valueOf(initXrandrExtension());
        }
        return xrandrExtSupported.booleanValue();
    }
    @Override
    public boolean isFullScreenSupported() {
        boolean fsAvailable = (screen == 0) && isXrandrExtensionSupported();
        if (fsAvailable) {
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                if (fullScreenExclusivePermission == null) {
                    fullScreenExclusivePermission =
                        new AWTPermission("fullScreenExclusive");
                }
                try {
                    security.checkPermission(fullScreenExclusivePermission);
                } catch (SecurityException e) {
                    return false;
                }
            }
        }
        return fsAvailable;
    }
    @Override
    public boolean isDisplayChangeSupported() {
        return (isFullScreenSupported() && (getFullScreenWindow() != null));
    }
    private static void enterFullScreenExclusive(Window w) {
        X11ComponentPeer peer = (X11ComponentPeer)w.getPeer();
        if (peer != null) {
            enterFullScreenExclusive(peer.getContentWindow());
            peer.setFullScreenExclusiveModeState(true);
        }
    }
    private static void exitFullScreenExclusive(Window w) {
        X11ComponentPeer peer = (X11ComponentPeer)w.getPeer();
        if (peer != null) {
            peer.setFullScreenExclusiveModeState(false);
            exitFullScreenExclusive(peer.getContentWindow());
        }
    }
    @Override
    public synchronized void setFullScreenWindow(Window w) {
        Window old = getFullScreenWindow();
        if (w == old) {
            return;
        }
        boolean fsSupported = isFullScreenSupported();
        if (fsSupported && old != null) {
            exitFullScreenExclusive(old);
            setDisplayMode(origDisplayMode);
        }
        super.setFullScreenWindow(w);
        if (fsSupported && w != null) {
            if (origDisplayMode == null) {
                origDisplayMode = getDisplayMode();
            }
            enterFullScreenExclusive(w);
        }
    }
    private DisplayMode getDefaultDisplayMode() {
        GraphicsConfiguration gc = getDefaultConfiguration();
        Rectangle r = gc.getBounds();
        return new DisplayMode(r.width, r.height,
                               DisplayMode.BIT_DEPTH_MULTI,
                               DisplayMode.REFRESH_RATE_UNKNOWN);
    }
    @Override
    public synchronized DisplayMode getDisplayMode() {
        if (isFullScreenSupported()) {
            return getCurrentDisplayMode(screen);
        } else {
            if (origDisplayMode == null) {
                origDisplayMode = getDefaultDisplayMode();
            }
            return origDisplayMode;
        }
    }
    @Override
    public synchronized DisplayMode[] getDisplayModes() {
        if (!isFullScreenSupported()) {
            return super.getDisplayModes();
        }
        ArrayList<DisplayMode> modes = new ArrayList<DisplayMode>();
        enumDisplayModes(screen, modes);
        DisplayMode[] retArray = new DisplayMode[modes.size()];
        return modes.toArray(retArray);
    }
    @Override
    public synchronized void setDisplayMode(DisplayMode dm) {
        if (!isDisplayChangeSupported()) {
            super.setDisplayMode(dm);
            return;
        }
        Window w = getFullScreenWindow();
        if (w == null) {
            throw new IllegalStateException("Must be in fullscreen mode " +
                                            "in order to set display mode");
        }
        if (getDisplayMode().equals(dm)) {
            return;
        }
        if (dm == null ||
            (dm = getMatchingDisplayMode(dm)) == null)
        {
            throw new IllegalArgumentException("Invalid display mode");
        }
        if (!shutdownHookRegistered) {
            shutdownHookRegistered = true;
            PrivilegedAction<Void> a = new PrivilegedAction<Void>() {
                public Void run() {
                    ThreadGroup mainTG = Thread.currentThread().getThreadGroup();
                    ThreadGroup parentTG = mainTG.getParent();
                    while (parentTG != null) {
                        mainTG = parentTG;
                        parentTG = mainTG.getParent();
                    }
                    Runnable r = new Runnable() {
                            public void run() {
                                Window old = getFullScreenWindow();
                                if (old != null) {
                                    exitFullScreenExclusive(old);
                                    setDisplayMode(origDisplayMode);
                                }
                            }
                        };
                    Thread t = new Thread(mainTG, r,"Display-Change-Shutdown-Thread-"+screen);
                    t.setContextClassLoader(null);
                    Runtime.getRuntime().addShutdownHook(t);
                    return null;
                }
            };
            AccessController.doPrivileged(a);
        }
        configDisplayMode(screen,
                          dm.getWidth(), dm.getHeight(),
                          dm.getRefreshRate());
        w.setBounds(0, 0, dm.getWidth(), dm.getHeight());
        ((X11GraphicsEnvironment)
         GraphicsEnvironment.getLocalGraphicsEnvironment()).displayChanged();
    }
    private synchronized DisplayMode getMatchingDisplayMode(DisplayMode dm) {
        if (!isDisplayChangeSupported()) {
            return null;
        }
        DisplayMode[] modes = getDisplayModes();
        for (DisplayMode mode : modes) {
            if (dm.equals(mode) ||
                (dm.getRefreshRate() == DisplayMode.REFRESH_RATE_UNKNOWN &&
                 dm.getWidth() == mode.getWidth() &&
                 dm.getHeight() == mode.getHeight() &&
                 dm.getBitDepth() == mode.getBitDepth()))
            {
                return mode;
            }
        }
        return null;
    }
    public synchronized void displayChanged() {
        defaultConfig = null;
        configs = null;
        doubleBufferVisuals = null;
        resetNativeData(screen);
        topLevels.notifyListeners();
    }
    public void paletteChanged() {
    }
    public void addDisplayChangedListener(DisplayChangedListener client) {
        topLevels.add(client);
    }
    public void removeDisplayChangedListener(DisplayChangedListener client) {
        topLevels.remove(client);
    }
    public String toString() {
        return ("X11GraphicsDevice[screen="+screen+"]");
    }
}
