public class GLES10Ext {
    native private static void _nativeClassInit();
    static {
	    _nativeClassInit();
    }
    public static native int glQueryMatrixxOES(
        int[] mantissa,
        int mantissaOffset,
        int[] exponent,
        int exponentOffset
    );
    public static native int glQueryMatrixxOES(
        java.nio.IntBuffer mantissa,
        java.nio.IntBuffer exponent
    );
}
