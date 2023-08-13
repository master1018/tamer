public class ThreadLocal<T> {
    public ThreadLocal() {}
    @SuppressWarnings("unchecked")
    public T get() {
        Thread currentThread = Thread.currentThread();
        Values values = values(currentThread);
        if (values != null) {
            Object[] table = values.table;
            int index = hash & values.mask;
            if (this.reference == table[index]) {
                return (T) table[index + 1];
            }
        } else {
            values = initializeValues(currentThread);
        }
        return (T) values.getAfterMiss(this);
    }
    protected T initialValue() {
        return null;
    }
    public void set(T value) {
        Thread currentThread = Thread.currentThread();
        Values values = values(currentThread);
        if (values == null) {
            values = initializeValues(currentThread);
        }
        values.put(this, value);
    }
    public void remove() {
        Thread currentThread = Thread.currentThread();
        Values values = values(currentThread);
        if (values != null) {
            values.remove(this);
        }
    }
    Values initializeValues(Thread current) {
        return current.localValues = new Values();
    }
    Values values(Thread current) {
        return current.localValues;
    }
    private final Reference<ThreadLocal<T>> reference
            = new WeakReference<ThreadLocal<T>>(this);
    private static AtomicInteger hashCounter = new AtomicInteger(0);
    private final int hash = hashCounter.getAndAdd(0x61c88647 << 1);
    static class Values {
        private static final int INITIAL_SIZE = 16;
        private static final Object TOMBSTONE = new Object();
        private Object[] table;
        private int mask;
        private int size;
        private int tombstones;
        private int maximumLoad;
        private int clean;
        Values() {
            initializeTable(INITIAL_SIZE);
            this.size = 0;
            this.tombstones = 0;
        }
        Values(Values fromParent) {
            this.table = fromParent.table.clone();
            this.mask = fromParent.mask;
            this.size = fromParent.size;
            this.tombstones = fromParent.tombstones;
            this.maximumLoad = fromParent.maximumLoad;
            this.clean = fromParent.clean;
            inheritValues(fromParent);
        }
        @SuppressWarnings({"unchecked"})
        private void inheritValues(Values fromParent) {
            Object[] table = this.table;
            for (int i = table.length - 2; i >= 0; i -= 2) {
                Object k = table[i];
                if (k == null || k == TOMBSTONE) {
                    continue;
                }
                Reference<InheritableThreadLocal<?>> reference
                        = (Reference<InheritableThreadLocal<?>>) k;
                InheritableThreadLocal key = reference.get();
                if (key != null) {
                    table[i + 1] = key.childValue(fromParent.table[i + 1]);
                } else {
                    table[i] = TOMBSTONE;
                    table[i + 1] = null;
                    fromParent.table[i] = TOMBSTONE;
                    fromParent.table[i + 1] = null;
                    tombstones++;
                    fromParent.tombstones++;
                    size--;
                    fromParent.size--;
                }
            }
        }
        private void initializeTable(int capacity) {
            this.table = new Object[capacity << 1];
            this.mask = table.length - 1;
            this.clean = 0;
            this.maximumLoad = capacity * 2 / 3; 
        }
        private void cleanUp() {
            if (rehash()) {
                return;
            }
            if (size == 0) {
                return;
            }
            int index = clean;
            Object[] table = this.table;
            for (int counter = table.length; counter > 0; counter >>= 1,
                    index = next(index)) {
                Object k = table[index];
                if (k == TOMBSTONE || k == null) {
                    continue; 
                }
                @SuppressWarnings("unchecked")
                Reference<ThreadLocal<?>> reference
                        = (Reference<ThreadLocal<?>>) k;
                if (reference.get() == null) {
                    table[index] = TOMBSTONE;
                    table[index + 1] = null;
                    tombstones++;
                    size--;
                }
            }
            clean = index;
        }
        private boolean rehash() {
            if (tombstones + size < maximumLoad) {
                return false;
            }
            int capacity = table.length >> 1;
            int newCapacity = capacity;
            if (size > (capacity >> 1)) {
                newCapacity = capacity << 1;
            }
            Object[] oldTable = this.table;
            initializeTable(newCapacity);
            this.tombstones = 0;
            if (size == 0) {
                return true;
            }
            for (int i = oldTable.length - 2; i >= 0; i -= 2) {
                Object k = oldTable[i];
                if (k == null || k == TOMBSTONE) {
                    continue;
                }
                @SuppressWarnings("unchecked")
                Reference<ThreadLocal<?>> reference
                        = (Reference<ThreadLocal<?>>) k;
                ThreadLocal<?> key = reference.get();
                if (key != null) {
                    add(key, oldTable[i + 1]);
                } else {
                    size--;
                }
            }
            return true;
        }
        void add(ThreadLocal<?> key, Object value) {
            for (int index = key.hash & mask;; index = next(index)) {
                Object k = table[index];
                if (k == null) {
                    table[index] = key.reference;
                    table[index + 1] = value;
                    return;
                }
            }
        }
        void put(ThreadLocal<?> key, Object value) {
            cleanUp();
            int firstTombstone = -1;
            for (int index = key.hash & mask;; index = next(index)) {
                Object k = table[index];
                if (k == key.reference) {
                    table[index + 1] = value;
                    return;
                }
                if (k == null) {
                    if (firstTombstone == -1) {
                        table[index] = key.reference;
                        table[index + 1] = value;
                        size++;
                        return;
                    }
                    table[firstTombstone] = key.reference;
                    table[firstTombstone + 1] = value;
                    tombstones--;
                    size++;
                    return;
                }
                if (firstTombstone == -1 && k == TOMBSTONE) {
                    firstTombstone = index;
                }
            }
        }
        Object getAfterMiss(ThreadLocal<?> key) {
            Object[] table = this.table;
            int index = key.hash & mask;
            if (table[index] == null) {
                Object value = key.initialValue();
                if (this.table == table && table[index] == null) {
                    table[index] = key.reference;
                    table[index + 1] = value;
                    size++;
                    cleanUp();
                    return value;
                }
                put(key, value);
                return value;
            }
            int firstTombstone = -1;
            for (index = next(index);; index = next(index)) {
                Object reference = table[index];
                if (reference == key.reference) {
                    return table[index + 1];
                }
                if (reference == null) {
                    Object value = key.initialValue();
                    if (this.table == table) {
                        if (firstTombstone > -1
                                && table[firstTombstone] == TOMBSTONE) {
                            table[firstTombstone] = key.reference;
                            table[firstTombstone + 1] = value;
                            tombstones--;
                            size++;
                            return value;
                        }
                        if (table[index] == null) {
                            table[index] = key.reference;
                            table[index + 1] = value;
                            size++;
                            cleanUp();
                            return value;
                        }
                    }
                    put(key, value);
                    return value;
                }
                if (firstTombstone == -1 && reference == TOMBSTONE) {
                    firstTombstone = index;
                }
            }
        }
        void remove(ThreadLocal<?> key) {
            cleanUp();
            for (int index = key.hash & mask;; index = next(index)) {
                Object reference = table[index];
                if (reference == key.reference) {
                    table[index] = TOMBSTONE;
                    table[index + 1] = null;
                    tombstones++;
                    size--;
                    return;
                }
                if (reference == null) {
                    return;
                }
            }
        }
        private int next(int index) {
            return (index + 2) & mask;
        }
    }
}
