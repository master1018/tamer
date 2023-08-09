public class CopyOnWriteArrayList<E> implements List<E>, RandomAccess, Cloneable, Serializable {
    private static final long serialVersionUID = 8673264195747942595L;
    private transient volatile E[] arr;
    private final transient ReentrantLock lock = new ReentrantLock();
    public CopyOnWriteArrayList() {
    }
    public CopyOnWriteArrayList(Collection<? extends E> c) {
        this((E[]) c.toArray());
    }
    public CopyOnWriteArrayList(E[] array) {
        int size = array.length;
        E[] data = newElementArray(size);
        for (int i = 0; i < size; i++) {
            data[i] = array[i];
        }
        arr = data;
    }
    public boolean add(E e) {
        lock.lock();
        try {
            E[] data;
            E[] old = getData();
            int size = old.length;
            data = newElementArray(size + 1);
            System.arraycopy(old, 0, data, 0, size);
            data[size] = e;
            setData(data);
            return true;
        } finally {
            lock.unlock();
        }
    }
    public void add(int index, E e) {
        lock.lock();
        try {
            E[] data;
            E[] old = getData();
            int size = old.length;
            checkIndexInclusive(index, size);
            data = newElementArray(size+1);
            System.arraycopy(old, 0, data, 0, index);
            data[index] = e;
            if (size > index) {
                System.arraycopy(old, index, data, index + 1, size - index);
            }
            setData(data);
        } finally {
            lock.unlock();
        }
    }
    public boolean addAll(Collection<? extends E> c) {
        Iterator it = c.iterator();
        int ssize = c.size();
        lock.lock();
        try {
            int size = size();
            E[] data;
            E[] old = getData();
            int nSize = size + ssize;
            data = newElementArray(nSize);
            System.arraycopy(old, 0, data, 0, size);
            while (it.hasNext()) {
                data[size++] = (E) it.next();
            }
            setData(data);
        } finally {
            lock.unlock();
        }
        return true;
    }
    public boolean addAll(int index, Collection<? extends E> c) {
        Iterator it = c.iterator();
        int ssize = c.size();
        lock.lock();
        try {
            int size = size();
            checkIndexInclusive(index, size);
            E[] data;
            E[] old = getData();
            int nSize = size + ssize;
            data = newElementArray(nSize);
            System.arraycopy(old, 0, data, 0, index);
            int i = index;
            while (it.hasNext()) {
                data[i++] = (E) it.next();
            }
            if (size > index) {
                System.arraycopy(old, index, data, index + ssize, size - index);
            }
            setData(data);
        } finally {
            lock.unlock();
        }
        return true;
    }
    public int addAllAbsent(Collection<? extends E> c) {
        if (c.size() == 0) {
            return 0;
        }
        lock.lock();
        try {
            E[] old = getData();
            int size = old.length;
            E[] toAdd = newElementArray(c.size());
            int i = 0;
            for (Iterator it = c.iterator(); it.hasNext();) {
                E o = (E) it.next();
                if (indexOf(o) < 0) {
                    toAdd[i++] = o;
                }
            }
            E[] data = newElementArray(size + i);
            System.arraycopy(old, 0, data, 0, size);
            System.arraycopy(toAdd, 0, data, size, i);
            setData(data);
            return i;
        } finally {
            lock.unlock();
        }
    }
    public boolean addIfAbsent(E e) {
        lock.lock();
        try {
            E[] data;
            E[] old = getData();
            int size = old.length;
            if (size != 0) {
                if (indexOf(e) >= 0) {
                    return false;
                }
            }
            data = newElementArray(size + 1);
            System.arraycopy(old, 0, data, 0, size);
            data[size] = e;
            setData(data);
            return true;
        } finally {
            lock.unlock();
        }
    }
    public void clear() {
        lock.lock();
        try {
            setData(newElementArray(0));
        } finally {
            lock.unlock();
        }
    }
    @Override
    public Object clone() {
        try {
            CopyOnWriteArrayList thisClone = (CopyOnWriteArrayList) super.clone();
            thisClone.setData(this.getData());
            return thisClone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("CloneNotSupportedException is not expected here");
        }
    }
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }
    public boolean containsAll(Collection<?> c) {
        E[] data = getData();
        return containsAll(c, data, 0, data.length);
    }
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof List)) {
            return false;
        }
        List l = (List) o;
        Iterator it = l.listIterator();
        Iterator ourIt = listIterator();
        while (it.hasNext()) {
            if (!ourIt.hasNext()) {
                return false;
            }
            Object thisListElem = it.next();
            Object anotherListElem = ourIt.next();
            if (!(thisListElem == null ? anotherListElem == null : thisListElem
                    .equals(anotherListElem))) {
                return false;
            }
        }
        if (ourIt.hasNext()) {
            return false;
        }
        return true;
    }
    public E get(int index) {
        E[] data = getData();
        return data[index];
    }
    public int hashCode() {
        int hashCode = 1;
        Iterator it = listIterator();
        while (it.hasNext()) {
            Object obj = it.next();
            hashCode = 31 * hashCode + (obj == null ? 0 : obj.hashCode());
        }
        return hashCode;
    }
    public int indexOf(E e, int index) {
        E[] data = getData();
        return indexOf(e, data, index, data.length - index);
    }
    public int indexOf(Object o) {
        E[] data = getData();
        return indexOf(o, data, 0, data.length);
    }
    public boolean isEmpty() {
        return size() == 0;
    }
    public Iterator<E> iterator() {
        return new ListIteratorImpl(getData(), 0);
    }
    public int lastIndexOf(E e, int index) {
        E[] data = getData();
        return lastIndexOf(e, data, 0, index);
    }
    public int lastIndexOf(Object o) {
        E[] data = getData();
        return lastIndexOf(o, data, 0, data.length);
    }
    public ListIterator<E> listIterator() {
        return new ListIteratorImpl(getData(), 0);
    }
    public ListIterator<E> listIterator(int index) {
        E[] data = getData();
        checkIndexInclusive(index, data.length);
        return new ListIteratorImpl(data, index);
    }
    public E remove(int index) {
        return removeRange(index, 1);
    }
    public boolean remove(Object o) {
        lock.lock();
        try {
            int index = indexOf(o);
            if (index == -1) {
                return false;
            }
            remove(index);
            return true;
        } finally {
            lock.unlock();
        }
    }
    public boolean removeAll(Collection<?> c) {
        lock.lock();
        try {
            return removeAll(c, 0, getData().length) != 0;
        } finally {
            lock.unlock();
        }
    }
    public boolean retainAll(Collection<?> c) {
        if (c == null) {
            throw new NullPointerException();
        }
        lock.lock();
        try {
            return retainAll(c, 0, getData().length) != 0;
        } finally {
            lock.unlock();
        }
    }
    public E set(int index, E e) {
        lock.lock();
        try {
            int size = size();
            checkIndexExlusive(index, size);
            E[] data;
            data = newElementArray(size);
            E[] oldArr = getData();
            System.arraycopy(oldArr, 0, data, 0, size);
            E old = data[index];
            data[index] = e;
            setData(data);
            return old;
        } finally {
            lock.unlock();
        }
    }
    public int size() {
        return getData().length;
    }
    public List<E> subList(int fromIndex, int toIndex) {
        return new SubList(this, fromIndex, toIndex);
    }
    public Object[] toArray() {
        E[] data = getData();
        return toArray(data, 0, data.length);
    }
    public <T> T[] toArray(T[] a) {
        E[] data = getData();
        return (T[]) toArray(a, data, 0, data.length);
    }
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Iterator it = listIterator();
        while (it.hasNext()) {
            sb.append(String.valueOf(it.next()));
            sb.append(", ");
        }
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 2);
        }
        sb.append("]");
        return sb.toString();
    }
    @SuppressWarnings("unchecked")
    private final E[] newElementArray(int size) {
        return (E[])new Object[size];
    }
    private final void setData(E[] data) {
        arr = data;
    }
    final E[] getData() {
        if (arr == null) {
            return newElementArray(0);
        }
        return arr;
    }
    final int removeAll(Collection c, int start, int size) {
        int ssize = c.size();
        if (ssize == 0) {
            return 0;
        }
        Object[] old = getData();
        int arrsize = old.length;
        if (arrsize == 0) {
            return 0;
        }
        Object[] data = new Object[size];
        int j = 0;
        for (int i = start; i < (start + size); i++) {
            if (!c.contains(old[i])) {
                data[j++] = old[i];
            }
        }
        if (j != size) {
            E[] result = newElementArray(arrsize - (size - j));
            System.arraycopy(old, 0, result, 0, start);
            System.arraycopy(data, 0, result, start, j);
            System.arraycopy(old, start + size, result, start + j, arrsize
                    - (start + size));
            setData(result);
            return (size - j);
        }
        return 0;
    }
    int retainAll(Collection c, int start, int size) {
        Object[] old = getData();
        if (size == 0) {
            return 0;
        }
        if (c.size() == 0) {
            E[] data;
            if (size == old.length) {
                data = newElementArray(0);
            } else {
                data = newElementArray(old.length - size);
                System.arraycopy(old, 0, data, 0, start);
                System.arraycopy(old, start + size, data, start, old.length
                        - start - size);
            }
            setData(data);
            return size;
        }
        Object[] temp = new Object[size];
        int pos = 0;
        for (int i = start; i < (start + size); i++) {
            if (c.contains(old[i])) {
                temp[pos++] = old[i];
            }
        }
        if (pos == size) {
            return 0;
        }
        E[] data = newElementArray(pos + old.length - size);
        System.arraycopy(old, 0, data, 0, start);
        System.arraycopy(temp, 0, data, start, pos);
        System.arraycopy(old, start + size, data, start + pos, old.length
                - start - size);
        setData(data);
        return (size - pos);
    }
    E removeRange(int start, int size) {
        lock.lock();
        try {
            int sizeArr = size();
            checkIndexExlusive(start, sizeArr);
            checkIndexInclusive(start + size, sizeArr);
            E[] data;
            data = newElementArray(sizeArr - size);
            E[] oldArr = getData();
            System.arraycopy(oldArr, 0, data, 0, start);
            E old = oldArr[start];
            if (sizeArr > (start + size)) {
                System.arraycopy(oldArr, start + size, data, start, sizeArr
                        - (start + size));
            }
            setData(data);
            return old;
        } finally {
            lock.unlock();
        }
    }
    static Object[] toArray(Object[] data, int start, int size) {
        Object[] result = new Object[size];
        System.arraycopy(data, start, result, 0, size);
        return result;
    }
    static Object[] toArray(Object[] to, Object[] data, int start, int size) {
        int l = data.length;
        if (to.length < l) {
            to = (Object[]) Array.newInstance(to.getClass().getComponentType(),
                    l);
        } else {
            if (to.length > l) {
                to[l] = null;
            }
        }
        System.arraycopy(data, start, to, 0, size);
        return to;
    }
    static final boolean containsAll(Collection c, Object[] data, int start,
                                     int size) {
        if (size == 0) {
            return false;
        }
        Iterator it = c.iterator();
        while (it.hasNext()) {
            Object next = it.next();
            if (indexOf(next, data, start, size) < 0) {
                return false;
            }
        }
        return true;
    }
    static final int lastIndexOf(Object o, Object[] data, int start, int size) {
        if (size == 0) {
            return -1;
        }
        if (o != null) {
            for (int i = start + size - 1; i > start - 1; i--) {
                if (o.equals(data[i])) {
                    return i;
                }
            }
        } else {
            for (int i = start + size - 1; i > start - 1; i--) {
                if (data[i] == null) {
                    return i;
                }
            }
        }
        return -1;
    }
    static final int indexOf(Object o, Object[] data, int start, int size) {
        if (size == 0) {
            return -1;
        }
        if (o == null) {
            for (int i = start; i < start + size; i++) {
                if (data[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = start; i < start + size; i++) {
                if (o.equals(data[i])) {
                    return i;
                }
            }
        }
        return -1;
    }
    static final void checkIndexInclusive(int index, int size) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index is " + index + ", size is " + size);
        }
    }
    static final void checkIndexExlusive(int index, int size) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index is " + index + ", size is " + size);
        }
    }
    final E[] getArray() {
        return arr;
    }
    private static class ListIteratorImpl implements ListIterator {
        private final Object[] arr;
        private int current;
        private final int size;
        final int size() {
            return size;
        }
        public ListIteratorImpl(Object[] data, int current) {
            this.current = current;
            arr = data;
            size = data.length;
        }
        public void add(Object o) {
            throw new UnsupportedOperationException("Unsupported operation add");
        }
        public boolean hasNext() {
            if (current < size) {
                return true;
            }
            return false;
        }
        public boolean hasPrevious() {
            return current > 0;
        }
        public Object next() {
            if (hasNext()) {
                return arr[current++];
            }
            throw new NoSuchElementException("pos is " + current + ", size is " + size);
        }
        public int nextIndex() {
            return current;
        }
        public Object previous() {
            if (hasPrevious()) {
                return arr[--current];
            }
            throw new NoSuchElementException("pos is " + (current-1) + ", size is " + size);
        }
        public int previousIndex() {
            return current - 1;
        }
        public void remove() {
            throw new UnsupportedOperationException("Unsupported operation remove");
        }
        public void set(Object o) {
            throw new UnsupportedOperationException("Unsupported operation set");
        }
    }
    static final class SubListReadData {
        final int size;
        final Object[] data;
        SubListReadData(int size, Object[] data) {
            this.size = size;
            this.data = data;
        }
    }
    static class SubList implements List {
        private final CopyOnWriteArrayList list;
        private volatile SubListReadData read;
        private final int start;
        public SubList(CopyOnWriteArrayList list, int fromIdx, int toIdx) {
            this.list = list;
            Object[] data = list.getData();
            int size = toIdx - fromIdx;
            checkIndexExlusive(fromIdx, data.length);
            checkIndexInclusive(toIdx, data.length);
            read = new SubListReadData(size, list.getData());
            start = fromIdx;
        }
        private void checkModifications() {
            if (read.data != list.getData()) {
                throw new ConcurrentModificationException();
            }
        }
        public ListIterator listIterator(int startIdx) {
            return new SubListIterator(startIdx, read);
        }
        public Object set(int index, Object obj) {
            list.lock.lock();
            try {
                checkIndexExlusive(index, read.size);
                checkModifications();
                Object result = list.set(index + start, obj);
                read = new SubListReadData(read.size, list.getData());
                return result;
            } finally {
                list.lock.unlock();
            }
        }
        public Object get(int index) {
            SubListReadData data = read;
            if (data.data != list.getData()) {
                list.lock.lock();
                try {
                    data = read;
                    if (data.data != list.getData()) {
                        throw new ConcurrentModificationException();
                    }
                } finally {
                    list.lock.unlock();
                }
            }
            checkIndexExlusive(index, data.size);
            return data.data[index + start];
        }
        public int size() {
            return read.size;
        }
        public Object remove(int index) {
            list.lock.lock();
            try {
                checkIndexExlusive(index, read.size);
                checkModifications();
                Object obj = list.remove(index + start);
                read = new SubListReadData(read.size - 1, list.getData());
                return obj;
            } finally {
                list.lock.unlock();
            }
        }
        public void add(int index, Object object) {
            list.lock.lock();
            try {
                checkIndexInclusive(index, read.size);
                checkModifications();
                list.add(index + start, object);
                read = new SubListReadData(read.size + 1, list.getData());
            } finally {
                list.lock.unlock();
            }
        }
        public boolean add(Object o) {
            list.lock.lock();
            try {
                checkModifications();
                list.add(start + read.size, o);
                read = new SubListReadData(read.size + 1, list.getData());
                return true;
            } finally {
                list.lock.unlock();
            }
        }
        public boolean addAll(Collection c) {
            list.lock.lock();
            try {
                checkModifications();
                int d = list.size();
                list.addAll(start + read.size, c);
                read = new SubListReadData(read.size + (list.size() - d), list
                        .getData());
                return true;
            } finally {
                list.lock.unlock();
            }
        }
        public void clear() {
            list.lock.lock();
            try {
                checkModifications();
                list.removeRange(start, read.size);
                read = new SubListReadData(0, list.getData());
            } finally {
                list.lock.unlock();
            }
        }
        public boolean contains(Object o) {
            return indexOf(o) != -1;
        }
        public boolean containsAll(Collection c) {
            SubListReadData b = read;
            return CopyOnWriteArrayList.containsAll(c, b.data, start, b.size);
        }
        public int indexOf(Object o) {
            SubListReadData b = read;
            int ind = CopyOnWriteArrayList.indexOf(o, b.data, start, b.size)
                    - start;
            return ind < 0 ? -1 : ind;
        }
        public boolean isEmpty() {
            return read.size == 0;
        }
        public Iterator iterator() {
            return new SubListIterator(0, read);
        }
        public int lastIndexOf(Object o) {
            SubListReadData b = read;
            int ind = CopyOnWriteArrayList
                    .lastIndexOf(o, b.data, start, b.size)
                    - start;
            return ind < 0 ? -1 : ind;
        }
        public ListIterator listIterator() {
            return new SubListIterator(0, read);
        }
        public boolean remove(Object o) {
            list.lock.lock();
            try {
                checkModifications();
                int i = indexOf(o);
                if (i == -1) {
                    return false;
                }
                boolean result = list.remove(i + start) != null;
                if (result) {
                    read = new SubListReadData(read.size - 1, list.getData());
                }
                return result;
            } finally {
                list.lock.unlock();
            }
        }
        public boolean removeAll(Collection c) {
            list.lock.lock();
            try {
                checkModifications();
                int removed = list.removeAll(c, start, read.size);
                if (removed > 0) {
                    read = new SubListReadData(read.size - removed, list
                            .getData());
                    return true;
                }
            } finally {
                list.lock.unlock();
            }
            return false;
        }
        public boolean retainAll(Collection c) {
            list.lock.lock();
            try {
                checkModifications();
                int removed = list.retainAll(c, start, read.size);
                if (removed > 0) {
                    read = new SubListReadData(read.size - removed, list
                            .getData());
                    return true;
                }
                return false;
            } finally {
                list.lock.unlock();
            }
        }
        public List subList(int fromIndex, int toIndex) {
            return new SubList(list, start + fromIndex, start + toIndex);
        }
        public Object[] toArray() {
            SubListReadData r = read;
            return CopyOnWriteArrayList.toArray(r.data, start, r.size);
        }
        public Object[] toArray(Object[] a) {
            SubListReadData r = read;
            return CopyOnWriteArrayList.toArray(a, r.data, start, r.size);
        }
        public boolean addAll(int index, Collection collection) {
            list.lock.lock();
            try {
                checkIndexInclusive(index, read.size);
                checkModifications();
                int d = list.size();
                boolean rt = list.addAll(index + start, collection);
                read = new SubListReadData(read.size + list.size() - d, list
                        .getData());
                return rt;
            } finally {
                list.lock.unlock();
            }
        }
        private class SubListIterator extends ListIteratorImpl {
            private final SubListReadData dataR;
            private SubListIterator(int index, SubListReadData d) {
                super(d.data, index + start);
                this.dataR = d;
            }
            public int nextIndex() {
                return super.nextIndex() - start;
            }
            public int previousIndex() {
                return super.previousIndex() - start;
            }
            public boolean hasNext() {
                return nextIndex() < dataR.size;
            }
            public boolean hasPrevious() {
                return previousIndex() > -1;
            }
        }
    }
    private void writeObject(ObjectOutputStream oos) throws IOException {
        E[] back = getData();
        int size = back.length;
        oos.defaultWriteObject();
        oos.writeInt(size);
        for (int i = 0; i < size; i++) {
            oos.writeObject(back[i]);
        }
    }
    private void readObject(ObjectInputStream ois) throws IOException,
            ClassNotFoundException {
        ois.defaultReadObject();
        int length = ois.readInt();
        if (length == 0) {
            setData(newElementArray(0));
        } else {
            E[] back = newElementArray(length);
            for (int i = 0; i < back.length; i++) {
                back[i] = (E) ois.readObject();
            }
            setData(back);
        }
    }
}
