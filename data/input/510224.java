public class Hashtable<K, V> extends Dictionary<K, V>
        implements Map<K, V>, Cloneable, Serializable {
    private static final int MINIMUM_CAPACITY = 4;
    private static final int MAXIMUM_CAPACITY = 1 << 30;
    private static final Entry[] EMPTY_TABLE
            = new HashtableEntry[MINIMUM_CAPACITY >>> 1];
    private static final float DEFAULT_LOAD_FACTOR = .75F;
    private transient HashtableEntry<K, V>[] table;
    private transient int size;
    private transient int modCount;
    private transient int threshold;
    private transient Set<K> keySet;
    private transient Set<Entry<K, V>> entrySet;
    private transient Collection<V> values;
    @SuppressWarnings("unchecked")
    public Hashtable() {
        table = (HashtableEntry<K, V>[]) EMPTY_TABLE;
        threshold = -1; 
    }
    public Hashtable(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Capacity: " + capacity);
        }
        if (capacity == 0) {
            @SuppressWarnings("unchecked")
            HashtableEntry<K, V>[] tab = (HashtableEntry<K, V>[]) EMPTY_TABLE;
            table = tab;
            threshold = -1; 
            return;
        }
        if (capacity < MINIMUM_CAPACITY) {
            capacity = MINIMUM_CAPACITY;
        } else if (capacity > MAXIMUM_CAPACITY) {
            capacity = MAXIMUM_CAPACITY;
        } else {
            capacity = roundUpToPowerOfTwo(capacity);
        }
        makeTable(capacity);
    }
    public Hashtable(int capacity, float loadFactor) {
        this(capacity);
        if (loadFactor <= 0 || Float.isNaN(loadFactor)) {
            throw new IllegalArgumentException("Load factor: " + loadFactor);
        }
    }
    public Hashtable(Map<? extends K, ? extends V> map) {
        this(capacityForInitSize(map.size()));
        constructorPutAll(map);
    }
    private void constructorPutAll(Map<? extends K, ? extends V> map) {
        for (Entry<? extends K, ? extends V> e : map.entrySet()) {
            constructorPut(e.getKey(), e.getValue());
        }
    }
    private static int capacityForInitSize(int size) {
        int result = (size >> 1) + size; 
        return (result & ~(MAXIMUM_CAPACITY-1))==0 ? result : MAXIMUM_CAPACITY;
    }
    @SuppressWarnings("unchecked")
    @Override public synchronized Object clone() {
        Hashtable<K, V> result;
        try {
            result = (Hashtable<K, V>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
        result.makeTable(table.length);
        result.size = 0;
        result.keySet = null;
        result.entrySet = null;
        result.values = null;
        result.constructorPutAll(this);
        return result;
    }
    public synchronized boolean isEmpty() {
        return size == 0;
    }
    public synchronized int size() {
        return size;
    }
    public synchronized V get(Object key) {
        int hash = key.hashCode();
        hash ^= (hash >>> 20) ^ (hash >>> 12);
        hash ^= (hash >>> 7) ^ (hash >>> 4);
        HashtableEntry<K, V>[] tab = table;
        for (HashtableEntry<K, V> e = tab[hash & (tab.length - 1)];
                e != null; e = e.next) {
            K eKey = e.key;
            if (eKey == key || (e.hash == hash && key.equals(eKey))) {
                return e.value;
            }
        }
        return null;
    }
    public synchronized boolean containsKey(Object key) {
        int hash = key.hashCode();
        hash ^= (hash >>> 20) ^ (hash >>> 12);
        hash ^= (hash >>> 7) ^ (hash >>> 4);
        HashtableEntry<K, V>[] tab = table;
        for (HashtableEntry<K, V> e = tab[hash & (tab.length - 1)];
                e != null; e = e.next) {
            K eKey = e.key;
            if (eKey == key || (e.hash == hash && key.equals(eKey))) {
                return true;
            }
        }
        return false;
    }
    public synchronized boolean containsValue(Object value) {
        if (value == null) {
            throw new NullPointerException();
        }
        HashtableEntry[] tab = table;
        int len = tab.length;
        for (int i = 0; i < len; i++) {
            for (HashtableEntry e = tab[i]; e != null; e = e.next) {
                if (value.equals(e.value)) {
                    return true;
                }
            }
        }
        return false;
    }
    public boolean contains(Object value) {
        return containsValue(value);
    }
    public synchronized V put(K key, V value) {
        if (value == null) {
            throw new NullPointerException();
        }
        int hash = secondaryHash(key.hashCode());
        HashtableEntry<K, V>[] tab = table;
        int index = hash & (tab.length - 1);
        HashtableEntry<K, V> first = tab[index];
        for (HashtableEntry<K, V> e = first; e != null; e = e.next) {
            if (e.hash == hash && key.equals(e.key)) {
                V oldValue = e.value;
                e.value = value;
                return oldValue;
            }
        }
        modCount++;
        if (size++ > threshold) {
            rehash();  
            tab = doubleCapacity();
            index = hash & (tab.length - 1);
            first = tab[index];
        }
        tab[index] = new HashtableEntry<K, V>(key, value, hash, first);
        return null;
    }
    private void constructorPut(K key, V value) {
        if (value == null) {
            throw new NullPointerException();
        }
        int hash = secondaryHash(key.hashCode());
        HashtableEntry<K, V>[] tab = table;
        int index = hash & (tab.length - 1);
        HashtableEntry<K, V> first = tab[index];
        for (HashtableEntry<K, V> e = first; e != null; e = e.next) {
            if (e.hash == hash && key.equals(e.key)) {
                e.value = value;
                return;
            }
        }
        tab[index] = new HashtableEntry<K, V>(key, value, hash, first);
        size++;
    }
    public synchronized void putAll(Map<? extends K, ? extends V> map) {
        ensureCapacity(map.size());
        for (Entry<? extends K, ? extends V> e : map.entrySet()) {
            put(e.getKey(), e.getValue());
        }
    }
    private void ensureCapacity(int numMappings) {
        int newCapacity = roundUpToPowerOfTwo(capacityForInitSize(numMappings));
        HashtableEntry<K, V>[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (newCapacity <= oldCapacity) {
            return;
        }
        rehash();  
        if (newCapacity == oldCapacity << 1) {
            doubleCapacity();
            return;
        }
        HashtableEntry<K, V>[] newTable = makeTable(newCapacity);
        if (size != 0) {
            int newMask = newCapacity - 1;
            for (int i = 0; i < oldCapacity; i++) {
                for (HashtableEntry<K, V> e = oldTable[i]; e != null;) {
                    HashtableEntry<K, V> oldNext = e.next;
                    int newIndex = e.hash & newMask;
                    HashtableEntry<K, V> newNext = newTable[newIndex];
                    newTable[newIndex] = e;
                    e.next = newNext;
                    e = oldNext;
                }
            }
        }
    }
    protected void rehash() {
    }
    private HashtableEntry<K, V>[] makeTable(int newCapacity) {
        @SuppressWarnings("unchecked") HashtableEntry<K, V>[] newTable
                = (HashtableEntry<K, V>[]) new HashtableEntry[newCapacity];
        table = newTable;
        threshold = (newCapacity >> 1) + (newCapacity >> 2); 
        return newTable;
    }
    private HashtableEntry<K, V>[] doubleCapacity() {
        HashtableEntry<K, V>[] oldTable = table;
        int oldCapacity = oldTable.length;
        if (oldCapacity == MAXIMUM_CAPACITY) {
            return oldTable;
        }
        int newCapacity = oldCapacity << 1;
        HashtableEntry<K, V>[] newTable = makeTable(newCapacity);
        if (size == 0) {
            return newTable;
        }
        for (int j = 0; j < oldCapacity; j++) {
            HashtableEntry<K, V> e = oldTable[j];
            if (e == null) {
                continue;
            }
            int highBit = e.hash & oldCapacity;
            HashtableEntry<K, V> broken = null;
            newTable[j | highBit] = e;
            for (HashtableEntry<K,V> n = e.next; n != null; e = n, n = n.next) {
                int nextHighBit = n.hash & oldCapacity;
                if (nextHighBit != highBit) {
                    if (broken == null)
                        newTable[j | nextHighBit] = n;
                    else
                        broken.next = n;
                    broken = e;
                    highBit = nextHighBit;
                }
            }
            if (broken != null)
                broken.next = null;
        }
        return newTable;
    }
    public synchronized V remove(Object key) {
        int hash = secondaryHash(key.hashCode());
        HashtableEntry<K, V>[] tab = table;
        int index = hash & (tab.length - 1);
        for (HashtableEntry<K, V> e = tab[index], prev = null;
                e != null; prev = e, e = e.next) {
            if (e.hash == hash && key.equals(e.key)) {
                if (prev == null) {
                    tab[index] = e.next;
                } else {
                    prev.next = e.next;
                }
                modCount++;
                size--;
                return e.value;
            }
        }
        return null;
    }
    public synchronized void clear() {
        if (size != 0) {
            Arrays.fill(table, null);
            modCount++;
            size = 0;
        }
    }
    public synchronized Set<K> keySet() {
        Set<K> ks = keySet;
        return (ks != null) ? ks : (keySet = new KeySet());
    }
    public synchronized Collection<V> values() {
        Collection<V> vs = values;
        return (vs != null) ? vs : (values = new Values());
    }
    public synchronized Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> es = entrySet;
        return (es != null) ? es : (entrySet = new EntrySet());
    }
    public synchronized Enumeration<K> keys() {
        return new KeyEnumeration();
    }
    public synchronized Enumeration<V> elements() {
        return new ValueEnumeration();
    }
    private static class HashtableEntry<K, V> implements Entry<K, V> {
        final K key;
        V value;
        final int hash;
        HashtableEntry<K, V> next;
        HashtableEntry(K key, V value, int hash, HashtableEntry<K, V> next) {
            this.key = key;
            this.value = value;
            this.hash = hash;
            this.next = next;
        }
        public final K getKey() {
            return key;
        }
        public final V getValue() {
            return value;
        }
        public final V setValue(V value) {
            if (value == null) {
                throw new NullPointerException();
            }
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }
        @Override public final boolean equals(Object o) {
            if (!(o instanceof Entry)) {
                return false;
            }
            Entry<?, ?> e = (Entry<?, ?>) o;
            return key.equals(e.getKey()) && value.equals(e.getValue());
        }
        @Override public final int hashCode() {
            return key.hashCode() ^ value.hashCode();
        }
        @Override public final String toString() {
            return key + "=" + value;
        }
    }
    private abstract class HashIterator {
        int nextIndex;
        HashtableEntry<K, V> nextEntry;
        HashtableEntry<K, V> lastEntryReturned;
        int expectedModCount = modCount;
        HashIterator() {
            HashtableEntry<K, V>[] tab = table;
            HashtableEntry<K, V> next = null;
            while (next == null && nextIndex < tab.length) {
                next = tab[nextIndex++];
            }
            nextEntry = next;
        }
        public boolean hasNext() {
            return nextEntry != null;
        }
        HashtableEntry<K, V> nextEntry() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (nextEntry == null)
                throw new NoSuchElementException();
            HashtableEntry<K, V> entryToReturn = nextEntry;
            HashtableEntry<K, V>[] tab = table;
            HashtableEntry<K, V> next = entryToReturn.next;
            while (next == null && nextIndex < tab.length) {
                next = tab[nextIndex++];
            }
            nextEntry = next;
            return lastEntryReturned = entryToReturn;
        }
        HashtableEntry<K, V> nextEntryNotFailFast() {
            if (nextEntry == null)
                throw new NoSuchElementException();
            HashtableEntry<K, V> entryToReturn = nextEntry;
            HashtableEntry<K, V>[] tab = table;
            HashtableEntry<K, V> next = entryToReturn.next;
            while (next == null && nextIndex < tab.length) {
                next = tab[nextIndex++];
            }
            nextEntry = next;
            return lastEntryReturned = entryToReturn;
        }
        public void remove() {
            if (lastEntryReturned == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            Hashtable.this.remove(lastEntryReturned.key);
            lastEntryReturned = null;
            expectedModCount = modCount;
        }
    }
    private final class KeyIterator extends HashIterator
            implements Iterator<K> {
        public K next() { return nextEntry().key; }
    }
    private final class ValueIterator extends HashIterator
            implements Iterator<V> {
        public V next() { return nextEntry().value; }
    }
    private final class EntryIterator extends HashIterator
            implements Iterator<Entry<K, V>> {
        public Entry<K, V> next() { return nextEntry(); }
    }
    private final class KeyEnumeration extends HashIterator
            implements Enumeration<K> {
        public boolean hasMoreElements() { return hasNext(); }
        public K nextElement() { return nextEntryNotFailFast().key; }
    }
    private final class ValueEnumeration extends HashIterator
            implements Enumeration<V> {
        public boolean hasMoreElements() { return hasNext(); }
        public V nextElement() { return nextEntryNotFailFast().value; }
    }
    private synchronized boolean containsMapping(Object key, Object value) {
        int hash = secondaryHash(key.hashCode());
        HashtableEntry<K, V>[] tab = table;
        int index = hash & (tab.length - 1);
        for (HashtableEntry<K, V> e = tab[index]; e != null; e = e.next) {
            if (e.hash == hash && e.key.equals(key)) {
                return e.value.equals(value);
            }
        }
        return false; 
    }
    private synchronized boolean removeMapping(Object key, Object value) {
        int hash = secondaryHash(key.hashCode());
        HashtableEntry<K, V>[] tab = table;
        int index = hash & (tab.length - 1);
        for (HashtableEntry<K, V> e = tab[index], prev = null;
                e != null; prev = e, e = e.next) {
            if (e.hash == hash && e.key.equals(key)) {
                if (!e.value.equals(value)) {
                    return false;  
                }
                if (prev == null) {
                    tab[index] = e.next;
                } else {
                    prev.next = e.next;
                }
                modCount++;
                size--;
                return true;
            }
        }
        return false; 
    }
    @Override public synchronized boolean equals(Object object) {
        return (object instanceof Map) &&
                entrySet().equals(((Map<?, ?>)object).entrySet());
    }
    @Override public synchronized int hashCode() {
        int result = 0;
        for (Entry<K, V> e : entrySet()) {
            K key = e.getKey();
            V value = e.getValue();
            if (key == this || value == this) {
                continue;
            }
            result += (key != null ? key.hashCode() : 0)
                    ^ (value != null ? value.hashCode() : 0);
        }
        return result;
    }
    private static final int CHARS_PER_ENTRY = 15;
    @Override public synchronized String toString() {
        StringBuilder result = new StringBuilder(CHARS_PER_ENTRY * size);
        result.append('{');
        Iterator<Entry<K, V>> i = entrySet().iterator();
        boolean hasMore = i.hasNext();
        while (hasMore) {
            Entry<K, V> entry = i.next();
            K key = entry.getKey();
            result.append(key == this ? "(this Map)" : key.toString());
            result.append('=');
            V value = entry.getValue();
            result.append(value == this ? "(this Map)" : value.toString());
            if (hasMore = i.hasNext()) {
                result.append(", ");
            }
        }
        result.append('}');
        return result.toString();
    }
    private final class KeySet extends AbstractSet<K> {
        public Iterator<K> iterator() {
            return new KeyIterator();
        }
        public int size() {
            return Hashtable.this.size();
        }
        public boolean contains(Object o) {
            return containsKey(o);
        }
        public boolean remove(Object o) {
            synchronized (Hashtable.this) {
                int oldSize = size;
                Hashtable.this.remove(o);
                return size != oldSize;
            }
        }
        public void clear() {
            Hashtable.this.clear();
        }
        public boolean removeAll(Collection<?> collection) {
            synchronized (Hashtable.this) {
                return super.removeAll(collection);
            }
        }
        public boolean retainAll(Collection<?> collection) {
            synchronized (Hashtable.this) {
                return super.retainAll(collection);
            }
        }
        public boolean containsAll(Collection<?> collection) {
            synchronized (Hashtable.this) {
                return super.containsAll(collection);
            }
        }
        public boolean equals(Object object) {
            synchronized (Hashtable.this) {
                return super.equals(object);
            }
        }
        public int hashCode() {
            synchronized (Hashtable.this) {
                return super.hashCode();
            }
        }
        public String toString() {
            synchronized (Hashtable.this) {
                return super.toString();
            }
        }
        public Object[] toArray() {
            synchronized (Hashtable.this) {
                return super.toArray();
            }
        }
        public <T> T[] toArray(T[] a) {
            synchronized (Hashtable.this) {
                return super.toArray(a);
            }
        }
    }
    private final class Values extends AbstractCollection<V> {
        public Iterator<V> iterator() {
            return new ValueIterator();
        }
        public int size() {
            return Hashtable.this.size();
        }
        public boolean contains(Object o) {
            return containsValue(o);
        }
        public void clear() {
            Hashtable.this.clear();
        }
        public boolean containsAll(Collection<?> collection) {
            synchronized (Hashtable.this) {
                return super.containsAll(collection);
            }
        }
        public String toString() {
            synchronized (Hashtable.this) {
                return super.toString();
            }
        }
        public Object[] toArray() {
            synchronized (Hashtable.this) {
                return super.toArray();
            }
        }
        public <T> T[] toArray(T[] a) {
            synchronized (Hashtable.this) {
                return super.toArray(a);
            }
        }
    }
    private final class EntrySet extends AbstractSet<Entry<K, V>> {
        public Iterator<Entry<K, V>> iterator() {
            return new EntryIterator();
        }
        public boolean contains(Object o) {
            if (!(o instanceof Entry))
                return false;
            Entry<?, ?> e = (Entry<?, ?>) o;
            return containsMapping(e.getKey(), e.getValue());
        }
        public boolean remove(Object o) {
            if (!(o instanceof Entry))
                return false;
            Entry<?, ?> e = (Entry<?, ?>)o;
            return removeMapping(e.getKey(), e.getValue());
        }
        public int size() {
            return Hashtable.this.size();
        }
        public void clear() {
            Hashtable.this.clear();
        }
        public boolean removeAll(Collection<?> collection) {
            synchronized (Hashtable.this) {
                return super.removeAll(collection);
            }
        }
        public boolean retainAll(Collection<?> collection) {
            synchronized (Hashtable.this) {
                return super.retainAll(collection);
            }
        }
        public boolean containsAll(Collection<?> collection) {
            synchronized (Hashtable.this) {
                return super.containsAll(collection);
            }
        }
        public boolean equals(Object object) {
            synchronized (Hashtable.this) {
                return super.equals(object);
            }
        }
        public int hashCode() {
            return Hashtable.this.hashCode();
        }
        public String toString() {
            synchronized (Hashtable.this) {
                return super.toString();
            }
        }
        public Object[] toArray() {
            synchronized (Hashtable.this) {
                return super.toArray();
            }
        }
        public <T> T[] toArray(T[] a) {
            synchronized (Hashtable.this) {
                return super.toArray(a);
            }
        }
    }
    private static int secondaryHash(int h) {
        h ^= (h >>> 20) ^ (h >>> 12);
        return h ^ (h >>> 7) ^ (h >>> 4);
    }
    private static int roundUpToPowerOfTwo(int i) {
        i--; 
        i |= i >>>  1;
        i |= i >>>  2;
        i |= i >>>  4;
        i |= i >>>  8;
        i |= i >>> 16;
        return i + 1;
    }
    private static final long serialVersionUID = 1421746759512286392L;
    private static final ObjectStreamField[] serialPersistentFields = {
        new ObjectStreamField("threshold", Integer.TYPE),
        new ObjectStreamField("loadFactor", Float.TYPE)
    };
    private synchronized void writeObject(ObjectOutputStream stream)
            throws IOException {
        ObjectOutputStream.PutField fields = stream.putFields();
        fields.put("threshold",  (int) (DEFAULT_LOAD_FACTOR * table.length));
        fields.put("loadFactor", DEFAULT_LOAD_FACTOR);
        stream.writeFields();
        stream.writeInt(table.length); 
        stream.writeInt(size);
        for (Entry<K, V> e : entrySet()) {
            stream.writeObject(e.getKey());
            stream.writeObject(e.getValue());
        }
    }
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
        int capacity = stream.readInt();
        if (capacity < 0) {
            throw new InvalidObjectException("Capacity: " + capacity);
        }
        if (capacity < MINIMUM_CAPACITY) {
            capacity = MINIMUM_CAPACITY;
        } else if (capacity > MAXIMUM_CAPACITY) {
            capacity = MAXIMUM_CAPACITY;
        } else {
            capacity = roundUpToPowerOfTwo(capacity);
        }
        makeTable(capacity);
        int size = stream.readInt();
        if (size < 0) {
            throw new InvalidObjectException("Size: " + size);
        }
        for (int i = 0; i < size; i++) {
            @SuppressWarnings("unchecked") K key = (K) stream.readObject();
            @SuppressWarnings("unchecked") V val = (V) stream.readObject();
            constructorPut(key, val);
        }
    }
}
