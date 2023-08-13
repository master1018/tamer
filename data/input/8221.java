public class Win32GraphicsDevice extends GraphicsDevice implements
 DisplayChangedListener {
    int screen;
    ColorModel dynamicColorModel;   
    ColorModel colorModel;          
    protected GraphicsConfiguration[] configs;
    protected GraphicsConfiguration defaultConfig;
    private final String idString;
    protected String descString;
    private boolean valid;
    private SunDisplayChanger topLevels = new SunDisplayChanger();
    protected static boolean pfDisabled;
    private static AWTPermission fullScreenExclusivePermission;
    private DisplayMode defaultDisplayMode;
    private WindowListener fsWindowListener;
    static {
        String nopixfmt = (String)java.security.AccessController.doPrivileged(
            new sun.security.action.GetPropertyAction("sun.awt.nopixfmt"));
        pfDisabled = (nopixfmt != null);
        initIDs();
    }
    private static native void initIDs();
    native void initDevice(int screen);
    public Win32GraphicsDevice(int screennum) {
        this.screen = screennum;
        idString = "\\Display"+screen;
        descString = "Win32GraphicsDevice[screen=" + screen;
        valid = true;
        initDevice(screennum);
    }
    public int getType() {
        return TYPE_RASTER_SCREEN;
    }
    public int getScreen() {
        return screen;
    }
    public boolean isValid() {
        return valid;
    }
    protected void invalidate(int defaultScreen) {
        valid = false;
        screen = defaultScreen;
    }
    public String getIDstring() {
        return idString;
    }
    public GraphicsConfiguration[] getConfigurations() {
        if (configs==null) {
            if (WindowsFlags.isOGLEnabled() && isDefaultDevice()) {
                defaultConfig = getDefaultConfiguration();
                if (defaultConfig != null) {
                    configs = new GraphicsConfiguration[1];
                    configs[0] = defaultConfig;
                    return configs.clone();
                }
            }
            int max = getMaxConfigs(screen);
            int defaultPixID = getDefaultPixID(screen);
            Vector v = new Vector( max );
            if (defaultPixID == 0) {
                defaultConfig = Win32GraphicsConfig.getConfig(this,
                                                              defaultPixID);
                v.addElement(defaultConfig);
            }
            else {
                for (int i = 1; i <= max; i++) {
                    if (isPixFmtSupported(i, screen)) {
                        if (i == defaultPixID) {
                            defaultConfig = Win32GraphicsConfig.getConfig(
                             this, i);
                            v.addElement(defaultConfig);
                        }
                        else {
                            v.addElement(Win32GraphicsConfig.getConfig(
                             this, i));
                        }
                    }
                }
            }
            configs = new GraphicsConfiguration[v.size()];
            v.copyInto(configs);
        }
        return configs.clone();
    }
    protected int getMaxConfigs(int screen) {
        if (pfDisabled) {
            return 1;
        } else {
            return getMaxConfigsImpl(screen);
        }
    }
    private native int getMaxConfigsImpl(int screen);
    protected native boolean isPixFmtSupported(int index, int screen);
    protected int getDefaultPixID(int screen) {
        if (pfDisabled) {
            return 0;
        } else {
            return getDefaultPixIDImpl(screen);
        }
    }
    private native int getDefaultPixIDImpl(int screen);
    public GraphicsConfiguration getDefaultConfiguration() {
        if (defaultConfig == null) {
            if (WindowsFlags.isOGLEnabled() && isDefaultDevice()) {
                int defPixID = WGLGraphicsConfig.getDefaultPixFmt(screen);
                defaultConfig = WGLGraphicsConfig.getConfig(this, defPixID);
                if (WindowsFlags.isOGLVerbose()) {
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
                defaultConfig = Win32GraphicsConfig.getConfig(this, 0);
            }
        }
        return defaultConfig;
    }
    public String toString() {
        return valid ? descString + "]" : descString + ", removed]";
    }
    private boolean isDefaultDevice() {
        return (this ==
                GraphicsEnvironment.
                    getLocalGraphicsEnvironment().getDefaultScreenDevice());
    }
    private static boolean isFSExclusiveModeAllowed() {
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
        return true;
    }
    @Override
    public boolean isFullScreenSupported() {
        return isFSExclusiveModeAllowed();
    }
    @Override
    public synchronized void setFullScreenWindow(Window w) {
        Window old = getFullScreenWindow();
        if (w == old) {
            return;
        }
        if (!isFullScreenSupported()) {
            super.setFullScreenWindow(w);
            return;
        }
        if (old != null) {
            if (defaultDisplayMode != null) {
                setDisplayMode(defaultDisplayMode);
                defaultDisplayMode = null;
            }
            WWindowPeer peer = (WWindowPeer)old.getPeer();
            if (peer != null) {
                peer.setFullScreenExclusiveModeState(false);
                synchronized(peer) {
                    exitFullScreenExclusive(screen, peer);
                }
            }
            removeFSWindowListener(old);
        }
        super.setFullScreenWindow(w);
        if (w != null) {
            defaultDisplayMode = getDisplayMode();
            addFSWindowListener(w);
            WWindowPeer peer = (WWindowPeer)w.getPeer();
            if (peer != null) {
                synchronized(peer) {
                    enterFullScreenExclusive(screen, peer);
                }
                peer.setFullScreenExclusiveModeState(true);
            }
            peer.updateGC();
        }
    }
    protected native void enterFullScreenExclusive(int screen, WindowPeer w);
    protected native void exitFullScreenExclusive(int screen, WindowPeer w);
    @Override
    public boolean isDisplayChangeSupported() {
        return (isFullScreenSupported() && getFullScreenWindow() != null);
    }
    @Override
    public synchronized void setDisplayMode(DisplayMode dm) {
        if (!isDisplayChangeSupported()) {
            super.setDisplayMode(dm);
            return;
        }
        if (dm == null || (dm = getMatchingDisplayMode(dm)) == null) {
            throw new IllegalArgumentException("Invalid display mode");
        }
        if (getDisplayMode().equals(dm)) {
            return;
        }
        Window w = getFullScreenWindow();
        if (w != null) {
            WWindowPeer peer = (WWindowPeer)w.getPeer();
            configDisplayMode(screen, peer, dm.getWidth(), dm.getHeight(),
                dm.getBitDepth(), dm.getRefreshRate());
            Rectangle screenBounds = getDefaultConfiguration().getBounds();
            w.setBounds(screenBounds.x, screenBounds.y,
                        dm.getWidth(), dm.getHeight());
        } else {
            throw new IllegalStateException("Must be in fullscreen mode " +
                                            "in order to set display mode");
        }
    }
    protected native DisplayMode getCurrentDisplayMode(int screen);
    protected native void configDisplayMode(int screen, WindowPeer w, int width,
                                          int height, int bitDepth,
                                          int refreshRate);
    protected native void enumDisplayModes(int screen, ArrayList modes);
    @Override
    public synchronized DisplayMode getDisplayMode() {
        DisplayMode res = getCurrentDisplayMode(screen);
        return res;
    }
    @Override
    public synchronized DisplayMode[] getDisplayModes() {
        ArrayList modes = new ArrayList();
        enumDisplayModes(screen, modes);
        int listSize = modes.size();
        DisplayMode[] retArray = new DisplayMode[listSize];
        for (int i = 0; i < listSize; i++) {
            retArray[i] = (DisplayMode)modes.get(i);
        }
        return retArray;
    }
    protected synchronized DisplayMode getMatchingDisplayMode(DisplayMode dm) {
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
    public void displayChanged() {
        dynamicColorModel = null;
        defaultConfig = null;
        configs = null;
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
    private native ColorModel makeColorModel (int screen,
                                              boolean dynamic);
    public ColorModel getDynamicColorModel() {
        if (dynamicColorModel == null) {
            dynamicColorModel = makeColorModel(screen, true);
        }
        return dynamicColorModel;
    }
    public ColorModel getColorModel() {
        if (colorModel == null)  {
            colorModel = makeColorModel(screen, false);
        }
        return colorModel;
    }
    private static class Win32FSWindowAdapter extends WindowAdapter {
        private Win32GraphicsDevice device;
        private DisplayMode dm;
        Win32FSWindowAdapter(Win32GraphicsDevice device) {
            this.device = device;
        }
        private void setFSWindowsState(Window other, int state) {
            GraphicsDevice gds[] =
                    GraphicsEnvironment.getLocalGraphicsEnvironment().
                    getScreenDevices();
            if (other != null) {
                for (GraphicsDevice gd : gds) {
                    if (other == gd.getFullScreenWindow()) {
                        return;
                    }
                }
            }
            for (GraphicsDevice gd : gds) {
                Window fsw = gd.getFullScreenWindow();
                if (fsw instanceof Frame) {
                    ((Frame)fsw).setExtendedState(state);
                }
            }
        }
        @Override
        public void windowDeactivated(WindowEvent e) {
            setFSWindowsState(e.getOppositeWindow(), Frame.ICONIFIED);
        }
        @Override
        public void windowActivated(WindowEvent e) {
            setFSWindowsState(e.getOppositeWindow(), Frame.NORMAL);
        }
        @Override
        public void windowIconified(WindowEvent e) {
            DisplayMode ddm = device.defaultDisplayMode;
            if (ddm != null) {
                dm = device.getDisplayMode();
                device.setDisplayMode(ddm);
            }
        }
        @Override
        public void windowDeiconified(WindowEvent e) {
            if (dm != null) {
                device.setDisplayMode(dm);
                dm = null;
            }
        }
    }
    protected void addFSWindowListener(final Window w) {
        fsWindowListener = new Win32FSWindowAdapter(this);
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                w.addWindowListener(fsWindowListener);
            }
        });
    }
    protected void removeFSWindowListener(Window w) {
        w.removeWindowListener(fsWindowListener);
        fsWindowListener = null;
    }
}
