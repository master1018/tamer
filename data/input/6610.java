class IdentityHashtableEntry {
        int hash;
        Object key;
        Object value;
        IdentityHashtableEntry next;
}
public final class IdentityHashtable extends Dictionary {
    private transient IdentityHashtableEntry table[];
    private transient int count;
    private int threshold;
    private float loadFactor;
    public IdentityHashtable(int initialCapacity, float loadFactor) {
        if ((initialCapacity <= 0) || (loadFactor <= 0.0)) {
            throw new IllegalArgumentException();
        }
        this.loadFactor = loadFactor;
        table = new IdentityHashtableEntry[initialCapacity];
        threshold = (int)(initialCapacity * loadFactor);
    }
    public IdentityHashtable(int initialCapacity) {
        this(initialCapacity, 0.75f);
    }
    public IdentityHashtable() {
        this(101, 0.75f);
    }
    public int size() {
        return count;
    }
    public boolean isEmpty() {
        return count == 0;
    }
    public Enumeration keys() {
        return new IdentityHashtableEnumerator(table, true);
    }
    public Enumeration elements() {
        return new IdentityHashtableEnumerator(table, false);
    }
    public boolean contains(Object value) {
        if (value == null) {
            throw new NullPointerException();
        }
        IdentityHashtableEntry tab[] = table;
        for (int i = tab.length ; i-- > 0 ;) {
            for (IdentityHashtableEntry e = tab[i] ; e != null ; e = e.next) {
                if (e.value == value) {
                    return true;
                }
            }
        }
        return false;
    }
    public boolean containsKey(Object key) {
        IdentityHashtableEntry tab[] = table;
        int hash = System.identityHashCode(key);
        int index = (hash & 0x7FFFFFFF) % tab.length;
        for (IdentityHashtableEntry e = tab[index] ; e != null ; e = e.next) {
            if ((e.hash == hash) && e.key == key) {
                return true;
            }
        }
        return false;
    }
    public Object get(Object key) {
        IdentityHashtableEntry tab[] = table;
        int hash = System.identityHashCode(key);
        int index = (hash & 0x7FFFFFFF) % tab.length;
        for (IdentityHashtableEntry e = tab[index] ; e != null ; e = e.next) {
            if ((e.hash == hash) && e.key == key) {
                return e.value;
            }
        }
        return null;
    }
    protected void rehash() {
        int oldCapacity = table.length;
        IdentityHashtableEntry oldTable[] = table;
        int newCapacity = oldCapacity * 2 + 1;
        IdentityHashtableEntry newTable[] = new IdentityHashtableEntry[newCapacity];
        threshold = (int)(newCapacity * loadFactor);
        table = newTable;
        for (int i = oldCapacity ; i-- > 0 ;) {
            for (IdentityHashtableEntry old = oldTable[i] ; old != null ; ) {
                IdentityHashtableEntry e = old;
                old = old.next;
                int index = (e.hash & 0x7FFFFFFF) % newCapacity;
                e.next = newTable[index];
                newTable[index] = e;
            }
        }
    }
    public Object put(Object key, Object value) {
        if (value == null) {
            throw new NullPointerException();
        }
        IdentityHashtableEntry tab[] = table;
        int hash = System.identityHashCode(key);
        int index = (hash & 0x7FFFFFFF) % tab.length;
        for (IdentityHashtableEntry e = tab[index] ; e != null ; e = e.next) {
            if ((e.hash == hash) && e.key == key) {
                Object old = e.value;
                e.value = value;
                return old;
            }
        }
        if (count >= threshold) {
            rehash();
            return put(key, value);
        }
        IdentityHashtableEntry e = new IdentityHashtableEntry();
        e.hash = hash;
        e.key = key;
        e.value = value;
        e.next = tab[index];
        tab[index] = e;
        count++;
        return null;
    }
    public Object remove(Object key) {
        IdentityHashtableEntry tab[] = table;
        int hash = System.identityHashCode(key);
        int index = (hash & 0x7FFFFFFF) % tab.length;
        for (IdentityHashtableEntry e = tab[index], prev = null ; e != null ; prev = e, e = e.next) {
            if ((e.hash == hash) && e.key == key) {
                if (prev != null) {
                    prev.next = e.next;
                } else {
                    tab[index] = e.next;
                }
                count--;
                return e.value;
            }
        }
        return null;
    }
    public void clear() {
        IdentityHashtableEntry tab[] = table;
        for (int index = tab.length; --index >= 0; )
            tab[index] = null;
        count = 0;
    }
    public String toString() {
        int max = size() - 1;
        StringBuffer buf = new StringBuffer();
        Enumeration k = keys();
        Enumeration e = elements();
        buf.append("{");
        for (int i = 0; i <= max; i++) {
            String s1 = k.nextElement().toString();
            String s2 = e.nextElement().toString();
            buf.append(s1 + "=" + s2);
            if (i < max) {
                buf.append(", ");
            }
        }
        buf.append("}");
        return buf.toString();
    }
}
