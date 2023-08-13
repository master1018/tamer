public class EnqueueNullRef {
    static void test(ReferenceQueue q, Reference r) {
        if (!r.enqueue())
            throw new RuntimeException("Enqueue operation failed");
    }
    public static void main(String[] args) {
        ReferenceQueue q = new ReferenceQueue();
        test(q, new WeakReference(null, q));
        test(q, new SoftReference(null, q));
        test(q, new PhantomReference(null, q));
    }
}
