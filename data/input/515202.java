public class Vector<E> extends AbstractList<E> implements List<E>,
        RandomAccess, Cloneable, Serializable {
    private static final long serialVersionUID = -2767605614048989439L;
    protected int elementCount;
    protected Object[] elementData;
    protected int capacityIncrement;
    private static final int DEFAULT_SIZE = 10;
    public Vector() {
        this(DEFAULT_SIZE, 0);
    }
    public Vector(int capacity) {
        this(capacity, 0);
    }
    public Vector(int capacity, int capacityIncrement) {
        if (capacity < 0) {
            throw new IllegalArgumentException();
        }
        elementData = newElementArray(capacity);
        elementCount = 0;
        this.capacityIncrement = capacityIncrement;
    }
    public Vector(Collection<? extends E> collection) {
        this(collection.size(), 0);
        Iterator<? extends E> it = collection.iterator();
        while (it.hasNext()) {
            elementData[elementCount++] = it.next();
        }
    }
    @SuppressWarnings("unchecked")
    private E[] newElementArray(int size) {
        return (E[]) new Object[size];
    }
    @Override
    public void add(int location, E object) {
        insertElementAt(object, location);
    }
    @Override
    public synchronized boolean add(E object) {
        if (elementCount == elementData.length) {
            growByOne();
        }
        elementData[elementCount++] = object;
        modCount++;
        return true;
    }
    @Override
    public synchronized boolean addAll(int location,
            Collection<? extends E> collection) {
        if (0 <= location && location <= elementCount) {
            int size = collection.size();
            if (size == 0) {
                return false;
            }
            int required = size - (elementData.length - elementCount);
            if (required > 0) {
                growBy(required);
            }
            int count = elementCount - location;
            if (count > 0) {
                System.arraycopy(elementData, location, elementData, location
                        + size, count);
            }
            Iterator<? extends E> it = collection.iterator();
            while (it.hasNext()) {
                elementData[location++] = it.next();
            }
            elementCount += size;
            modCount++;
            return true;
        }
        throw new ArrayIndexOutOfBoundsException(location);
    }
    @Override
    public synchronized boolean addAll(Collection<? extends E> collection) {
        return addAll(elementCount, collection);
    }
    public synchronized void addElement(E object) {
        if (elementCount == elementData.length) {
            growByOne();
        }
        elementData[elementCount++] = object;
        modCount++;
    }
    public synchronized int capacity() {
        return elementData.length;
    }
    @Override
    public void clear() {
        removeAllElements();
    }
    @Override
    @SuppressWarnings("unchecked")
    public synchronized Object clone() {
        try {
            Vector<E> vector = (Vector<E>) super.clone();
            vector.elementData = elementData.clone();
            return vector;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); 
        }
    }
    @Override
    public boolean contains(Object object) {
        return indexOf(object, 0) != -1;
    }
    @Override
    public synchronized boolean containsAll(Collection<?> collection) {
        return super.containsAll(collection);
    }
    public synchronized void copyInto(Object[] elements) {
        System.arraycopy(elementData, 0, elements, 0, elementCount);
    }
    @SuppressWarnings("unchecked")
    public synchronized E elementAt(int location) {
        if (location < elementCount) {
            return (E) elementData[location];
        }
        throw new ArrayIndexOutOfBoundsException(location);
    }
    public Enumeration<E> elements() {
        return new Enumeration<E>() {
            int pos = 0;
            public boolean hasMoreElements() {
                return pos < elementCount;
            }
            @SuppressWarnings("unchecked")
            public E nextElement() {
                synchronized (Vector.this) {
                    if (pos < elementCount) {
                        return (E) elementData[pos++];
                    }
                }
                throw new NoSuchElementException();
            }
        };
    }
    public synchronized void ensureCapacity(int minimumCapacity) {
        if (elementData.length < minimumCapacity) {
            int next = (capacityIncrement <= 0 ? elementData.length
                    : capacityIncrement)
                    + elementData.length;
            grow(minimumCapacity > next ? minimumCapacity : next);
        }
    }
    @Override
    public synchronized boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof List) {
            List<?> list = (List<?>) object;
            if (list.size() != elementCount) {
                return false;
            }
            int index = 0;
            Iterator<?> it = list.iterator();
            while (it.hasNext()) {
                Object e1 = elementData[index++], e2 = it.next();
                if (!(e1 == null ? e2 == null : e1.equals(e2))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    @SuppressWarnings("unchecked")
    public synchronized E firstElement() {
        if (elementCount > 0) {
            return (E) elementData[0];
        }
        throw new NoSuchElementException();
    }
    @Override
    public E get(int location) {
        return elementAt(location);
    }
    private void grow(int newCapacity) {
        E[] newData = newElementArray(newCapacity);
        assert elementCount <= newCapacity;
        System.arraycopy(elementData, 0, newData, 0, elementCount);
        elementData = newData;
    }
    private void growByOne() {
        int adding = 0;
        if (capacityIncrement <= 0) {
            if ((adding = elementData.length) == 0) {
                adding = 1;
            }
        } else {
            adding = capacityIncrement;
        }
        E[] newData = newElementArray(elementData.length + adding);
        System.arraycopy(elementData, 0, newData, 0, elementCount);
        elementData = newData;
    }
    private void growBy(int required) {
        int adding = 0;
        if (capacityIncrement <= 0) {
            if ((adding = elementData.length) == 0) {
                adding = required;
            }
            while (adding < required) {
                adding += adding;
            }
        } else {
            adding = (required / capacityIncrement) * capacityIncrement;
            if (adding < required) {
                adding += capacityIncrement;
            }
        }
        E[] newData = newElementArray(elementData.length + adding);
        System.arraycopy(elementData, 0, newData, 0, elementCount);
        elementData = newData;
    }
    @Override
    public synchronized int hashCode() {
        int result = 1;
        for (int i = 0; i < elementCount; i++) {
            result = (31 * result)
                    + (elementData[i] == null ? 0 : elementData[i].hashCode());
        }
        return result;
    }
    @Override
    public int indexOf(Object object) {
        return indexOf(object, 0);
    }
    public synchronized int indexOf(Object object, int location) {
        if (object != null) {
            for (int i = location; i < elementCount; i++) {
                if (object.equals(elementData[i])) {
                    return i;
                }
            }
        } else {
            for (int i = location; i < elementCount; i++) {
                if (elementData[i] == null) {
                    return i;
                }
            }
        }
        return -1;
    }
    public synchronized void insertElementAt(E object, int location) {
        if (0 <= location && location <= elementCount) {
            if (elementCount == elementData.length) {
                growByOne();
            }
            int count = elementCount - location;
            if (count > 0) {
                System.arraycopy(elementData, location, elementData,
                        location + 1, count);
            }
            elementData[location] = object;
            elementCount++;
            modCount++;
        } else {
            throw new ArrayIndexOutOfBoundsException(location);
        }
    }
    @Override
    public synchronized boolean isEmpty() {
        return elementCount == 0;
    }
    @SuppressWarnings("unchecked")
    public synchronized E lastElement() {
        try {
            return (E) elementData[elementCount - 1];
        } catch (IndexOutOfBoundsException e) {
            throw new NoSuchElementException();
        }
    }
    @Override
    public synchronized int lastIndexOf(Object object) {
        return lastIndexOf(object, elementCount - 1);
    }
    public synchronized int lastIndexOf(Object object, int location) {
        if (location < elementCount) {
            if (object != null) {
                for (int i = location; i >= 0; i--) {
                    if (object.equals(elementData[i])) {
                        return i;
                    }
                }
            } else {
                for (int i = location; i >= 0; i--) {
                    if (elementData[i] == null) {
                        return i;
                    }
                }
            }
            return -1;
        }
        throw new ArrayIndexOutOfBoundsException(location);
    }
    @SuppressWarnings("unchecked")
    @Override
    public synchronized E remove(int location) {
        if (location < elementCount) {
            E result = (E) elementData[location];
            elementCount--;
            int size = elementCount - location;
            if (size > 0) {
                System.arraycopy(elementData, location + 1, elementData,
                        location, size);
            }
            elementData[elementCount] = null;
            modCount++;
            return result;
        }
        throw new ArrayIndexOutOfBoundsException(location);
    }
    @Override
    public boolean remove(Object object) {
        return removeElement(object);
    }
    @Override
    public synchronized boolean removeAll(Collection<?> collection) {
        return super.removeAll(collection);
    }
    public synchronized void removeAllElements() {
        for (int i = 0; i < elementCount; i++) {
            elementData[i] = null;
        }
        modCount++;
        elementCount = 0;
    }
    public synchronized boolean removeElement(Object object) {
        int index;
        if ((index = indexOf(object, 0)) == -1) {
            return false;
        }
        removeElementAt(index);
        return true;
    }
    public synchronized void removeElementAt(int location) {
        if (0 <= location && location < elementCount) {
            elementCount--;
            int size = elementCount - location;
            if (size > 0) {
                System.arraycopy(elementData, location + 1, elementData,
                        location, size);
            }
            elementData[elementCount] = null;
            modCount++;
        } else {
            throw new ArrayIndexOutOfBoundsException(location);
        }
    }
    @Override
    protected void removeRange(int start, int end) {
        if (start >= 0 && start <= end && end <= elementCount) {
            if (start == end) {
                return;
            }
            if (end != elementCount) {
                System.arraycopy(elementData, end, elementData, start,
                        elementCount - end);
                int newCount = elementCount - (end - start);
                Arrays.fill(elementData, newCount, elementCount, null);
                elementCount = newCount;
            } else {
                Arrays.fill(elementData, start, elementCount, null);
                elementCount = start;
            }
            modCount++;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }
    @Override
    public synchronized boolean retainAll(Collection<?> collection) {
        return super.retainAll(collection);
    }
    @SuppressWarnings("unchecked")
    @Override
    public synchronized E set(int location, E object) {
        if (location < elementCount) {
            E result = (E) elementData[location];
            elementData[location] = object;
            return result;
        }
        throw new ArrayIndexOutOfBoundsException(location);
    }
    public synchronized void setElementAt(E object, int location) {
        if (location < elementCount) {
            elementData[location] = object;
        } else {
            throw new ArrayIndexOutOfBoundsException(location);
        }
    }
    public synchronized void setSize(int length) {
        if (length == elementCount) {
            return;
        }
        ensureCapacity(length);
        if (elementCount > length) {
            Arrays.fill(elementData, length, elementCount, null);
        }
        elementCount = length;
        modCount++;
    }
    @Override
    public synchronized int size() {
        return elementCount;
    }
    @Override
    public synchronized List<E> subList(int start, int end) {
        return new Collections.SynchronizedRandomAccessList<E>(super.subList(
                start, end), this);
    }
    @Override
    public synchronized Object[] toArray() {
        Object[] result = new Object[elementCount];
        System.arraycopy(elementData, 0, result, 0, elementCount);
        return result;
    }
    @Override
    @SuppressWarnings("unchecked")
    public synchronized <T> T[] toArray(T[] contents) {
        if (elementCount > contents.length) {
            Class<?> ct = contents.getClass().getComponentType();
            contents = (T[]) Array.newInstance(ct, elementCount);
        }
        System.arraycopy(elementData, 0, contents, 0, elementCount);
        if (elementCount < contents.length) {
            contents[elementCount] = null;
        }
        return contents;
    }
    @Override
    public synchronized String toString() {
        if (elementCount == 0) {
            return "[]"; 
        }
        int length = elementCount - 1;
        StringBuilder buffer = new StringBuilder(elementCount * 16);
        buffer.append('[');
        for (int i = 0; i < length; i++) {
            if (elementData[i] == this) {
                buffer.append("(this Collection)"); 
            } else {
                buffer.append(elementData[i]);
            }
            buffer.append(", "); 
        }
        if (elementData[length] == this) {
            buffer.append("(this Collection)"); 
        } else {
            buffer.append(elementData[length]);
        }
        buffer.append(']');
        return buffer.toString();
    }
    public synchronized void trimToSize() {
        if (elementData.length != elementCount) {
            grow(elementCount);
        }
    }
    private synchronized void writeObject(ObjectOutputStream stream)
            throws IOException {
        stream.defaultWriteObject();
    }
}
