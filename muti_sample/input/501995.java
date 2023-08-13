public class DashPathEffect extends PathEffect {
    public DashPathEffect(float intervals[], float phase) {
        if (intervals.length < 2) {
            throw new ArrayIndexOutOfBoundsException();
        }
        native_instance = nativeCreate(intervals, phase);
    }
    private static native int nativeCreate(float intervals[], float phase);
}
