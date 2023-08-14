public class X509NameElementList {
    private DERObjectIdentifier key0;
    private DERObjectIdentifier key1;
    private DERObjectIdentifier key2;
    private DERObjectIdentifier key3;
    private String value0;
    private String value1;
    private String value2;
    private String value3;
    private ArrayList<Object> rest;
    private BitSet added = new BitSet();
    private int size;
    public void add(DERObjectIdentifier key, String value) {
        add(key, value, false);
    }
    public void add(DERObjectIdentifier key, String value, boolean added) {
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
                rest = new ArrayList<Object>();
            }
            default: {
                rest.add(key);
                rest.add(value);
                break;
            }
        }
        if (added) {
            this.added.set(sz);
        }
        size = sz + 1;
    }
    public void setLastAddedFlag() {
        added.set(size - 1);
    }
    public int size() {
        return size;
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
            default: return (DERObjectIdentifier) rest.get((n - 4) * 2);
        }
    }
    public String getValue(int n) {
        if ((n < 0) || (n >= size)) {
            throw new IndexOutOfBoundsException(Integer.toString(n));
        }
        switch (n) {
            case 0: return value0;
            case 1: return value1;
            case 2: return value2;
            case 3: return value3;
            default: return (String) rest.get(((n - 4) * 2) + 1);
        }
    }
    public boolean getAdded(int n) {
        if ((n < 0) || (n >= size)) {
            throw new IndexOutOfBoundsException(Integer.toString(n));
        }
        return added.get(n);
    }
    public X509NameElementList reverse() {
        X509NameElementList result = new X509NameElementList();
        for (int i = size - 1; i >= 0; i--) {
            result.add(getKey(i), getValue(i), getAdded(i));
        }
        return result;
    }
}
