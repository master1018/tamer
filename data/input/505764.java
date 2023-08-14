public class TreeSet<E> extends AbstractSet<E> implements SortedSet<E>,
        Cloneable, Serializable {
    private static final long serialVersionUID = -2479143000061671589L;
    private transient SortedMap<E, Object> backingMap;
    private TreeSet(SortedMap<E, Object> map) {
        this.backingMap = map;
    }
    public TreeSet() {
        backingMap = new TreeMap<E, Object>();
    }
    public TreeSet(Collection<? extends E> collection) {
        this();
        addAll(collection);
    }
    public TreeSet(Comparator<? super E> comparator) {
        backingMap = new TreeMap<E, Object>(comparator);
    }
    public TreeSet(SortedSet<E> set) {
        this(set.comparator());
        Iterator<E> it = set.iterator();
        while (it.hasNext()) {
            add(it.next());
        }
    }
    @Override
    public boolean add(E object) {
        return backingMap.put(object, Boolean.TRUE) == null;
    }
    @Override
    public boolean addAll(Collection<? extends E> collection) {
        return super.addAll(collection);
    }
    @Override
    public void clear() {
        backingMap.clear();
    }
    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        try {
            TreeSet<E> clone = (TreeSet<E>) super.clone();
            if (backingMap instanceof TreeMap) {
                clone.backingMap = (SortedMap<E, Object>) ((TreeMap<E, Object>) backingMap)
                        .clone();
            } else {
                clone.backingMap = new TreeMap<E, Object>(backingMap);
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); 
        }
    }
    public Comparator<? super E> comparator() {
        return backingMap.comparator();
    }
    @Override
    public boolean contains(Object object) {
        return backingMap.containsKey(object);
    }
    public E first() {
        return backingMap.firstKey();
    }
    @SuppressWarnings("unchecked")
    public SortedSet<E> headSet(E end) {
        Comparator<? super E> c = backingMap.comparator();
        if (c == null) {
            ((Comparable<E>) end).compareTo(end);
        } else {
            c.compare(end, end);
        }
        return new TreeSet<E>(backingMap.headMap(end));
    }
    @Override
    public boolean isEmpty() {
        return backingMap.isEmpty();
    }
    @Override
    public Iterator<E> iterator() {
        return backingMap.keySet().iterator();
    }
    public E last() {
        return backingMap.lastKey();
    }
    @Override
    public boolean remove(Object object) {
        return backingMap.remove(object) != null;
    }
    @Override
    public int size() {
        return backingMap.size();
    }
    @SuppressWarnings("unchecked")
    public SortedSet<E> subSet(E start, E end) {
        Comparator<? super E> c = backingMap.comparator();
        if (c == null) {
            if (((Comparable<E>) start).compareTo(end) <= 0) {
                return new TreeSet<E>(backingMap.subMap(start, end));
            }
        } else {
            if (c.compare(start, end) <= 0) {
                return new TreeSet<E>(backingMap.subMap(start, end));
            }
        }
        throw new IllegalArgumentException();
    }
    @SuppressWarnings("unchecked")
    public SortedSet<E> tailSet(E start) {
        Comparator<? super E> c = backingMap.comparator();
        if (c == null) {
            ((Comparable<E>) start).compareTo(start);
        } else {
            c.compare(start, start);
        }
        return new TreeSet<E>(backingMap.tailMap(start));
    }
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeObject(backingMap.comparator());
        int size = backingMap.size();
        stream.writeInt(size);
        if (size > 0) {
            Iterator<E> it = backingMap.keySet().iterator();
            while (it.hasNext()) {
                stream.writeObject(it.next());
            }
        }
    }
    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
        TreeMap<E, Object> map = new TreeMap<E, Object>(
                (Comparator<? super E>) stream.readObject());
        int size = stream.readInt();
        if (size > 0) {
            TreeMap.Node<E, Object> lastNode = null;
            for(int i=0; i<size; i++) {
                E elem = (E)stream.readObject();
                lastNode = map.addToLast(lastNode,elem, Boolean.TRUE);
            }
        }
        backingMap = map;
    }
}
