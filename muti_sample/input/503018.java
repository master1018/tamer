public class PhantomReference<T> extends Reference<T> {
    public PhantomReference(T r, ReferenceQueue<? super T> q) {
        super();
        referent = r;
        queue = q;
    }
    @Override
    public T get() {
        return null;
    }
}
