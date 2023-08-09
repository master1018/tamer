public abstract class Cache {
    protected Cache() {
    }
    public abstract int size();
    public abstract void clear();
    public abstract void put(Object key, Object value);
    public abstract Object get(Object key);
    public abstract void remove(Object key);
    public abstract void setCapacity(int size);
    public abstract void setTimeout(int timeout);
    public abstract void accept(CacheVisitor visitor);
    public static Cache newSoftMemoryCache(int size) {
        return new MemoryCache(true, size);
    }
    public static Cache newSoftMemoryCache(int size, int timeout) {
        return new MemoryCache(true, size, timeout);
    }
    public static Cache newHardMemoryCache(int size) {
        return new MemoryCache(false, size);
    }
    public static Cache newNullCache() {
        return NullCache.INSTANCE;
    }
    public static Cache newHardMemoryCache(int size, int timeout) {
        return new MemoryCache(false, size, timeout);
    }
    public static class EqualByteArray {
        private final byte[] b;
        private volatile int hash;
        public EqualByteArray(byte[] b) {
            this.b = b;
        }
        public int hashCode() {
            int h = hash;
            if (h == 0) {
                h = b.length + 1;
                for (int i = 0; i < b.length; i++) {
                    h += (b[i] & 0xff) * 37;
                }
                hash = h;
            }
            return h;
        }
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj instanceof EqualByteArray == false) {
                return false;
            }
            EqualByteArray other = (EqualByteArray)obj;
            return Arrays.equals(this.b, other.b);
        }
    }
    public interface CacheVisitor {
        public void visit(Map<Object, Object> map);
    }
}
class NullCache extends Cache {
    final static Cache INSTANCE = new NullCache();
    private NullCache() {
    }
    public int size() {
        return 0;
    }
    public void clear() {
    }
    public void put(Object key, Object value) {
    }
    public Object get(Object key) {
        return null;
    }
    public void remove(Object key) {
    }
    public void setCapacity(int size) {
    }
    public void setTimeout(int timeout) {
    }
    public void accept(CacheVisitor visitor) {
    }
}
class MemoryCache extends Cache {
    private final static float LOAD_FACTOR = 0.75f;
    private final static boolean DEBUG = false;
    private final Map<Object, CacheEntry> cacheMap;
    private int maxSize;
    private long lifetime;
    private final ReferenceQueue queue;
    public MemoryCache(boolean soft, int maxSize) {
        this(soft, maxSize, 0);
    }
    public MemoryCache(boolean soft, int maxSize, int lifetime) {
        this.maxSize = maxSize;
        this.lifetime = lifetime * 1000;
        this.queue = soft ? new ReferenceQueue() : null;
        int buckets = (int)(maxSize / LOAD_FACTOR) + 1;
        cacheMap = new LinkedHashMap<Object, CacheEntry>(buckets,
                                                        LOAD_FACTOR, true);
    }
    private void emptyQueue() {
        if (queue == null) {
            return;
        }
        int startSize = cacheMap.size();
        while (true) {
            CacheEntry entry = (CacheEntry)queue.poll();
            if (entry == null) {
                break;
            }
            Object key = entry.getKey();
            if (key == null) {
                continue;
            }
            CacheEntry currentEntry = cacheMap.remove(key);
            if ((currentEntry != null) && (entry != currentEntry)) {
                cacheMap.put(key, currentEntry);
            }
        }
        if (DEBUG) {
            int endSize = cacheMap.size();
            if (startSize != endSize) {
                System.out.println("*** Expunged " + (startSize - endSize)
                        + " entries, " + endSize + " entries left");
            }
        }
    }
    private void expungeExpiredEntries() {
        emptyQueue();
        if (lifetime == 0) {
            return;
        }
        int cnt = 0;
        long time = System.currentTimeMillis();
        for (Iterator<CacheEntry> t = cacheMap.values().iterator();
                t.hasNext(); ) {
            CacheEntry entry = t.next();
            if (entry.isValid(time) == false) {
                t.remove();
                cnt++;
            }
        }
        if (DEBUG) {
            if (cnt != 0) {
                System.out.println("Removed " + cnt
                        + " expired entries, remaining " + cacheMap.size());
            }
        }
    }
    public synchronized int size() {
        expungeExpiredEntries();
        return cacheMap.size();
    }
    public synchronized void clear() {
        if (queue != null) {
            for (CacheEntry entry : cacheMap.values()) {
                entry.invalidate();
            }
            while (queue.poll() != null) {
            }
        }
        cacheMap.clear();
    }
    public synchronized void put(Object key, Object value) {
        emptyQueue();
        long expirationTime = (lifetime == 0) ? 0 :
                                        System.currentTimeMillis() + lifetime;
        CacheEntry newEntry = newEntry(key, value, expirationTime, queue);
        CacheEntry oldEntry = cacheMap.put(key, newEntry);
        if (oldEntry != null) {
            oldEntry.invalidate();
            return;
        }
        if (maxSize > 0 && cacheMap.size() > maxSize) {
            expungeExpiredEntries();
            if (cacheMap.size() > maxSize) { 
                Iterator<CacheEntry> t = cacheMap.values().iterator();
                CacheEntry lruEntry = t.next();
                if (DEBUG) {
                    System.out.println("** Overflow removal "
                        + lruEntry.getKey() + " | " + lruEntry.getValue());
                }
                t.remove();
                lruEntry.invalidate();
            }
        }
    }
    public synchronized Object get(Object key) {
        emptyQueue();
        CacheEntry entry = cacheMap.get(key);
        if (entry == null) {
            return null;
        }
        long time = (lifetime == 0) ? 0 : System.currentTimeMillis();
        if (entry.isValid(time) == false) {
            if (DEBUG) {
                System.out.println("Ignoring expired entry");
            }
            cacheMap.remove(key);
            return null;
        }
        return entry.getValue();
    }
    public synchronized void remove(Object key) {
        emptyQueue();
        CacheEntry entry = cacheMap.remove(key);
        if (entry != null) {
            entry.invalidate();
        }
    }
    public synchronized void setCapacity(int size) {
        expungeExpiredEntries();
        if (size > 0 && cacheMap.size() > size) {
            Iterator<CacheEntry> t = cacheMap.values().iterator();
            for (int i = cacheMap.size() - size; i > 0; i--) {
                CacheEntry lruEntry = t.next();
                if (DEBUG) {
                    System.out.println("** capacity reset removal "
                        + lruEntry.getKey() + " | " + lruEntry.getValue());
                }
                t.remove();
                lruEntry.invalidate();
            }
        }
        maxSize = size > 0 ? size : 0;
        if (DEBUG) {
            System.out.println("** capacity reset to " + size);
        }
    }
    public synchronized void setTimeout(int timeout) {
        emptyQueue();
        lifetime = timeout > 0 ? timeout * 1000L : 0L;
        if (DEBUG) {
            System.out.println("** lifetime reset to " + timeout);
        }
    }
    public synchronized void accept(CacheVisitor visitor) {
        expungeExpiredEntries();
        Map<Object, Object> cached = getCachedEntries();
        visitor.visit(cached);
    }
    private Map<Object, Object> getCachedEntries() {
        Map<Object,Object> kvmap = new HashMap<Object,Object>(cacheMap.size());
        for (CacheEntry entry : cacheMap.values()) {
            kvmap.put(entry.getKey(), entry.getValue());
        }
        return kvmap;
    }
    protected CacheEntry newEntry(Object key, Object value,
            long expirationTime, ReferenceQueue queue) {
        if (queue != null) {
            return new SoftCacheEntry(key, value, expirationTime, queue);
        } else {
            return new HardCacheEntry(key, value, expirationTime);
        }
    }
    private static interface CacheEntry {
        boolean isValid(long currentTime);
        void invalidate();
        Object getKey();
        Object getValue();
    }
    private static class HardCacheEntry implements CacheEntry {
        private Object key, value;
        private long expirationTime;
        HardCacheEntry(Object key, Object value, long expirationTime) {
            this.key = key;
            this.value = value;
            this.expirationTime = expirationTime;
        }
        public Object getKey() {
            return key;
        }
        public Object getValue() {
            return value;
        }
        public boolean isValid(long currentTime) {
            boolean valid = (currentTime <= expirationTime);
            if (valid == false) {
                invalidate();
            }
            return valid;
        }
        public void invalidate() {
            key = null;
            value = null;
            expirationTime = -1;
        }
    }
    private static class SoftCacheEntry
            extends SoftReference implements CacheEntry {
        private Object key;
        private long expirationTime;
        SoftCacheEntry(Object key, Object value, long expirationTime,
                ReferenceQueue queue) {
            super(value, queue);
            this.key = key;
            this.expirationTime = expirationTime;
        }
        public Object getKey() {
            return key;
        }
        public Object getValue() {
            return get();
        }
        public boolean isValid(long currentTime) {
            boolean valid = (currentTime <= expirationTime) && (get() != null);
            if (valid == false) {
                invalidate();
            }
            return valid;
        }
        public void invalidate() {
            clear();
            key = null;
            expirationTime = -1;
        }
    }
}
