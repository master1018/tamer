public class ComposePathEffect extends PathEffect {
    public ComposePathEffect(PathEffect outerpe, PathEffect innerpe) {
        native_instance = nativeCreate(outerpe.native_instance,
                                       innerpe.native_instance);
    }
    private static native int nativeCreate(int outerpe, int innerpe);
}
