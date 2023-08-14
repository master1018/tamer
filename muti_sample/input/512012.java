public class WeakReference<T> extends Reference<T> {
    public WeakReference(T r) {
        super();
        referent = r;
    }
    public WeakReference(T r, ReferenceQueue<? super T> q) {
        super();
        referent = r;
        queue = q;
    }
}
