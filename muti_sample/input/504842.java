public class LinkedHashMap<K, V> extends HashMap<K, V> {
    transient LinkedEntry<K, V> header;
    private final boolean accessOrder;
    public LinkedHashMap() {
        super();
        init();
        accessOrder = false;
    }
    public LinkedHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }
    public LinkedHashMap(int initialCapacity, float loadFactor) {
        this(initialCapacity, loadFactor, false);
    }
    public LinkedHashMap(
            int initialCapacity, float loadFactor, boolean accessOrder) {
        super(initialCapacity, loadFactor);
        init();
        this.accessOrder = accessOrder;
    }
    public LinkedHashMap(Map<? extends K, ? extends V> map) {
        this(capacityForInitSize(map.size()));
        constructorPutAll(map);
    }
    @Override void init() {
        header = new LinkedEntry<K, V>();
    }
    static class LinkedEntry<K, V> extends HashMapEntry<K, V> {
        LinkedEntry<K, V> nxt;
        LinkedEntry<K, V> prv;
        LinkedEntry() {
            super(null, null, 0, null);
            nxt = prv = this;
        }
        LinkedEntry(K key, V value, int hash, HashMapEntry<K, V> next,
                    LinkedEntry<K, V> nxt, LinkedEntry<K, V> prv) {
            super(key, value, hash, next);
            this.nxt = nxt;
            this.prv = prv;
        }
    }
    @Override void addNewEntry(K key, V value, int hash, int index) {
        LinkedEntry<K, V> header = this.header;
        LinkedEntry<K, V> eldest = header.nxt;
        if (eldest != header && removeEldestEntry(eldest)) {
            remove(eldest.key);
        }
        LinkedEntry<K, V> oldTail = header.prv;
        LinkedEntry<K, V> newTail = new LinkedEntry<K,V>(
                key, value, hash, table[index], header, oldTail);
        table[index] = oldTail.nxt = header.prv = newTail;
    }
    @Override void addNewEntryForNullKey(V value) {
        LinkedEntry<K, V> header = this.header;
        LinkedEntry<K, V> eldest = header.nxt;
        if (eldest != header && removeEldestEntry(eldest)) {
            remove(eldest.key);
        }
        LinkedEntry<K, V> oldTail = header.prv;
        LinkedEntry<K, V> newTail = new LinkedEntry<K,V>(
                null, value, 0, null, header, oldTail);
        entryForNullKey = oldTail.nxt = header.prv = newTail;
    }
    @Override HashMapEntry<K, V> constructorNewEntry(
            K key, V value, int hash, HashMapEntry<K, V> next) {
        LinkedEntry<K, V> header = this.header;
        LinkedEntry<K, V> oldTail = header.prv;
        LinkedEntry<K, V> newTail
                = new LinkedEntry<K,V>(key, value, hash, next, header, oldTail);
        return oldTail.nxt = header.prv = newTail;
    }
    @Override public V get(Object key) {
        if (key == null) {
            HashMapEntry<K, V> e = entryForNullKey;
            if (e == null)
                return null;
            if (accessOrder)
                makeTail((LinkedEntry<K, V>) e);
            return e.value;
        }
        int hash = key.hashCode();
        hash ^= (hash >>> 20) ^ (hash >>> 12);
        hash ^= (hash >>> 7) ^ (hash >>> 4);
        HashMapEntry<K, V>[] tab = table;
        for (HashMapEntry<K, V> e = tab[hash & (tab.length - 1)];
                e != null; e = e.next) {
            K eKey = e.key;
            if (eKey == key || (e.hash == hash && key.equals(eKey))) {
                if (accessOrder)
                    makeTail((LinkedEntry<K, V>) e);
                return e.value;
            }
        }
        return null;
    }
    private void makeTail(LinkedEntry<K, V> e) {
        e.prv.nxt = e.nxt;
        e.nxt.prv = e.prv;
        LinkedEntry<K, V> header = this.header;
        LinkedEntry<K, V> oldTail = header.prv;
        e.nxt = header;
        e.prv = oldTail;
        oldTail.nxt = header.prv = e;
        modCount++;
    }
    @Override void preModify(HashMapEntry<K, V> e) {
        if (accessOrder) {
            makeTail((LinkedEntry<K, V>) e);
        }
    }
    @Override void postRemove(HashMapEntry<K, V> e) {
        LinkedEntry<K, V> le = (LinkedEntry<K, V>) e;
        le.prv.nxt = le.nxt;
        le.nxt.prv = le.prv;
        le.nxt = le.prv = null; 
    }
    @Override public boolean containsValue(Object value) {
        if (value == null) {
            for (LinkedEntry<K, V> header = this.header, e = header.nxt;
                    e != header; e = e.nxt) {
                if (e.value == null) {
                    return true;
                }
            }
            return false;
        }
        for (LinkedEntry<K, V> header = this.header, e = header.nxt;
                e != header; e = e.nxt) {
            if (value.equals(e.value)) {
                return true;
            }
        }
        return false;
    }
    public void clear() {
        super.clear();
        LinkedEntry<K, V> header = this.header;
        for (LinkedEntry<K, V> e = header.nxt; e != header; ) {
            LinkedEntry<K, V> nxt = e.nxt;
            e.nxt = e.prv = null;
            e = nxt;
        }
        header.nxt = header.prv = header;
    }
    private abstract class LinkedHashIterator<T> implements Iterator<T> {
        LinkedEntry<K, V> next = header.nxt;
        LinkedEntry<K, V> lastReturned = null;
        int expectedModCount = modCount;
        public final boolean hasNext() {
            return next != header;
        }
        final LinkedEntry<K, V> nextEntry() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            LinkedEntry<K, V> e = next;
            if (e == header)
                throw new NoSuchElementException();
            next = e.nxt;
            return lastReturned = e;
        }
        public final void remove() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (lastReturned == null)
                throw new IllegalStateException();
            LinkedHashMap.this.remove(lastReturned.key);
            lastReturned = null;
            expectedModCount = modCount;
        }
    }
    private final class KeyIterator extends LinkedHashIterator<K> {
        public final K next() { return nextEntry().key; }
    }
    private final class ValueIterator extends LinkedHashIterator<V> {
        public final V next() { return nextEntry().value; }
    }
    private final class EntryIterator
            extends LinkedHashIterator<Map.Entry<K, V>> {
        public final Map.Entry<K, V> next() { return nextEntry(); }
    }
    @Override Iterator<K> newKeyIterator() {
        return new KeyIterator();
    }
    @Override Iterator<V> newValueIterator() {
        return new ValueIterator();
    }
    @Override Iterator<Map.Entry<K, V>> newEntryIterator() {
        return new EntryIterator();
    }
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return false;
    }
    private static final long serialVersionUID = 3801124242820219131L;
}
