public class Win32GraphicsEnvironment
    extends SunGraphicsEnvironment
{
    static {
        WToolkit.loadLibraries();
        WindowsFlags.initFlags();
        initDisplayWrapper();
        SurfaceManagerFactory.setInstance(new WindowsSurfaceManagerFactory());
    }
    private static native void initDisplay();
    private static boolean displayInitialized;      
    public static void initDisplayWrapper() {
        if (!displayInitialized) {
            displayInitialized = true;
            initDisplay();
        }
    }
    public Win32GraphicsEnvironment() {
    }
    protected native int getNumScreens();
    protected native int getDefaultScreen();
    public GraphicsDevice getDefaultScreenDevice() {
        return getScreenDevices()[getDefaultScreen()];
    }
    public native int getXResolution();
    public native int getYResolution();
    private ArrayList<WeakReference<Win32GraphicsDevice>> oldDevices;
    @Override
    public void displayChanged() {
        GraphicsDevice newDevices[] = new GraphicsDevice[getNumScreens()];
        GraphicsDevice oldScreens[] = screens;
        if (oldScreens != null) {
            for (int i = 0; i < oldScreens.length; i++) {
                if (!(screens[i] instanceof Win32GraphicsDevice)) {
                    assert (false) : oldScreens[i];
                    continue;
                }
                Win32GraphicsDevice gd = (Win32GraphicsDevice)oldScreens[i];
                if (!gd.isValid()) {
                    if (oldDevices == null) {
                        oldDevices =
                            new ArrayList<WeakReference<Win32GraphicsDevice>>();
                    }
                    oldDevices.add(new WeakReference<Win32GraphicsDevice>(gd));
                } else if (i < newDevices.length) {
                    newDevices[i] = gd;
                }
            }
            oldScreens = null;
        }
        for (int i = 0; i < newDevices.length; i++) {
            if (newDevices[i] == null) {
                newDevices[i] = makeScreenDevice(i);
            }
        }
        screens = newDevices;
        for (GraphicsDevice gd : screens) {
            if (gd instanceof DisplayChangedListener) {
                ((DisplayChangedListener)gd).displayChanged();
            }
        }
        if (oldDevices != null) {
            int defScreen = getDefaultScreen();
            for (ListIterator<WeakReference<Win32GraphicsDevice>> it =
                    oldDevices.listIterator(); it.hasNext();)
            {
                Win32GraphicsDevice gd = it.next().get();
                if (gd != null) {
                    gd.invalidate(defScreen);
                    gd.displayChanged();
                } else {
                    it.remove();
                }
            }
        }
        WToolkit.resetGC();
        displayChanger.notifyListeners();
    }
    protected GraphicsDevice makeScreenDevice(int screennum) {
        GraphicsDevice device = null;
        if (WindowsFlags.isD3DEnabled()) {
            device = D3DGraphicsDevice.createDevice(screennum);
        }
        if (device == null) {
            device = new Win32GraphicsDevice(screennum);
        }
        return device;
    }
    public boolean isDisplayLocal() {
        return true;
    }
    @Override
    public boolean isFlipStrategyPreferred(ComponentPeer peer) {
        GraphicsConfiguration gc;
        if (peer != null && (gc = peer.getGraphicsConfiguration()) != null) {
            GraphicsDevice gd = gc.getDevice();
            if (gd instanceof D3DGraphicsDevice) {
                return ((D3DGraphicsDevice)gd).isD3DEnabledOnDevice();
            }
        }
        return false;
    }
    private static volatile boolean isDWMCompositionEnabled;
    public static boolean isDWMCompositionEnabled() {
        return isDWMCompositionEnabled;
    }
    private static void dwmCompositionChanged(boolean enabled) {
        isDWMCompositionEnabled = enabled;
    }
    public static native boolean isVistaOS();
}
