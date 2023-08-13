public class BlurMaskFilter extends MaskFilter {
    public enum Blur {
        NORMAL(0),  
        SOLID(1),   
        OUTER(2),   
        INNER(3);   
        Blur(int value) {
            native_int = value;
        }
        final int native_int;
    }
    public BlurMaskFilter(float radius, Blur style) {
        native_instance = nativeConstructor(radius, style.native_int);
    }
    private static native int nativeConstructor(float radius, int style);
}
