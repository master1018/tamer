public class SoftReference<T> extends Reference<T> {
    public SoftReference(T r) {
        super();
        referent = r;
    }
    public SoftReference(T r, ReferenceQueue<? super T> q) {
        super();
        referent = r;
        queue = q;
    }
}
