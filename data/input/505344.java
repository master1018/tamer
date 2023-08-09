public class AvoidXfermode extends Xfermode {
    public enum Mode {
        AVOID   (0),    
        TARGET  (1);    
        Mode(int nativeInt) {
            this.nativeInt = nativeInt;
        }
        final int nativeInt;
    }
    public AvoidXfermode(int opColor, int tolerance, Mode mode) {
        if (tolerance < 0 || tolerance > 255) {
            throw new IllegalArgumentException("tolerance must be 0..255");
        }
        native_instance = nativeCreate(opColor, tolerance, mode.nativeInt);
    }
    private static native int nativeCreate(int opColor, int tolerance,
                                           int nativeMode);
}
