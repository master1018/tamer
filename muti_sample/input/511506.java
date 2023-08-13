public class ProcessMap<E> {
    final HashMap<String, SparseArray<E>> mMap
            = new HashMap<String, SparseArray<E>>();
    public E get(String name, int uid) {
        SparseArray<E> uids = mMap.get(name);
        if (uids == null) return null;
        return uids.get(uid);
    }
    public E put(String name, int uid, E value) {
        SparseArray<E> uids = mMap.get(name);
        if (uids == null) {
            uids = new SparseArray<E>(2);
            mMap.put(name, uids);
        }
        uids.put(uid, value);
        return value;
    }
    public void remove(String name, int uid) {
        SparseArray<E> uids = mMap.get(name);
        if (uids != null) {
            uids.remove(uid);
            if (uids.size() == 0) {
                mMap.remove(name);
            }
        }
    }
    public HashMap<String, SparseArray<E>> getMap() {
        return mMap;
    }
}
