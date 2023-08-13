public class LongHashMap
{
    static class Entry {
        private int    hash;
        private long   key;
        private Object value;
        private Entry  next;
        Entry(int hash, long key, Object value, Entry next) {
            this.hash  = hash;
            this.key   = key;
            this.value = value;
            this.next  = next;
        }
        long getKey() { return key; }
        Object getValue() { return value; }
        Object setValue(Object value) {
            Object oldValue = this.value;
            this.value = value;
            return oldValue;
        }
        public boolean equals(Object o) {
            if (!(o instanceof Entry))
                return false;
            Entry e = (Entry)o;
            return (key == e.getKey()) && eq(value, e.getValue());
        }
        public int hashCode() {
            return hash ^ (value==null ? 0 : value.hashCode());
        }
    }
    transient Entry table[];
    transient int size;
    int threshold;
    final float loadFactor;
    transient int modCount = 0;
    public LongHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Initial Capacity: "+
                                               initialCapacity);
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal Load factor: "+
                                               loadFactor);
        if (initialCapacity==0)
            initialCapacity = 1;
        this.loadFactor = loadFactor;
        table = new Entry[initialCapacity];
        threshold = (int)(initialCapacity * loadFactor);
    }
    public LongHashMap(int initialCapacity) {
        this(initialCapacity, 0.75f);
    }
    public LongHashMap() {
        this(11, 0.75f);
    }
    public int size() {
        return size;
    }
    public boolean isEmpty() {
        return size == 0;
    }
    public Object get(long key) {
        Entry e = getEntry(key);
        return (e == null ? null : e.value);
    }
    public boolean containsKey(long key) {
        return getEntry(key) != null;
    }
    Entry getEntry(long key) {
        Entry tab[] = table;
        int hash = (int) key;
        int index = (hash & 0x7FFFFFFF) % tab.length;
        for (Entry e = tab[index]; e != null; e = e.next)
            if (e.hash == hash && e.key ==key)
                return e;
        return null;
    }
    public boolean containsValue(Object value) {
        Entry tab[] = table;
        if (value==null) {
            for (int i = tab.length ; i-- > 0 ;)
                for (Entry e = tab[i] ; e != null ; e = e.next)
                    if (e.value==null)
                        return true;
        } else {
            for (int i = tab.length ; i-- > 0 ;)
                for (Entry e = tab[i] ; e != null ; e = e.next)
                    if (value.equals(e.value))
                        return true;
        }
        return false;
    }
    public Object put(long key, Object value) {
        Entry tab[] = table;
        int hash = (int) key;
        int index = (hash & 0x7FFFFFFF) % tab.length;
        for (Entry e = tab[index] ; e != null ; e = e.next) {
            if (e.hash == hash && e.key == key) {
                Object oldValue = e.value;
                e.value = value;
                return oldValue;
            }
        }
        modCount++;
        if (size >= threshold) {
            rehash();
            tab = table;
            index = (hash & 0x7FFFFFFF) % tab.length;
        }
        size++;
        tab[index] = newEntry(hash, key, value, tab[index]);
        return null;
    }
    public Object remove(long key) {
        Entry e = removeEntryForKey(key);
        return (e == null ? null : e.value);
    }
    Entry removeEntryForKey(long key) {
        Entry tab[] = table;
        int hash = (int) key;
        int index = (hash & 0x7FFFFFFF) % tab.length;
        for (Entry e = tab[index], prev = null; e != null;
             prev = e, e = e.next) {
            if (e.hash == hash && e.key == key) {
                modCount++;
                if (prev != null)
                    prev.next = e.next;
                else
                    tab[index] = e.next;
                size--;
                return e;
            }
        }
        return null;
    }
    void removeEntry(Entry doomed) {
        Entry[] tab = table;
        int index = (doomed.hash & 0x7FFFFFFF) % tab.length;
        for (Entry e = tab[index], prev = null; e != null;
             prev = e, e = e.next) {
            if (e == doomed) {
                modCount++;
                if (prev == null)
                    tab[index] = e.next;
                else
                    prev.next = e.next;
                size--;
                return;
            }
        }
        throw new ConcurrentModificationException();
    }
    public void clear() {
        Entry tab[] = table;
        modCount++;
        for (int index = tab.length; --index >= 0; )
            tab[index] = null;
        size = 0;
    }
    void rehash() {
        Entry oldTable[] = table;
        int oldCapacity = oldTable.length;
        int newCapacity = oldCapacity * 2 + 1;
        Entry newTable[] = new Entry[newCapacity];
        modCount++;
        threshold = (int)(newCapacity * loadFactor);
        table = newTable;
        for (int i = oldCapacity ; i-- > 0 ;) {
            for (Entry old = oldTable[i] ; old != null ; ) {
                Entry e = old;
                old = old.next;
                int index = (e.hash & 0x7FFFFFFF) % newCapacity;
                e.next = newTable[index];
                newTable[index] = e;
            }
        }
    }
    static boolean eq(Object o1, Object o2) {
        return (o1==null ? o2==null : o1.equals(o2));
    }
    Entry newEntry(int hash, long key, Object value, Entry next) {
        return new Entry(hash, key, value, next);
    }
    int capacity() {
        return table.length;
    }
    float loadFactor() {
        return loadFactor;
    }
}
