public class LinkedHashSet<E> extends HashSet<E> implements Set<E>, Cloneable,
        Serializable {
    private static final long serialVersionUID = -2851667679971038690L;
    public LinkedHashSet() {
        super(new LinkedHashMap<E, HashSet<E>>());
    }
    public LinkedHashSet(int capacity) {
        super(new LinkedHashMap<E, HashSet<E>>(capacity));
    }
    public LinkedHashSet(int capacity, float loadFactor) {
        super(new LinkedHashMap<E, HashSet<E>>(capacity, loadFactor));
    }
    public LinkedHashSet(Collection<? extends E> collection) {
        super(new LinkedHashMap<E, HashSet<E>>(collection.size() < 6 ? 11
                : collection.size() * 2));
        for (E e : collection) {
            add(e);
        }
    }
    @Override
    HashMap<E, HashSet<E>> createBackingMap(int capacity, float loadFactor) {
        return new LinkedHashMap<E, HashSet<E>>(capacity, loadFactor);
    }
}
