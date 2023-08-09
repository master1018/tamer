public class PixelXorXfermode extends Xfermode {
    public PixelXorXfermode(int opColor) {
        native_instance = nativeCreate(opColor);
    }
    private static native int nativeCreate(int opColor);
}
