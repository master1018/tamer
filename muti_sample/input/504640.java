public class LayerRasterizer extends Rasterizer {
    public LayerRasterizer() {
        native_instance = nativeConstructor();
    }
    public void addLayer(Paint paint, float dx, float dy) {
        nativeAddLayer(native_instance, paint.mNativePaint, dx, dy);
    }
    public void addLayer(Paint paint) {
        nativeAddLayer(native_instance, paint.mNativePaint, 0, 0);
    }
    private static native int nativeConstructor();
    private static native void nativeAddLayer(int native_layer, int native_paint, float dx, float dy);
}
