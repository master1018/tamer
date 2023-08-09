public final class OrderedTable {
    private DERObjectIdentifier key0;
    private DERObjectIdentifier key1;
    private DERObjectIdentifier key2;
    private DERObjectIdentifier key3;
    private Object value0;
    private Object value1;
    private Object value2;
    private Object value3;
    private Object[] rest;
    private int size;
    public void add(DERObjectIdentifier key, Object value) {
        if (key == null) {
            throw new NullPointerException("key == null");
        }
        if (value == null) {
            throw new NullPointerException("value == null");
        }
        int sz = size;
        switch (sz) {
            case 0: {
                key0 = key;
                value0 = value;
                break;
            }
            case 1: {
                key1 = key;
                value1 = value;
                break;
            }
            case 2: {
                key2 = key;
                value2 = value;
                break;
            }
            case 3: {
                key3 = key;
                value3 = value;
                break;
            }
            case 4: {
                rest = new Object[10];
                rest[0] = key;
                rest[1] = value;
                break;
            }
            default: {
                int index = (sz - 4) * 2;
                int index1 = index + 1;
                if (index1 >= rest.length) {
                    Object[] newRest = new Object[index1 * 2 + 10];
                    System.arraycopy(rest, 0, newRest, 0, rest.length);
                    rest = newRest;
                }
                rest[index] = key;
                rest[index1] = value;
                break;
            }
        }
        size = sz + 1;
    }
    public int size() {
        return size;
    }
    public Object get(DERObjectIdentifier key) {
        int keyHash = key.hashCode();
        int sz = size;
        for (int i = 0; i < size; i++) {
            DERObjectIdentifier probe = getKey(i);
            if ((probe.hashCode() == keyHash) &&
                    probe.equals(key)) {
                return getValue(i);
            }
        }
        return null;
    }
    public DERObjectIdentifier getKey(int n) {
        if ((n < 0) || (n >= size)) {
            throw new IndexOutOfBoundsException(Integer.toString(n));
        }
        switch (n) {
            case 0: return key0;
            case 1: return key1;
            case 2: return key2;
            case 3: return key3;
            default: return (DERObjectIdentifier) rest[(n - 4) * 2];
        }
    }
    public Object getValue(int n) {
        if ((n < 0) || (n >= size)) {
            throw new IndexOutOfBoundsException(Integer.toString(n));
        }
        switch (n) {
            case 0: return value0;
            case 1: return value1;
            case 2: return value2;
            case 3: return value3;
            default: return rest[((n - 4) * 2) + 1];
        }
    }
    public Enumeration getKeys() {
        return new KeyEnumeration();
    }
    private class KeyEnumeration implements Enumeration {
        private final int origSize = size;
        private int at = 0;
        public boolean hasMoreElements() {
            if (size != origSize) {
                throw new ConcurrentModificationException();
            }
            return at < origSize;
        }
        public Object nextElement() {
            if (size != origSize) {
                throw new ConcurrentModificationException();
            }
            return getKey(at++);
        }
    }
}
