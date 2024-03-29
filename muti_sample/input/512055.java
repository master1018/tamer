public class TwoKeyHashMap<E, K, V> extends AbstractMap<String, V> {
    static final float DEFAULT_LOAD_FACTOR = 0.75f;
    static final int DEFAULT_INITIAL_SIZE = 16;
    private Set<Map.Entry<String, V>> entrySet;
    private Collection<V> values;
    private int size;
    private int arrSize;
    private int modCount;
    private Entry<E, K, V>[] arr;
    private float loadFactor;
    int threshold = 0;
    public TwoKeyHashMap() {
        this(DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR);
    }
    public TwoKeyHashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }
    @SuppressWarnings("unchecked")
    public TwoKeyHashMap(int initialCapacity, float initialLoadFactor) {
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("initialCapacity should be >= 0");
        }
        if (initialLoadFactor <= 0) {
            throw new IllegalArgumentException(
                    "initialLoadFactor should be > 0");
        }
        loadFactor = initialLoadFactor;
        if (initialCapacity == Integer.MAX_VALUE) {
            initialCapacity--;
        }
        arrSize = initialCapacity > 0 ? initialCapacity : 1;
        threshold = (int) (arrSize * loadFactor);
        arr = new Entry[arrSize + 1];
    }
    public Collection<V> values() {
        if (values == null) {
            values = new ValuesCollectionImpl();
        }
        return values;
    }
    public Set<Map.Entry<String, V>> entrySet() {
        if (entrySet == null) {
            entrySet = new EntrySetImpl();
        }
        return entrySet;
    }
    public void clear() {
        modCount++;
        size = 0;
        Arrays.fill(arr, 0, arr.length, null);
    }
    public V remove(Object key1, Object key2) {
        Entry<E, K, V> e = removeEntry(key1, key2);
        return null != e ? e.value : null;
    }
    public V put(E key1, K key2, V value) {
        if (key1 == null && key2 == null) {
            int index = arrSize;
            if (arr[index] == null) {
                arr[index] = createEntry(0, null, null, value, null);
                size++;
                modCount++;
                return null;
            } else {
                V oldValue = arr[index].value;
                arr[index].value = value;
                return oldValue;
            }
        }
        int hash = key1.hashCode() + key2.hashCode();
        int index = (hash & 0x7fffffff) % arrSize;
        Entry<E, K, V> e = arr[index];
        while (e != null) {
            if (hash == e.hash && key1.equals(e.getKey1())
                    && key2.equals(e.getKey2())) {
                V oldValue = e.value;
                e.value = value;
                return oldValue;
            }
            e = e.next;
        }
        arr[index] = createEntry(hash, key1, key2, value, arr[index]);
        size++;
        modCount++;
        if (size > threshold) {
            rehash();
        }
        return null;
    }
    @SuppressWarnings("unchecked")
    void rehash() {
        int newArrSize = (arrSize + 1) * 2 + 1;
        if (newArrSize < 0) {
            newArrSize = Integer.MAX_VALUE - 1;
        }
        Entry<E, K, V>[] newArr = new Entry[newArrSize + 1];
        for (int i = 0; i < arr.length - 1; i++) {
            Entry<E, K, V> entry = arr[i];
            while (entry != null) {
                Entry<E, K, V> next = entry.next;
                int newIndex = (entry.hash & 0x7fffffff) % newArrSize;
                entry.next = newArr[newIndex];
                newArr[newIndex] = entry;
                entry = next;
            }
        }
        newArr[newArrSize] = arr[arrSize]; 
        arrSize = newArrSize;
        if (arrSize == Integer.MAX_VALUE) {
            loadFactor *= 10;
        }
        threshold = (int) (arrSize * loadFactor);
        arr = newArr;
    }
    public boolean containsKey(Object key1, Object key2) {
        return findEntry(key1, key2) != null;
    }
    public V get(Object key1, Object key2) {
        Entry<E, K, V> e = findEntry(key1, key2);
        if (e != null) {
            return e.value;
        }
        return null;
    }
    public boolean isEmpty() {
        return size == 0;
    }
    public int size() {
        return size;
    }
    Entry<E, K, V> createEntry(int hashCode, E key1, K key2, V value,
            Entry<E, K, V> next) {
        return new Entry<E, K, V>(hashCode, key1, key2, value, next);
    }
    Iterator<Map.Entry<String, V>> createEntrySetIterator() {
        return new EntryIteratorImpl();
    }
    Iterator<V> createValueCollectionIterator() {
        return new ValueIteratorImpl();
    }
    public static class Entry<E, K, V> implements Map.Entry<String, V> {
        int hash;
        E key1;
        K key2;
        V value;
        Entry<E, K, V> next;
        public Entry(int hash, E key1, K key2, V value, Entry<E, K, V> next) {
            this.hash = hash;
            this.key1 = key1;
            this.key2 = key2;
            this.value = value;
            this.next = next;
        }
        public String getKey() {
            return key1.toString() + key2.toString();
        }
        public E getKey1() {
            return key1;
        }
        public K getKey2() {
            return key2;
        }
        public V getValue() {
            return value;
        }
        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }
        public boolean equals(Object obj) {
            if (!(obj instanceof Entry)) {
                return false;
            }
            Entry<?, ?, ?> e = (Entry<?, ?, ?>) obj;
            Object getKey1 = e.getKey1();
            Object getKey2 = e.getKey2();
            Object getValue = e.getValue();
            if ((key1 == null && getKey1 != null)
                    || (key2 == null && getKey2 != null)
                    || (value == null && getValue != null)
                    || !key1.equals(e.getKey1()) || !key2.equals(e.getKey2())
                    || !value.equals(getValue)) {
                return false;
            }
            return true;
        }
        public int hashCode() {
            int hash1 = (key1 == null ? 0 : key1.hashCode());
            int hash2 = (key2 == null ? 0 : key2.hashCode());
            return (hash1 + hash2) ^ (value == null ? 0 : value.hashCode());
        }
    }
    class EntrySetImpl extends AbstractSet<Map.Entry<String, V>> {
        public int size() {
            return size;
        }
        public void clear() {
            TwoKeyHashMap.this.clear();
        }
        public boolean isEmpty() {
            return size == 0;
        }
        public boolean contains(Object obj) {
            if (!(obj instanceof Entry)) {
                return false;
            }
            Entry<?, ?, ?> entry = (Entry<?, ?, ?>) obj;
            Entry<E, K, V> entry2 = findEntry(entry.getKey1(), entry.getKey2());
            if (entry2 == null) {
                return false;
            }
            Object value = entry.getValue();
            Object value2 = entry2.getValue();
            return value == null ? value2 == null : value.equals(value2);
        }
        public boolean remove(Object obj) {
            if (!(obj instanceof Entry)) {
                return false;
            }
            return removeEntry(((Entry) obj).getKey1(), ((Entry) obj).getKey2()) != null;
        }
        public Iterator<Map.Entry<String, V>> iterator() {
            return createEntrySetIterator();
        }
    }
    class EntryIteratorImpl implements Iterator<Map.Entry<String, V>> {
        private int startModCount;
        private boolean found;
        private int curr = -1;
        private int returned_index = -1;
        private Entry<E, K, V> curr_entry;
        private Entry<E, K, V> returned_entry;
        EntryIteratorImpl() {
            startModCount = modCount;
        }
        public boolean hasNext() {
            if (found) {
                return true;
            }
            if (curr_entry != null) {
                curr_entry = curr_entry.next;
            }
            if (curr_entry == null) {
                for (curr++; curr < arr.length && arr[curr] == null; curr++) {
                }
                if (curr < arr.length) {
                    curr_entry = arr[curr];
                }
            }
            return found = (curr_entry != null);
        }
        public Map.Entry<String, V> next() {
            if (modCount != startModCount) {
                throw new ConcurrentModificationException();
            }
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            found = false;
            returned_index = curr;
            returned_entry = curr_entry;
            return (Map.Entry<String, V>) curr_entry;
        }
        public void remove() {
            if (returned_index == -1) {
                throw new IllegalStateException();
            }
            if (modCount != startModCount) {
                throw new ConcurrentModificationException();
            }
            Entry<E, K, V> p = null;
            Entry<E, K, V> e = arr[returned_index];
            while (e != returned_entry) {
                p = e;
                e = e.next;
            }
            if (p != null) {
                p.next = returned_entry.next;
            } else {
                arr[returned_index] = returned_entry.next;
            }
            size--;
            modCount++;
            startModCount++;
            returned_index = -1;
        }
    }
    private final Entry<E, K, V> findEntry(Object key1, Object key2) {
        if (key1 == null && key2 == null) {
            return arr[arrSize];
        }
        int hash = key1.hashCode() + key2.hashCode();
        int index = (hash & 0x7fffffff) % arrSize;
        Entry<E, K, V> e = arr[index];
        while (e != null) {
            if (hash == e.hash && key1.equals(e.getKey1())
                    && key2.equals(e.getKey2())) {
                return e;
            }
            e = e.next;
        }
        return null;
    }
    private final Entry<E, K, V> removeEntry(Object key1, Object key2) {
        if (key1 == null && key2 == null) {
            int index = arrSize;
            if (arr[index] != null) {
                Entry<E, K, V> ret = arr[index];
                arr[index] = null;
                size--;
                modCount++;
                return ret;
            }
            return null;
        }
        int hash = key1.hashCode() + key2.hashCode();
        int index = (hash & 0x7fffffff) % arrSize;
        Entry<E, K, V> e = arr[index];
        Entry<E, K, V> prev = e;
        while (e != null) {
            if (hash == e.hash && key1.equals(e.getKey1())
                    && key2.equals(e.getKey2())) {
                if (prev == e) {
                    arr[index] = e.next;
                } else {
                    prev.next = e.next;
                }
                size--;
                modCount++;
                return e;
            }
            prev = e;
            e = e.next;
        }
        return null;
    }
    class ValuesCollectionImpl extends AbstractCollection<V> {
        public int size() {
            return size;
        }
        public void clear() {
            TwoKeyHashMap.this.clear();
        }
        public boolean isEmpty() {
            return size == 0;
        }
        public Iterator<V> iterator() {
            return createValueCollectionIterator();
        }
        public boolean contains(Object obj) {
            return containsValue(obj);
        }
    }
    class ValueIteratorImpl implements Iterator<V> {
        private EntryIteratorImpl itr;
        ValueIteratorImpl() {
            super();
            this.itr = new EntryIteratorImpl();
        }
        public V next() {
            return itr.next().getValue();
        }
        public void remove() {
            itr.remove();
        }
        public boolean hasNext() {
            return itr.hasNext();
        }
    }
}
