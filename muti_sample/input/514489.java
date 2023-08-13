public class ArrayList<E> extends AbstractList<E>
        implements Cloneable, Serializable, RandomAccess {
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    private static final int MIN_CAPACITY_INCREMENT = 12;
    int size;
    transient Object[] array;
    public ArrayList(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException();
        }
        array = (capacity == 0 ? EMPTY_OBJECT_ARRAY : new Object[capacity]);
    }
    public ArrayList() {
        array = EMPTY_OBJECT_ARRAY;
    }
    public ArrayList(Collection<? extends E> collection) {
        Object[] a = collection.toArray();
        if (a.getClass() != Object[].class) {
            Object[] newArray = new Object[a.length];
            System.arraycopy(a, 0, newArray, 0, a.length);
            a = newArray;
        }
        array = a;
        size = a.length;
    }
    @Override public boolean add(E object) {
        Object[] a = array;
        int s = size;
        if (s == a.length) {
            Object[] newArray = new Object[s +
                    (s < (MIN_CAPACITY_INCREMENT / 2) ?
                     MIN_CAPACITY_INCREMENT : s >> 1)];
            System.arraycopy(a, 0, newArray, 0, s);
            array = a = newArray;
        }
        a[s] = object;
        size = s + 1;
        modCount++;
        return true;
    }
    @Override public void add(int index, E object) {
        Object[] a = array;
        int s = size;
        if (index > s) {
            throwIndexOutOfBoundsException(index, s);
        }
        if (s < a.length) {
            System.arraycopy(a, index, a, index + 1, s - index);
        } else {
            Object[] newArray = new Object[newCapacity(s)];
            System.arraycopy(a, 0, newArray, 0, index);
            System.arraycopy(a, index, newArray, index + 1, s - index);
            array = a = newArray;
        }
        a[index] = object;
        size = s + 1;
        modCount++;
    }
    private static int newCapacity(int currentCapacity) {
        int increment = (currentCapacity < (MIN_CAPACITY_INCREMENT / 2) ?
                MIN_CAPACITY_INCREMENT : currentCapacity >> 1);
        return currentCapacity + increment;
    }
    @Override public boolean addAll(Collection<? extends E> collection) {
        Object[] newPart = collection.toArray();
        int newPartSize = newPart.length;
        if (newPartSize == 0) {
            return false;
        }
        Object[] a = array;
        int s = size;
        int newSize = s + newPartSize; 
        if (newSize > a.length) {
            int newCapacity = newCapacity(newSize - 1);  
            Object[] newArray = new Object[newCapacity];
            System.arraycopy(a, 0, newArray, 0, s);
            array = a = newArray;
        }
        System.arraycopy(newPart, 0, a, s, newPartSize);
        size = newSize;
        modCount++;
        return true;
    }
    @Override
    public boolean addAll(int index, Collection<? extends E> collection) {
        Object[] newPart = collection.toArray();
        int newPartSize = newPart.length;
        if (newPartSize == 0) {
            return false;
        }
        Object[] a = array;
        int s = size;
        if (index > s) {
            throwIndexOutOfBoundsException(index, s);
        }
        int newSize = s + newPartSize; 
        if (newSize <= a.length) {
             System.arraycopy(a, index, a, index + newPartSize, s - index);
        } else {
            int newCapacity = newCapacity(newSize - 1);  
            Object[] newArray = new Object[newCapacity];
            System.arraycopy(a, 0, newArray, 0, index);
            System.arraycopy(a, index, newArray, index + newPartSize, s-index);
            array = a = newArray;
        }
        System.arraycopy(newPart, 0, a, index, newPartSize);
        size = newSize;
        modCount++;
        return true;
    }
    private static void throwIndexOutOfBoundsException(int index, int size) {
        throw new IndexOutOfBoundsException("Invalid index " + index
                + ", size is " + size);
    }
    @Override public void clear() {
        if (size != 0) {
            Arrays.fill(array, 0, size, null);
            size = 0;
            modCount++;
        }
    }
    @Override public Object clone() {
        try {
            ArrayList<?> result = (ArrayList<?>) super.clone();
            result.array = array.clone();
            return result;
        } catch (CloneNotSupportedException e) {
           throw new AssertionError();
        }
    }
    public void ensureCapacity(int minimumCapacity) {
        Object[] a = array;
        if (a.length < minimumCapacity) {
            Object[] newArray = new Object[minimumCapacity];
            System.arraycopy(a, 0, newArray, 0, size);
            array = newArray;
            modCount++;
        }
    }
    @SuppressWarnings("unchecked") @Override public E get(int index) {
        if (index >= size) {
            throwIndexOutOfBoundsException(index, size);
        }
        return (E) array[index];
    }
    @Override public int size() {
        return size;
    }
    @Override public boolean isEmpty() {
        return size == 0;
    }
    @Override public boolean contains(Object object) {
        Object[] a = array;
        int s = size;
        if (object != null) {
            for (int i = 0; i < s; i++) {
                if (object.equals(a[i])) {
                    return true;
                }
            }
        } else {
            for (int i = 0; i < s; i++) {
                if (a[i] == null) {
                    return true;
                }
            }
        }
        return false;
    }
    @Override public int indexOf(Object object) {
        Object[] a = array;
        int s = size;
        if (object != null) {
            for (int i = 0; i < s; i++) {
                if (object.equals(a[i])) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < s; i++) {
                if (a[i] == null) {
                    return i;
                }
            }
        }
        return -1;
    }
    @Override public int lastIndexOf(Object object) {
        Object[] a = array;
        if (object != null) {
            for (int i = size - 1; i >= 0; i--) {
                if (object.equals(a[i])) {
                    return i;
                }
            }
        } else {
            for (int i = size - 1; i >= 0; i--) {
                if (a[i] == null) {
                    return i;
                }
            }
        }
        return -1;
    }
    @Override public E remove(int index) {
        Object[] a = array;
        int s = size;
        if (index >= s) {
            throwIndexOutOfBoundsException(index, s);
        }
        @SuppressWarnings("unchecked") E result = (E) a[index];
        System.arraycopy(a, index + 1, a, index, --s - index);
        a[s] = null;  
        size = s;
        modCount++;
        return result;
    }
    @Override public boolean remove(Object object) {
        Object[] a = array;
        int s = size;
        if (object != null) {
            for (int i = 0; i < s; i++) {
                if (object.equals(a[i])) {
                    System.arraycopy(a, i + 1, a, i, --s - i);
                    a[s] = null;  
                    size = s;
                    modCount++;
                    return true;
                }
            }
        } else {
            for (int i = 0; i < s; i++) {
                if (a[i] == null) {
                    System.arraycopy(a, i + 1, a, i, --s - i);
                    a[s] = null;  
                    size = s;
                    modCount++;
                    return true;
                }
            }
        }
        return false;
    }
    @Override protected void removeRange(int fromIndex, int toIndex) {
        Object[] a = array;
        int s = size;
        if (fromIndex >= s) {
            throw new IndexOutOfBoundsException("fromIndex " + fromIndex
                    + " >= size " + size);
        }
        if (toIndex > s) {
            throw new IndexOutOfBoundsException("toIndex " + toIndex
                    + " > size " + size);
        }
        if (fromIndex > toIndex) {
            throw new IndexOutOfBoundsException("fromIndex " + fromIndex
                    + " > toIndex " + toIndex);
        }
        System.arraycopy(a, toIndex, a, fromIndex, s - toIndex);
        int rangeSize = toIndex - fromIndex;
        Arrays.fill(a, s - rangeSize, s, null);
        size = s - rangeSize;
        modCount++;
    }
    @Override public E set(int index, E object) {
        Object[] a = array;
        if (index >= size) {
            throwIndexOutOfBoundsException(index, size);
        }
        @SuppressWarnings("unchecked") E result = (E) a[index];
        a[index] = object;
        return result;
    }
    @Override public Object[] toArray() {
        int s = size;
        Object[] result = new Object[s];
        System.arraycopy(array, 0, result, 0, s);
        return result;
    }
    @Override public <T> T[] toArray(T[] contents) {
        int s = size;
        if (contents.length < s) {
            @SuppressWarnings("unchecked") T[] newArray
                = (T[]) Array.newInstance(contents.getClass().getComponentType(), s);
            contents = newArray;
        }
        System.arraycopy(this.array, 0, contents, 0, s);
        if (contents.length > s) {
            contents[s] = null;
        }
        return contents;
    }
    public void trimToSize() {
        int s = size;
        if (s == array.length) {
            return;
        }
        if (s == 0) {
            array = EMPTY_OBJECT_ARRAY;
        } else {
            Object[] newArray = new Object[s];
            System.arraycopy(array, 0, newArray, 0, s);
            array = newArray;
        }
        modCount++;
    }
    @Override public Iterator<E> iterator() {
        return new ArrayListIterator();
    }
    private class ArrayListIterator implements Iterator<E> {
        private int remaining = size;
        private int removalIndex = -1;
        private int expectedModCount = modCount;
        public boolean hasNext() {
            return remaining != 0;
        }
        @SuppressWarnings("unchecked") public E next() {
            ArrayList<E> ourList = ArrayList.this;
            int rem = remaining;
            if (ourList.modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            if (rem == 0) {
                throw new NoSuchElementException();
            }
            remaining = rem - 1;
            return (E) ourList.array[removalIndex = ourList.size - rem];
        }
        public void remove() {
            Object[] a = array;
            int removalIdx = removalIndex;
            if (modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
            if (removalIdx < 0) {
                throw new IllegalStateException();
            }
            System.arraycopy(a, removalIdx + 1, a, removalIdx, remaining);
            a[--size] = null;  
            removalIndex = -1;
            expectedModCount = ++modCount;
        }
    }
    @Override public int hashCode() {
        Object[] a = array;
        int hashCode = 1;
        for (int i = 0, s = size; i < s; i++) {
            Object e = a[i];
            hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
        }
        return hashCode;
    }
    @Override public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof List)) {
            return false;
        }
        List<?> that = (List<?>) o;
        int s = size;
        if (that.size() != s) {
            return false;
        }
        Object[] a = array;
        if (that instanceof RandomAccess) {
            for (int i = 0; i < s; i++) {
                Object eThis = a[i];
                Object ethat = that.get(i);
                if (eThis == null ? ethat != null : !eThis.equals(ethat)) {
                    return false;
                }
            }
        } else {  
            Iterator<?> it = that.iterator();
            for (int i = 0; i < s; i++) {
                Object eThis = a[i];
                Object eThat = it.next();
                if (eThis == null ? eThat != null : !eThis.equals(eThat)) {
                    return false;
                }
            }
        }
        return true;
    }
    private static final long serialVersionUID = 8683452581122892189L;
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeInt(array.length);
        for (int i = 0; i < size; i++) {
            stream.writeObject(array[i]);
        }
    }
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
        int cap = stream.readInt();
        if (cap < size) {
            throw new InvalidObjectException(
                    "Capacity: " + cap + " < size: " + size);
        }
        array = (cap == 0 ? EMPTY_OBJECT_ARRAY : new Object[cap]);
        for (int i = 0; i < size; i++) {
            array[i] = stream.readObject();
        }
    }
 }
