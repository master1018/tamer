class WeakIdentityHashMap<K, V> {
    private WeakIdentityHashMap() {}
    static <K, V> WeakIdentityHashMap<K, V> make() {
        return new WeakIdentityHashMap<K, V>();
    }
    V get(K key) {
        expunge();
        WeakReference<K> keyref = makeReference(key);
        return map.get(keyref);
    }
    public V put(K key, V value) {
        expunge();
        if (key == null)
            throw new IllegalArgumentException("Null key");
        WeakReference<K> keyref = makeReference(key, refQueue);
        return map.put(keyref, value);
    }
    public V remove(K key) {
        expunge();
        WeakReference<K> keyref = makeReference(key);
        return map.remove(keyref);
    }
    private void expunge() {
        Reference<? extends K> ref;
        while ((ref = refQueue.poll()) != null)
            map.remove(ref);
    }
    private WeakReference<K> makeReference(K referent) {
        return new IdentityWeakReference<K>(referent);
    }
    private WeakReference<K> makeReference(K referent, ReferenceQueue<K> q) {
        return new IdentityWeakReference<K>(referent, q);
    }
    private static class IdentityWeakReference<T> extends WeakReference<T> {
        IdentityWeakReference(T o) {
            this(o, null);
        }
        IdentityWeakReference(T o, ReferenceQueue<T> q) {
            super(o, q);
            this.hashCode = (o == null) ? 0 : System.identityHashCode(o);
        }
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof IdentityWeakReference<?>))
                return false;
            IdentityWeakReference<?> wr = (IdentityWeakReference<?>) o;
            Object got = get();
            return (got != null && got == wr.get());
        }
        public int hashCode() {
            return hashCode;
        }
        private final int hashCode;
    }
    private Map<WeakReference<K>, V> map = newMap();
    private ReferenceQueue<K> refQueue = new ReferenceQueue<K>();
}
