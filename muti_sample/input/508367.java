public class PorterDuffXfermode extends Xfermode {
    public PorterDuffXfermode(PorterDuff.Mode mode) {
        native_instance = nativeCreateXfermode(mode.nativeInt);
    }
    private static native int nativeCreateXfermode(int mode);
}
