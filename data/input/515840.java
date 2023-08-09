public class CornerPathEffect extends PathEffect {
    public CornerPathEffect(float radius) {
        native_instance = nativeCreate(radius);
    }
    private static native int nativeCreate(float radius);
}
