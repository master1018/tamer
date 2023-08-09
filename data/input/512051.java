public abstract class GraphicsDevice {
    private DisplayMode displayMode;
    public static final int TYPE_IMAGE_BUFFER = 2;
    public static final int TYPE_PRINTER = 1;
    public static final int TYPE_RASTER_SCREEN = 0;
    protected GraphicsDevice() {
        displayMode = new DisplayMode(0, 0, DisplayMode.BIT_DEPTH_MULTI,
                DisplayMode.REFRESH_RATE_UNKNOWN);
    }
    public abstract GraphicsConfiguration[] getConfigurations();
    public abstract GraphicsConfiguration getDefaultConfiguration();
    public abstract String getIDstring();
    public abstract int getType();
    public int getAvailableAcceleratedMemory() {
        return 0;
    }
    public DisplayMode getDisplayMode() {
        return displayMode;
    }
    public DisplayMode[] getDisplayModes() {
        DisplayMode[] dms = {
            displayMode
        };
        return dms;
    }
    public boolean isDisplayChangeSupported() {
        return false;
    }
    public boolean isFullScreenSupported() {
        return false;
    }
    public void setDisplayMode(DisplayMode dm) {
        if (!isDisplayChangeSupported()) {
            throw new UnsupportedOperationException(Messages.getString("awt.122")); 
        }
        DisplayMode[] dms = getDisplayModes();
        for (DisplayMode element : dms) {
            if (element.equals(dm)) {
                displayMode = dm;
                return;
            }
        }
        throw new IllegalArgumentException(Messages.getString("awt.123", dm)); 
    }
}
