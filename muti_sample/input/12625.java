public abstract class PreHashedMap<V>
    extends AbstractMap<String,V>
{
    private final int rows;
    private final int size;
    private final int shift;
    private final int mask;
    private final Object[] ht;
    protected PreHashedMap(int rows, int size, int shift, int mask) {
        this.rows = rows;
        this.size = size;
        this.shift = shift;
        this.mask = mask;
        this.ht = new Object[rows];
        init(ht);
    }
    protected abstract void init(Object[] ht);
    private V toV(Object x) {
        return (V)x;
    }
    public V get(Object k) {
        int h = (k.hashCode() >> shift) & mask;
        Object[] a = (Object[])ht[h];
        if (a == null) return null;
        for (;;) {
            if (a[0].equals(k))
                return toV(a[1]);
            if (a.length < 3)
                return null;
            a = (Object[])a[2];
        }
    }
    public V put(String k, V v) {
        int h = (k.hashCode() >> shift) & mask;
        Object[] a = (Object[])ht[h];
        if (a == null)
            throw new UnsupportedOperationException(k);
        for (;;) {
            if (a[0].equals(k)) {
                V ov = toV(a[1]);
                a[1] = v;
                return ov;
            }
            if (a.length < 3)
                throw new UnsupportedOperationException(k);
            a = (Object[])a[2];
        }
    }
    public Set<String> keySet() {
        return new AbstractSet<String> () {
            public int size() {
                return size;
            }
            public Iterator<String> iterator() {
                return new Iterator<String>() {
                    private int i = -1;
                    Object[] a = null;
                    String cur = null;
                    private boolean findNext() {
                        if (a != null) {
                            if (a.length == 3) {
                                a = (Object[])a[2];
                                cur = (String)a[0];
                                return true;
                            }
                            i++;
                            a = null;
                        }
                        cur = null;
                        if (i >= rows)
                            return false;
                        if (i < 0 || ht[i] == null) {
                            do {
                                if (++i >= rows)
                                    return false;
                            } while (ht[i] == null);
                        }
                        a = (Object[])ht[i];
                        cur = (String)a[0];
                        return true;
                    }
                    public boolean hasNext() {
                        if (cur != null)
                            return true;
                        return findNext();
                    }
                    public String next() {
                        if (cur == null) {
                            if (!findNext())
                                throw new NoSuchElementException();
                        }
                        String s = cur;
                        cur = null;
                        return s;
                    }
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }
    public Set<Map.Entry<String,V>> entrySet() {
        return new AbstractSet<Map.Entry<String,V>> () {
            public int size() {
                return size;
            }
            public Iterator<Map.Entry<String,V>> iterator() {
                return new Iterator<Map.Entry<String,V>>() {
                    final Iterator<String> i = keySet().iterator();
                    public boolean hasNext() {
                        return i.hasNext();
                    }
                    public Map.Entry<String,V> next() {
                        return new Map.Entry<String,V>() {
                            String k = i.next();
                            public String getKey() { return k; }
                            public V getValue() { return get(k); }
                            public int hashCode() {
                                V v = get(k);
                                return (k.hashCode()
                                        + (v == null
                                           ? 0
                                           : v.hashCode()));
                            }
                            public boolean equals(Object ob) {
                                if (ob == this)
                                    return true;
                                if (!(ob instanceof Map.Entry))
                                    return false;
                                Map.Entry<String,V> that
                                    = (Map.Entry<String,V>)ob;
                                return ((this.getKey() == null
                                         ? that.getKey() == null
                                         : this.getKey()
                                               .equals(that.getKey()))
                                        &&
                                        (this.getValue() == null
                                         ? that.getValue() == null
                                         : this.getValue()
                                               .equals(that.getValue())));
                            }
                            public V setValue(V v) {
                                throw new UnsupportedOperationException();
                            }
                        };
                    }
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }
}
