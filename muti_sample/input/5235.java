final class KeyCache {
    private final Cache strongCache;
    private WeakReference<Map<Key,P11Key>> cacheReference;
    KeyCache() {
        strongCache = Cache.newHardMemoryCache(16);
    }
    private static final class IdentityWrapper {
        final Object obj;
        IdentityWrapper(Object obj) {
            this.obj = obj;
        }
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof IdentityWrapper == false) {
                return false;
            }
            IdentityWrapper other = (IdentityWrapper)o;
            return this.obj == other.obj;
        }
        public int hashCode() {
            return System.identityHashCode(obj);
        }
    }
    synchronized P11Key get(Key key) {
        P11Key p11Key = (P11Key)strongCache.get(new IdentityWrapper(key));
        if (p11Key != null) {
            return p11Key;
        }
        Map<Key,P11Key> map =
                (cacheReference == null) ? null : cacheReference.get();
        if (map == null) {
            return null;
        }
        return map.get(key);
    }
    synchronized void put(Key key, P11Key p11Key) {
        strongCache.put(new IdentityWrapper(key), p11Key);
        Map<Key,P11Key> map =
                (cacheReference == null) ? null : cacheReference.get();
        if (map == null) {
            map = new IdentityHashMap<Key,P11Key>();
            cacheReference = new WeakReference<Map<Key,P11Key>>(map);
        }
        map.put(key, p11Key);
    }
}
