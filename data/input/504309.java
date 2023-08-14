public class SumPathEffect extends PathEffect {
    public SumPathEffect(PathEffect first, PathEffect second) {
        native_instance = nativeCreate(first.native_instance,
                                       second.native_instance);
    }
    private static native int nativeCreate(int first, int second);
}
