public final class DisplayMode {
    private final int width;
    private final int height;
    private final int bitDepth;
    private final int refreshRate;
    public static final int BIT_DEPTH_MULTI = -1;
    public static final int REFRESH_RATE_UNKNOWN = 0;
    public DisplayMode(int width, int height, int bitDepth, int refreshRate) {
        this.width = width;
        this.height = height;
        this.bitDepth = bitDepth;
        this.refreshRate = refreshRate;
    }
    @Override
    public boolean equals(Object dm) {
        if (dm instanceof DisplayMode) {
            return equals((DisplayMode)dm);
        }
        return false;
    }
    public boolean equals(DisplayMode dm) {
        if (dm == null) {
            return false;
        }
        if (dm.bitDepth != bitDepth) {
            return false;
        }
        if (dm.refreshRate != refreshRate) {
            return false;
        }
        if (dm.width != width) {
            return false;
        }
        if (dm.height != height) {
            return false;
        }
        return true;
    }
    public int getBitDepth() {
        return bitDepth;
    }
    public int getHeight() {
        return height;
    }
    public int getRefreshRate() {
        return refreshRate;
    }
    public int getWidth() {
        return width;
    }
}
