public class CacheMap<K, V> extends WeakHashMap<K, V> {
    public CacheMap(int nSoftReferences) {
        if (nSoftReferences < 0) {
            throw new IllegalArgumentException("nSoftReferences = " +
                                               nSoftReferences);
        }
        this.nSoftReferences = nSoftReferences;
    }
    public V put(K key, V value) {
        cache(key);
        return super.put(key, value);
    }
    public V get(Object key) {
        cache(Util.<K>cast(key));
        return super.get(key);
    }
    private void cache(K key) {
        Iterator<SoftReference<K>> it = cache.iterator();
        while (it.hasNext()) {
            SoftReference<K> sref = it.next();
            K key1 = sref.get();
            if (key1 == null)
                it.remove();
            else if (key.equals(key1)) {
                it.remove();
                cache.add(0, sref);
                return;
            }
        }
        int size = cache.size();
        if (size == nSoftReferences) {
            if (size == 0)
                return;  
            it.remove();
        }
        cache.add(0, new SoftReference<K>(key));
    }
    private final LinkedList<SoftReference<K>> cache =
            new LinkedList<SoftReference<K>>();
    private final int nSoftReferences;
}
