public class Power
{
    private Power()
    {
    }
    public static final int PARTIAL_WAKE_LOCK = 1;
    public static final int FULL_WAKE_LOCK = 2;
    public static native void acquireWakeLock(int lock, String id);
    public static native void releaseWakeLock(String id);
    public static final int BRIGHTNESS_OFF = 0;
    public static final int BRIGHTNESS_DIM = 20;
    public static final int BRIGHTNESS_ON = 255;
    public static final int BRIGHTNESS_LOW_BATTERY = 10;
    public static final int LOW_BATTERY_THRESHOLD = 10;
    public static native int setScreenState(boolean on);
    public static native int setLastUserActivityTimeout(long ms);
    @Deprecated
    public static native void shutdown();
    public static void reboot(String reason) throws IOException
    {
        rebootNative(reason);
    }
    private static native void rebootNative(String reason) throws IOException ;
}
