public class EmbossMaskFilter extends MaskFilter {
    public EmbossMaskFilter(float[] direction, float ambient, float specular, float blurRadius) {
        if (direction.length < 3) {
            throw new ArrayIndexOutOfBoundsException();
        }
        native_instance = nativeConstructor(direction, ambient, specular, blurRadius);
    }
    private static native int nativeConstructor(float[] direction, float ambient, float specular, float blurRadius);
}
