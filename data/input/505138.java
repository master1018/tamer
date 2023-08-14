public class PaintFlagsDrawFilter extends DrawFilter {
    public PaintFlagsDrawFilter(int clearBits, int setBits) {
        mNativeInt = nativeConstructor(clearBits, setBits);
    }
    private static native int nativeConstructor(int clearBits, int setBits);
}
