public class SoftCache extends AbstractMap implements Map {
    static private class ValueCell extends SoftReference {
        static private Object INVALID_KEY = new Object();
        static private int dropped = 0;
        private Object key;
        private ValueCell(Object key, Object value, ReferenceQueue queue) {
            super(value, queue);
            this.key = key;
        }
        private static ValueCell create(Object key, Object value,
                                        ReferenceQueue queue)
        {
            if (value == null) return null;
            return new ValueCell(key, value, queue);
        }
        private static Object strip(Object val, boolean drop) {
            if (val == null) return null;
            ValueCell vc = (ValueCell)val;
            Object o = vc.get();
            if (drop) vc.drop();
            return o;
        }
        private boolean isValid() {
            return (key != INVALID_KEY);
        }
        private void drop() {
            super.clear();
            key = INVALID_KEY;
            dropped++;
        }
    }
    private Map hash;
    private ReferenceQueue queue = new ReferenceQueue();
    private void processQueue() {
        ValueCell vc;
        while ((vc = (ValueCell)queue.poll()) != null) {
            if (vc.isValid()) hash.remove(vc.key);
            else ValueCell.dropped--;
        }
    }
    public SoftCache(int initialCapacity, float loadFactor) {
        hash = new HashMap(initialCapacity, loadFactor);
    }
    public SoftCache(int initialCapacity) {
        hash = new HashMap(initialCapacity);
    }
    public SoftCache() {
        hash = new HashMap();
    }
    public int size() {
        return entrySet().size();
    }
    public boolean isEmpty() {
        return entrySet().isEmpty();
    }
    public boolean containsKey(Object key) {
        return ValueCell.strip(hash.get(key), false) != null;
    }
    protected Object fill(Object key) {
        return null;
    }
    public Object get(Object key) {
        processQueue();
        Object v = hash.get(key);
        if (v == null) {
            v = fill(key);
            if (v != null) {
                hash.put(key, ValueCell.create(key, v, queue));
                return v;
            }
        }
        return ValueCell.strip(v, false);
    }
    public Object put(Object key, Object value) {
        processQueue();
        ValueCell vc = ValueCell.create(key, value, queue);
        return ValueCell.strip(hash.put(key, vc), true);
    }
    public Object remove(Object key) {
        processQueue();
        return ValueCell.strip(hash.remove(key), true);
    }
    public void clear() {
        processQueue();
        hash.clear();
    }
    private static boolean valEquals(Object o1, Object o2) {
        return (o1 == null) ? (o2 == null) : o1.equals(o2);
    }
    private class Entry implements Map.Entry {
        private Map.Entry ent;
        private Object value;   
        Entry(Map.Entry ent, Object value) {
            this.ent = ent;
            this.value = value;
        }
        public Object getKey() {
            return ent.getKey();
        }
        public Object getValue() {
            return value;
        }
        public Object setValue(Object value) {
            return ent.setValue(ValueCell.create(ent.getKey(), value, queue));
        }
        public boolean equals(Object o) {
            if (! (o instanceof Map.Entry)) return false;
            Map.Entry e = (Map.Entry)o;
            return (valEquals(ent.getKey(), e.getKey())
                    && valEquals(value, e.getValue()));
        }
        public int hashCode() {
            Object k;
            return ((((k = getKey()) == null) ? 0 : k.hashCode())
                    ^ ((value == null) ? 0 : value.hashCode()));
        }
    }
    private class EntrySet extends AbstractSet {
        Set hashEntries = hash.entrySet();
        public Iterator iterator() {
            return new Iterator() {
                Iterator hashIterator = hashEntries.iterator();
                Entry next = null;
                public boolean hasNext() {
                    while (hashIterator.hasNext()) {
                        Map.Entry ent = (Map.Entry)hashIterator.next();
                        ValueCell vc = (ValueCell)ent.getValue();
                        Object v = null;
                        if ((vc != null) && ((v = vc.get()) == null)) {
                            continue;
                        }
                        next = new Entry(ent, v);
                        return true;
                    }
                    return false;
                }
                public Object next() {
                    if ((next == null) && !hasNext())
                        throw new NoSuchElementException();
                    Entry e = next;
                    next = null;
                    return e;
                }
                public void remove() {
                    hashIterator.remove();
                }
            };
        }
        public boolean isEmpty() {
            return !(iterator().hasNext());
        }
        public int size() {
            int j = 0;
            for (Iterator i = iterator(); i.hasNext(); i.next()) j++;
            return j;
        }
        public boolean remove(Object o) {
            processQueue();
            if (o instanceof Entry) return hashEntries.remove(((Entry)o).ent);
            else return false;
        }
    }
    private Set entrySet = null;
    public Set entrySet() {
        if (entrySet == null) entrySet = new EntrySet();
        return entrySet;
    }
}
