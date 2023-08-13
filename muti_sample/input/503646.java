public class RegionIterator {
    public RegionIterator(Region region) {
        mNativeIter = nativeConstructor(region.ni());
    }
    public final boolean next(Rect r) {
        if (r == null) {
            throw new NullPointerException("The Rect must be provided");
        }
        return nativeNext(mNativeIter, r);
    }
    protected void finalize() throws Throwable {
        nativeDestructor(mNativeIter);
    }
    private static native int nativeConstructor(int native_region);
    private static native void nativeDestructor(int native_iter);
    private static native boolean nativeNext(int native_iter, Rect r);
    private final int mNativeIter;
}
