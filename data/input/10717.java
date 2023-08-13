public class BakedArrayList extends ArrayList {
    private int _hashCode;
    public BakedArrayList(int size) {
        super(size);
    }
    public BakedArrayList(java.util.List data) {
        this(data.size());
        for (int counter = 0, max = data.size(); counter < max; counter++){
            add(data.get(counter));
        }
        cacheHashCode();
    }
    public void cacheHashCode() {
        _hashCode = 1;
        for (int counter = size() - 1; counter >= 0; counter--) {
            _hashCode = 31 * _hashCode + get(counter).hashCode();
        }
    }
    public int hashCode() {
        return _hashCode;
    }
    public boolean equals(Object o) {
        BakedArrayList list = (BakedArrayList)o;
        int size = size();
        if (list.size() != size) {
            return false;
        }
        while (size-- > 0) {
            if (!get(size).equals(list.get(size))) {
                return false;
            }
        }
        return true;
    }
}
