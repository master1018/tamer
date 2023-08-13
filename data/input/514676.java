public class PriorityQueue<E> extends AbstractQueue<E> implements Serializable {
    private static final long serialVersionUID = -7720805057305804111L;
    private static final int DEFAULT_CAPACITY = 11;
    private static final double DEFAULT_INIT_CAPACITY_RATIO = 1.1;
    private static final int DEFAULT_CAPACITY_RATIO = 2;
    private int size;
    private Comparator<? super E> comparator;
    private transient E[] elements;
    public PriorityQueue() {
        this(DEFAULT_CAPACITY);
    }
    public PriorityQueue(int initialCapacity) {
        this(initialCapacity, null);
    }
    public PriorityQueue(int initialCapacity, Comparator<? super E> comparator) {
        if (initialCapacity < 1) {
            throw new IllegalArgumentException();
        }
        elements = newElementArray(initialCapacity);
        this.comparator = comparator;
    }
    public PriorityQueue(Collection<? extends E> c) {
        if (c instanceof PriorityQueue) {
            getFromPriorityQueue((PriorityQueue<? extends E>) c);
        } else if (c instanceof SortedSet) {
            getFromSortedSet((SortedSet<? extends E>) c);
        } else {
            initSize(c);
            addAll(c);
        }
    }
    public PriorityQueue(PriorityQueue<? extends E> c) {
        getFromPriorityQueue(c);
    }
    public PriorityQueue(SortedSet<? extends E> c) {
        getFromSortedSet(c);
    }
    @Override
    public Iterator<E> iterator() {
        return new PriorityIterator();
    }
    @Override
    public int size() {
        return size;
    }
    @Override
    public void clear() {
        Arrays.fill(elements, null);
        size = 0;
    }
    public boolean offer(E o) {
        if (null == o) {
            throw new NullPointerException();
        }
        growToSize(size + 1);
        elements[size] = o;
        siftUp(size++);
        return true;
    }
    public E poll() {
        if (isEmpty()) {
            return null;
        }
        E result = elements[0];
        removeAt(0);
        return result;
    }
    public E peek() {
        if (isEmpty()) {
            return null;
        }
        return elements[0];
    }
    public Comparator<? super E> comparator() {
        return comparator;
    }
    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        if (o == null) {
            return false;
        }
        int targetIndex;
        for (targetIndex = 0; targetIndex < size; targetIndex++) {
            if (0 == this.compare((E) o, elements[targetIndex])) {
                break;
            }
        }
        if (size == 0 || size == targetIndex) {
            return false;
        }
        removeAt(targetIndex);
        return true;
    }
    @Override
    public boolean add(E o) {
        return offer(o);
    }
    private class PriorityIterator implements Iterator<E> {
        private int currentIndex = -1;
        private boolean allowRemove = false;
        public boolean hasNext() {
            return currentIndex < size - 1;
        }
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            allowRemove = true;
            return elements[++currentIndex];
        }
        public void remove() {
            if (!allowRemove) {
                throw new IllegalStateException();
            }
            allowRemove = false;
            removeAt(currentIndex--);
        }
    }
    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();
        int capacity = in.readInt();
        elements = newElementArray(capacity);
        for (int i = 0; i < size; i++) {
            elements[i] = (E) in.readObject();
        }
    }
    @SuppressWarnings("unchecked")
    private E[] newElementArray(int capacity) {
        return (E[]) new Object[capacity];
    }
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(elements.length);
        for (int i = 0; i < size; i++) {
            out.writeObject(elements[i]);
        }
    }
    @SuppressWarnings("unchecked")
    private void getFromPriorityQueue(PriorityQueue<? extends E> c) {
        initSize(c);
        comparator = (Comparator<? super E>) c.comparator();
        System.arraycopy(c.elements, 0, elements, 0, c.size());
        size = c.size();
    }
    @SuppressWarnings("unchecked")
    private void getFromSortedSet(SortedSet<? extends E> c) {
        initSize(c);
        comparator = (Comparator<? super E>) c.comparator();
        Iterator<? extends E> iter = c.iterator();
        while (iter.hasNext()) {
            elements[size++] = iter.next();
        }
    }
    private void removeAt(int index) {
        size--;
        elements[index] = elements[size];
        siftDown(index);
        elements[size] = null;
    }
    private int compare(E o1, E o2) {
        if (null != comparator) {
            return comparator.compare(o1, o2);
        }
        return ((Comparable<? super E>) o1).compareTo(o2);
    }
    private void siftUp(int childIndex) {
        E target = elements[childIndex];
        int parentIndex;
        while (childIndex > 0) {
            parentIndex = (childIndex - 1) / 2;
            E parent = elements[parentIndex];
            if (compare(parent, target) <= 0) {
                break;
            }
            elements[childIndex] = parent;
            childIndex = parentIndex;
        }
        elements[childIndex] = target;
    }
    private void siftDown(int rootIndex) {
        E target = elements[rootIndex];
        int childIndex;
        while ((childIndex = rootIndex * 2 + 1) < size) {
            if (childIndex + 1 < size
                    && compare(elements[childIndex + 1], elements[childIndex]) < 0) {
                childIndex++;
            }
            if (compare(target, elements[childIndex]) <= 0) {
                break;
            }
            elements[rootIndex] = elements[childIndex];
            rootIndex = childIndex;
        }
        elements[rootIndex] = target;
    }
    private void initSize(Collection<? extends E> c) {
        if (null == c) {
            throw new NullPointerException();
        }
        if (c.isEmpty()) {
            elements = newElementArray(1);
        } else {
            int capacity = (int) Math.ceil(c.size()
                    * DEFAULT_INIT_CAPACITY_RATIO);
            elements = newElementArray(capacity);
        }
    }
    private void growToSize(int size) {
        if (size > elements.length) {
            E[] newElements = newElementArray(size * DEFAULT_CAPACITY_RATIO);
            System.arraycopy(elements, 0, newElements, 0, elements.length);
            elements = newElements;
        }
    }
}
