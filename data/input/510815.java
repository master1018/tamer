public final class NativeUtils {
    private NativeUtils() {
    }
    public static native boolean nativeScrollRect(Canvas canvas, Rect src,
            int dx, int dy);
}
