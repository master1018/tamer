public class ColorMatrixColorFilter extends ColorFilter {
    public ColorMatrixColorFilter(ColorMatrix matrix) {
        native_instance = nativeColorMatrixFilter(matrix.getArray());
    }
    public ColorMatrixColorFilter(float[] array) {
        if (array.length < 20) {
            throw new ArrayIndexOutOfBoundsException();
        }
        native_instance = nativeColorMatrixFilter(array);
    }
    private static native int nativeColorMatrixFilter(float[] array);
}
