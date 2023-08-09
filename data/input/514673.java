public abstract class AbstractCache<K, V> {
    private static final String TAG = "AbstractCache";
    private static final boolean DEBUG = false;
    private static final boolean LOCAL_LOGV = DEBUG ? Config.LOGD : Config.LOGV;
    private static final int MAX_CACHED_ITEMS  = 500;
    private final HashMap<K, CacheEntry<V>> mCacheMap;
    protected AbstractCache() {
        mCacheMap = new HashMap<K, CacheEntry<V>>();
    }
    public boolean put(K key, V value) {
        if (LOCAL_LOGV) {
            Log.v(TAG, "Trying to put " + key + " into cache.");
        }
        if (mCacheMap.size() >= MAX_CACHED_ITEMS) {
            if (LOCAL_LOGV) {
                Log.v(TAG, "Failed! size limitation reached.");
            }
            return false;
        }
        if (key != null) {
            CacheEntry<V> cacheEntry = new CacheEntry<V>();
            cacheEntry.value = value;
            mCacheMap.put(key, cacheEntry);
            if (LOCAL_LOGV) {
                Log.v(TAG, key + " cached, " + mCacheMap.size() + " items total.");
            }
            return true;
        }
        return false;
    }
    public V get(K key) {
        if (LOCAL_LOGV) {
            Log.v(TAG, "Trying to get " + key + " from cache.");
        }
        if (key != null) {
            CacheEntry<V> cacheEntry = mCacheMap.get(key);
            if (cacheEntry != null) {
                cacheEntry.hit++;
                if (LOCAL_LOGV) {
                    Log.v(TAG, key + " hit " + cacheEntry.hit + " times.");
                }
                return cacheEntry.value;
            }
        }
        return null;
    }
    public V purge(K key) {
        if (LOCAL_LOGV) {
            Log.v(TAG, "Trying to purge " + key);
        }
        CacheEntry<V> v = mCacheMap.remove(key);
        if (LOCAL_LOGV) {
            Log.v(TAG, mCacheMap.size() + " items cached.");
        }
        return v != null ? v.value : null;
    }
    public void purgeAll() {
        if (LOCAL_LOGV) {
            Log.v(TAG, "Purging cache, " + mCacheMap.size()
                    + " items dropped.");
        }
        mCacheMap.clear();
    }
    public int size() {
        return mCacheMap.size();
    }
    private static class CacheEntry<V> {
        int hit;
        V value;
    }
}
