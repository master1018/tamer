public class LinkedHashMap extends AbstractMap implements Map, Serializable {
    private transient Entry table[];
    private transient Entry header;
    private transient int count;
    private int threshold;
    private float loadFactor;
    private transient int modCount = 0;
    public LinkedHashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal Initial Capacity: "+
                                               initialCapacity);
        if ((loadFactor > 1) || (loadFactor <= 0))
            throw new IllegalArgumentException("Illegal Load factor: "+
                                               loadFactor);
        if (initialCapacity==0)
            initialCapacity = 1;
        this.loadFactor = loadFactor;
        table = new Entry[initialCapacity];
        threshold = (int)(initialCapacity * loadFactor);
        header = new Entry(-1, null, null, null);
        header.before = header.after = header;
    }
    public LinkedHashMap(int initialCapacity) {
        this(initialCapacity, 0.75f);
    }
    public LinkedHashMap() {
        this(101, 0.75f);
    }
    public LinkedHashMap(Map t) {
        this(Math.max(3*t.size(), 11), 0.75f);
        putAll(t);
    }
    public int size() {
        return count;
    }
    public boolean isEmpty() {
        return count == 0;
    }
    public boolean containsValue(Object value) {
        if (value==null) {
            for (Entry e = header.after; e != header; e = e.after)
                if (e.value==null)
                    return true;
        } else {
            for (Entry e = header.after; e != header; e = e.after)
                if (value.equals(e.value))
                    return true;
        }
        return false;
    }
    public boolean containsKey(Object key) {
        Entry tab[] = table;
        if (key != null) {
            int hash = key.hashCode();
            int index = (hash & 0x7FFFFFFF) % tab.length;
            for (Entry e = tab[index]; e != null; e = e.next)
                if (e.hash==hash && e.key.equals(key))
                    return true;
        } else {
            for (Entry e = tab[0]; e != null; e = e.next)
                if (e.key==null)
                    return true;
        }
        return false;
    }
    public Object get(Object key) {
        Entry e = getEntry(key);
        return e==null ? null : e.value;
    }
    private Entry getEntry(Object key) {
        Entry tab[] = table;
        if (key != null) {
            int hash = key.hashCode();
            int index = (hash & 0x7FFFFFFF) % tab.length;
            for (Entry e = tab[index]; e != null; e = e.next)
                if ((e.hash == hash) && e.key.equals(key))
                    return e;
        } else {
            for (Entry e = tab[0]; e != null; e = e.next)
                if (e.key==null)
                    return e;
        }
        return null;
    }
    private void rehash() {
        int oldCapacity = table.length;
        Entry oldMap[] = table;
        int newCapacity = oldCapacity * 2 + 1;
        Entry newMap[] = new Entry[newCapacity];
        modCount++;
        threshold = (int)(newCapacity * loadFactor);
        table = newMap;
        for (Entry e = header.after; e != header; e = e.after) {
            int index = (e.hash & 0x7FFFFFFF) % newCapacity;
            e.next = newMap[index];
            newMap[index] = e;
        }
    }
    private void listRemove(Entry entry) {
        if (entry == null) {
            return;
        }
        entry.before.after = entry.after;
        entry.after.before = entry.before;
    }
    private void listAddBefore(Entry entry, Entry existEntry) {
        entry.after = existEntry;
        entry.before = existEntry.before;
        entry.before.after = entry;
        entry.after.before = entry;
    }
    public int indexOf(Object key) {
        int i = 0;
        if (key == null) {
            for (Entry e = header.after; e != header; e = e.after, i++)
                if (e.key == null)
                    return i;
        } else {
            for (Entry e = header.after; e != header; e = e.after, i++)
                if(key.equals(e.key))
                    return i;
        }
        return -1;
    }
    private Object putAhead(Object key, Object value, Entry existEntry) {
        Entry tab[] = table;
        int hash = 0;
        int index = 0;
        if (key != null) {
            hash = key.hashCode();
            index = (hash & 0x7FFFFFFF) % tab.length;
            for (Entry e = tab[index] ; e != null ; e = e.next) {
                if ((e.hash == hash) && e.key.equals(key)) {
                    Object old = e.value;
                    e.value = value;
                    return old;
                }
            }
        } else {
            for (Entry e = tab[0] ; e != null ; e = e.next) {
                if (e.key == null) {
                    Object old = e.value;
                    e.value = value;
                    return old;
                }
            }
        }
        modCount++;
        if (count >= threshold) {
            rehash();
            tab = table;
            index = (hash & 0x7FFFFFFF) % tab.length;
        }
        Entry e = new Entry(hash, key, value, tab[index]);
        tab[index] = e;
        listAddBefore(e, existEntry);
        count++;
        return null;
    }
    public Object put(int index, Object key, Object value) {
        if (index < 0 || index > count)
            throw new IndexOutOfBoundsException();
        Entry e = header.after;
        if (index == count)
            return putAhead(key, value, header); 
        else {
            for (int i = 0; i < index; i++)
                e = e.after;
            return putAhead(key, value, e);
        }
    }
    public Object put(Object key, Object value) {
        return putAhead(key, value, header);
    }
    public Object remove(Object key) {
        Entry tab[] = table;
        if (key != null) {
            int hash = key.hashCode();
            int index = (hash & 0x7FFFFFFF) % tab.length;
            for (Entry e = tab[index], prev = null; e != null;
                 prev = e, e = e.next) {
                if ((e.hash == hash) && e.key.equals(key)) {
                    modCount++;
                    if (prev != null)
                        prev.next = e.next;
                    else
                        tab[index] = e.next;
                    count--;
                    Object oldValue = e.value;
                    e.value = null;
                    listRemove(e);
                    return oldValue;
                }
            }
        } else {
            for (Entry e = tab[0], prev = null; e != null;
                 prev = e, e = e.next) {
                if (e.key == null) {
                    modCount++;
                    if (prev != null)
                        prev.next = e.next;
                    else
                        tab[0] = e.next;
                    count--;
                    Object oldValue = e.value;
                    e.value = null;
                    listRemove(e);
                    return oldValue;
                }
            }
        }
        return null;
    }
    public void putAll(Map t) {
        Iterator i = t.entrySet().iterator();
        while (i.hasNext()) {
            Map.Entry e = (Map.Entry) i.next();
            put(e.getKey(), e.getValue());
        }
    }
    public void clear() {
        Entry tab[] = table;
        modCount++;
        for (int index = tab.length; --index >= 0; )
            tab[index] = null;
        count = 0;
        header.before = header.after = header;
    }
    public Object clone() {
        return new LinkedHashMap(this);
    }
    private transient Set keySet = null;
    private transient Set entries = null;
    private transient Collection values = null;
    public Set keySet() {
        if (keySet == null) {
            keySet = new AbstractSet() {
                public Iterator iterator() {
                    return new HashIterator(KEYS);
                }
                public int size() {
                    return count;
                }
                public boolean contains(Object o) {
                    return containsKey(o);
                }
                public boolean remove(Object o) {
                    return LinkedHashMap.this.remove(o) != null;
                }
                public void clear() {
                    LinkedHashMap.this.clear();
                }
            };
        }
        return keySet;
    }
    public Collection values() {
        if (values==null) {
            values = new AbstractCollection() {
                public Iterator iterator() {
                    return new HashIterator(VALUES);
                }
                public int size() {
                    return count;
                }
                public boolean contains(Object o) {
                    return containsValue(o);
                }
                public void clear() {
                    LinkedHashMap.this.clear();
                }
            };
        }
        return values;
    }
    public Set entrySet() {
        if (entries==null) {
            entries = new AbstractSet() {
                public Iterator iterator() {
                    return new HashIterator(ENTRIES);
                }
                public boolean contains(Object o) {
                    if (!(o instanceof Map.Entry))
                        return false;
                    Map.Entry entry = (Map.Entry)o;
                    Object key = entry.getKey();
                    Entry tab[] = table;
                    int hash = (key==null ? 0 : key.hashCode());
                    int index = (hash & 0x7FFFFFFF) % tab.length;
                    for (Entry e = tab[index]; e != null; e = e.next)
                        if (e.hash==hash && e.equals(entry))
                            return true;
                    return false;
                }
                public boolean remove(Object o) {
                    if (!(o instanceof Map.Entry))
                        return false;
                    Map.Entry entry = (Map.Entry)o;
                    Object key = entry.getKey();
                    Entry tab[] = table;
                    int hash = (key==null ? 0 : key.hashCode());
                    int index = (hash & 0x7FFFFFFF) % tab.length;
                    for (Entry e = tab[index], prev = null; e != null;
                         prev = e, e = e.next) {
                        if (e.hash==hash && e.equals(entry)) {
                            modCount++;
                            if (prev != null)
                                prev.next = e.next;
                            else
                                tab[index] = e.next;
                            count--;
                            e.value = null;
                            listRemove(e);
                            return true;
                        }
                    }
                    return false;
                }
                public int size() {
                    return count;
                }
                public void clear() {
                    LinkedHashMap.this.clear();
                }
            };
        }
        return entries;
    }
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof LinkedHashMap))
            return false;
        LinkedHashMap t = (LinkedHashMap) o;
        if (t.size() != size())
            return false;
        Iterator i1 = entrySet().iterator();
        Iterator i2 = t.entrySet().iterator();
        while (i1.hasNext()) {
            Entry e1 = (Entry) i1.next();
            Entry e2 = (Entry) i2.next();
            Object key1 = e1.getKey();
            Object value1 = e1.getValue();
            Object key2 = e2.getKey();
            Object value2 = e2.getValue();
            if ((key1 == null ? key2 == null : key1.equals(key2)) &&
                (value1 == null ? value2 == null : value1.equals(value2))) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }
    private static class Entry implements Map.Entry {
        int hash;
        Object key;
        Object value;
        Entry next;
        Entry before, after;
        Entry(int hash, Object key, Object value, Entry next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
        public Object getKey() {
            return key;
        }
        public Object getValue() {
            return value;
        }
        public Object setValue(Object value) {
            Object oldValue = this.value;
            this.value = value;
            return oldValue;
        }
        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry e = (Map.Entry)o;
            return (key==null ? e.getKey()==null : key.equals(e.getKey())) &&
               (value==null ? e.getValue()==null : value.equals(e.getValue()));
        }
        public int hashCode() {
            return hash ^ (value==null ? 0 : value.hashCode());
        }
        public String toString() {
            return key+"="+value;
        }
    }
    private static final int KEYS = 0;
    private static final int VALUES = 1;
    private static final int ENTRIES = 2;
    private class HashIterator implements Iterator {
        private Entry[] table = LinkedHashMap.this.table;
        private Entry entry = null;
        private Entry lastReturned = null;
        private int type;
        private int expectedModCount = modCount;
        HashIterator(int type) {
            this.type = type;
            this.entry = LinkedHashMap.this.header.after;
        }
        public boolean hasNext() {
            return entry != header;
        }
        public Object next() {
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (entry == LinkedHashMap.this.header)
                throw new NoSuchElementException();
            Entry e = lastReturned = entry;
            entry = e.after;
            return type == KEYS ? e.key : (type == VALUES ? e.value : e);
        }
        public void remove() {
            if (lastReturned == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            Entry[] tab = LinkedHashMap.this.table;
            int index = (lastReturned.hash & 0x7FFFFFFF) % tab.length;
            for (Entry e = tab[index], prev = null; e != null;
                 prev = e, e = e.next) {
                if (e == lastReturned) {
                    modCount++;
                    expectedModCount++;
                    if (prev == null)
                        tab[index] = e.next;
                    else
                        prev.next = e.next;
                    count--;
                    listRemove(e);
                    lastReturned = null;
                    return;
                }
            }
            throw new ConcurrentModificationException();
        }
    }
    private void writeObject(java.io.ObjectOutputStream s)
        throws IOException
    {
        s.defaultWriteObject();
        s.writeInt(table.length);
        s.writeInt(count);
        for (Entry e = header.after; e != header; e = e.after) {
            s.writeObject(e.key);
            s.writeObject(e.value);
        }
    }
    private void readObject(java.io.ObjectInputStream s)
         throws IOException, ClassNotFoundException
    {
        s.defaultReadObject();
        int numBuckets = s.readInt();
        table = new Entry[numBuckets];
        header = new Entry(-1, null, null, null);
        header.before = header;
        header.after = header;
        int size = s.readInt();
        for (int i=0; i<size; i++) {
            Object key = s.readObject();
            Object value = s.readObject();
            put(key, value);
        }
    }
}
