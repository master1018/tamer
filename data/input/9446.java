public abstract class ClassValue<T> {
    protected ClassValue() {
    }
    protected abstract T computeValue(Class<?> type);
    public T get(Class<?> type) {
        ClassValueMap map = getMap(type);
        if (map != null) {
            Object x = map.get(this);
            if (x != null) {
                return (T) map.unmaskNull(x);
            }
        }
        return setComputedValue(type);
    }
    public void remove(Class<?> type) {
        ClassValueMap map = getMap(type);
        if (map != null) {
            synchronized (map) {
                map.remove(this);
            }
        }
    }
    private static final AtomicInteger STORE_BARRIER = new AtomicInteger();
    private T setComputedValue(Class<?> type) {
        ClassValueMap map = getMap(type);
        if (map == null) {
            map = initializeMap(type);
        }
        T value = computeValue(type);
        STORE_BARRIER.lazySet(0);
        synchronized (map) {
            map.preInitializeEntry(this);
        }
        STORE_BARRIER.lazySet(0);
        synchronized (map) {
            value = (T) map.initializeEntry(this, value);
        }
        return value;
    }
    private static final WeakHashMap<Class<?>, ClassValueMap> ROOT
        = new WeakHashMap<Class<?>, ClassValueMap>();
    private static ClassValueMap getMap(Class<?> type) {
        type.getClass();  
        return ROOT.get(type);
    }
    private static ClassValueMap initializeMap(Class<?> type) {
        synchronized (ClassValue.class) {
            ClassValueMap map = ROOT.get(type);
            if (map == null)
                ROOT.put(type, map = new ClassValueMap());
            return map;
        }
    }
    static class ClassValueMap extends WeakHashMap<ClassValue, Object> {
        void preInitializeEntry(ClassValue key) {
            if (!this.containsKey(key))
                this.put(key, null);
        }
        Object initializeEntry(ClassValue key, Object value) {
            Object prior = this.get(key);
            if (prior != null) {
                return unmaskNull(prior);
            }
            this.put(key, maskNull(value));
            return value;
        }
        Object maskNull(Object x) {
            return x == null ? this : x;
        }
        Object unmaskNull(Object x) {
            return x == this ? null : x;
        }
    }
}
