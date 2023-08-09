public class DiscretePathEffect extends PathEffect {
    public DiscretePathEffect(float segmentLength, float deviation) {
        native_instance = nativeCreate(segmentLength, deviation);
    }
    private static native int nativeCreate(float length, float deviation);
}
