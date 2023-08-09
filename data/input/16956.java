public abstract class GraphicsDevice {
    private Window fullScreenWindow;
    private AppContext fullScreenAppContext; 
    private final Object fsAppContextLock = new Object();
    private Rectangle windowedModeBounds;
    protected GraphicsDevice() {
    }
    public final static int TYPE_RASTER_SCREEN          = 0;
    public final static int TYPE_PRINTER                = 1;
    public final static int TYPE_IMAGE_BUFFER           = 2;
    public static enum WindowTranslucency {
        PERPIXEL_TRANSPARENT,
        TRANSLUCENT,
        PERPIXEL_TRANSLUCENT;
    }
    public abstract int getType();
    public abstract String getIDstring();
    public abstract GraphicsConfiguration[] getConfigurations();
    public abstract GraphicsConfiguration getDefaultConfiguration();
    public GraphicsConfiguration
           getBestConfiguration(GraphicsConfigTemplate gct) {
        GraphicsConfiguration[] configs = getConfigurations();
        return gct.getBestConfiguration(configs);
    }
    public boolean isFullScreenSupported() {
        return false;
    }
    public void setFullScreenWindow(Window w) {
        if (w != null) {
            if (w.getShape() != null) {
                w.setShape(null);
            }
            if (w.getOpacity() < 1.0f) {
                w.setOpacity(1.0f);
            }
            if (!w.isOpaque()) {
                Color bgColor = w.getBackground();
                bgColor = new Color(bgColor.getRed(), bgColor.getGreen(),
                                    bgColor.getBlue(), 255);
                w.setBackground(bgColor);
            }
        }
        if (fullScreenWindow != null && windowedModeBounds != null) {
            if (windowedModeBounds.width  == 0) windowedModeBounds.width  = 1;
            if (windowedModeBounds.height == 0) windowedModeBounds.height = 1;
            fullScreenWindow.setBounds(windowedModeBounds);
        }
        synchronized (fsAppContextLock) {
            if (w == null) {
                fullScreenAppContext = null;
            } else {
                fullScreenAppContext = AppContext.getAppContext();
            }
            fullScreenWindow = w;
        }
        if (fullScreenWindow != null) {
            windowedModeBounds = fullScreenWindow.getBounds();
            Rectangle screenBounds = getDefaultConfiguration().getBounds();
            fullScreenWindow.setBounds(screenBounds.x, screenBounds.y,
                                       screenBounds.width, screenBounds.height);
            fullScreenWindow.setVisible(true);
            fullScreenWindow.toFront();
        }
    }
    public Window getFullScreenWindow() {
        Window returnWindow = null;
        synchronized (fsAppContextLock) {
            if (fullScreenAppContext == AppContext.getAppContext()) {
                returnWindow = fullScreenWindow;
            }
        }
        return returnWindow;
    }
    public boolean isDisplayChangeSupported() {
        return false;
    }
    public void setDisplayMode(DisplayMode dm) {
        throw new UnsupportedOperationException("Cannot change display mode");
    }
    public DisplayMode getDisplayMode() {
        GraphicsConfiguration gc = getDefaultConfiguration();
        Rectangle r = gc.getBounds();
        ColorModel cm = gc.getColorModel();
        return new DisplayMode(r.width, r.height, cm.getPixelSize(), 0);
    }
    public DisplayMode[] getDisplayModes() {
        return new DisplayMode[] { getDisplayMode() };
    }
    public int getAvailableAcceleratedMemory() {
        return -1;
    }
    public boolean isWindowTranslucencySupported(WindowTranslucency translucencyKind) {
        switch (translucencyKind) {
            case PERPIXEL_TRANSPARENT:
                return isWindowShapingSupported();
            case TRANSLUCENT:
                return isWindowOpacitySupported();
            case PERPIXEL_TRANSLUCENT:
                return isWindowPerpixelTranslucencySupported();
        }
        return false;
    }
    static boolean isWindowShapingSupported() {
        Toolkit curToolkit = Toolkit.getDefaultToolkit();
        if (!(curToolkit instanceof SunToolkit)) {
            return false;
        }
        return ((SunToolkit)curToolkit).isWindowShapingSupported();
    }
    static boolean isWindowOpacitySupported() {
        Toolkit curToolkit = Toolkit.getDefaultToolkit();
        if (!(curToolkit instanceof SunToolkit)) {
            return false;
        }
        return ((SunToolkit)curToolkit).isWindowOpacitySupported();
    }
    boolean isWindowPerpixelTranslucencySupported() {
        Toolkit curToolkit = Toolkit.getDefaultToolkit();
        if (!(curToolkit instanceof SunToolkit)) {
            return false;
        }
        if (!((SunToolkit)curToolkit).isWindowTranslucencySupported()) {
            return false;
        }
        return getTranslucencyCapableGC() != null;
    }
    GraphicsConfiguration getTranslucencyCapableGC() {
        GraphicsConfiguration defaultGC = getDefaultConfiguration();
        if (defaultGC.isTranslucencyCapable()) {
            return defaultGC;
        }
        GraphicsConfiguration[] configs = getConfigurations();
        for (int j = 0; j < configs.length; j++) {
            if (configs[j].isTranslucencyCapable()) {
                return configs[j];
            }
        }
        return null;
    }
}
