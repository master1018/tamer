public class HashSet<E> extends AbstractSet<E> implements Set<E>, Cloneable,
        Serializable {
    private static final long serialVersionUID = -5024744406713321676L;
    transient HashMap<E, HashSet<E>> backingMap;
    public HashSet() {
        this(new HashMap<E, HashSet<E>>());
    }
    public HashSet(int capacity) {
        this(new HashMap<E, HashSet<E>>(capacity));
    }
    public HashSet(int capacity, float loadFactor) {
        this(new HashMap<E, HashSet<E>>(capacity, loadFactor));
    }
    public HashSet(Collection<? extends E> collection) {
        this(new HashMap<E, HashSet<E>>(collection.size() < 6 ? 11 : collection
                .size() * 2));
        for (E e : collection) {
            add(e);
        }
    }
    HashSet(HashMap<E, HashSet<E>> backingMap) {
        this.backingMap = backingMap;
    }
    @Override
    public boolean add(E object) {
        return backingMap.put(object, this) == null;
    }
    @Override
    public void clear() {
        backingMap.clear();
    }
    @Override
    @SuppressWarnings("unchecked")
    public Object clone() {
        try {
            HashSet<E> clone = (HashSet<E>) super.clone();
            clone.backingMap = (HashMap<E, HashSet<E>>) backingMap.clone();
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); 
        }
    }
    @Override
    public boolean contains(Object object) {
        return backingMap.containsKey(object);
    }
    @Override
    public boolean isEmpty() {
        return backingMap.isEmpty();
    }
    @Override
    public Iterator<E> iterator() {
        return backingMap.keySet().iterator();
    }
    @Override
    public boolean remove(Object object) {
        return backingMap.remove(object) != null;
    }
    @Override
    public int size() {
        return backingMap.size();
    }
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeInt(backingMap.table.length);
        stream.writeFloat(HashMap.DEFAULT_LOAD_FACTOR);
        stream.writeInt(size());
        for (E e : this) {
            stream.writeObject(e);
        }
    }
    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
        int length = stream.readInt();
        float loadFactor = stream.readFloat();
        backingMap = createBackingMap(length, loadFactor);
        int elementCount = stream.readInt();
        for (int i = elementCount; --i >= 0;) {
            E key = (E) stream.readObject();
            backingMap.put(key, this);
        }
    }
    HashMap<E, HashSet<E>> createBackingMap(int capacity, float loadFactor) {
        return new HashMap<E, HashSet<E>>(capacity, loadFactor);
    }
}
