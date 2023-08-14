public class Collections {
    private static final class CopiesList<E> extends AbstractList<E> implements
            Serializable {
        private static final long serialVersionUID = 2739099268398711800L;
        private final int n;
        private final E element;
        CopiesList(int length, E object) {
            if (length < 0) {
                throw new IllegalArgumentException();
            }
            n = length;
            element = object;
        }
        @Override
        public boolean contains(Object object) {
            return element == null ? object == null : element.equals(object);
        }
        @Override
        public int size() {
            return n;
        }
        @Override
        public E get(int location) {
            if (0 <= location && location < n) {
                return element;
            }
            throw new IndexOutOfBoundsException();
        }
    }
    @SuppressWarnings("unchecked")
    private static final class EmptyList extends AbstractList implements
            RandomAccess, Serializable {
        private static final long serialVersionUID = 8842843931221139166L;
        @Override
        public boolean contains(Object object) {
            return false;
        }
        @Override
        public int size() {
            return 0;
        }
        @Override
        public Object get(int location) {
            throw new IndexOutOfBoundsException();
        }
        private Object readResolve() {
            return Collections.EMPTY_LIST;
        }
    }
    @SuppressWarnings("unchecked")
    private static final class EmptySet extends AbstractSet implements
            Serializable {
        private static final long serialVersionUID = 1582296315990362920L;
        @Override
        public boolean contains(Object object) {
            return false;
        }
        @Override
        public int size() {
            return 0;
        }
        @Override
        public Iterator iterator() {
            return new Iterator() {
                public boolean hasNext() {
                    return false;
                }
                public Object next() {
                    throw new NoSuchElementException();
                }
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
        private Object readResolve() {
            return Collections.EMPTY_SET;
        }
    }
    @SuppressWarnings("unchecked")
    private static final class EmptyMap extends AbstractMap implements
            Serializable {
        private static final long serialVersionUID = 6428348081105594320L;
        @Override
        public boolean containsKey(Object key) {
            return false;
        }
        @Override
        public boolean containsValue(Object value) {
            return false;
        }
        @Override
        public Set entrySet() {
            return EMPTY_SET;
        }
        @Override
        public Object get(Object key) {
            return null;
        }
        @Override
        public Set keySet() {
            return EMPTY_SET;
        }
        @Override
        public Collection values() {
            return EMPTY_LIST;
        }
        private Object readResolve() {
            return Collections.EMPTY_MAP;
        }
    }
    @SuppressWarnings("unchecked")
    public static final List EMPTY_LIST = new EmptyList();
    @SuppressWarnings("unchecked")
    public static final Set EMPTY_SET = new EmptySet();
    @SuppressWarnings("unchecked")
    public static final Map EMPTY_MAP = new EmptyMap();
    private static final class ReverseComparator<T> implements Comparator<T>,
            Serializable {
        private static final ReverseComparator<Object> INSTANCE
                = new ReverseComparator<Object>();
        private static final long serialVersionUID = 7207038068494060240L;
        @SuppressWarnings("unchecked")
        public int compare(T o1, T o2) {
            Comparable<T> c2 = (Comparable<T>) o2;
            return c2.compareTo(o1);
        }
        private Object readResolve() throws ObjectStreamException {
            return INSTANCE;
        }
    }
    private static final class ReverseComparatorWithComparator<T> implements
            Comparator<T>, Serializable {
        private static final long serialVersionUID = 4374092139857L;
        private final Comparator<T> comparator;
        ReverseComparatorWithComparator(Comparator<T> comparator) {
            super();
            this.comparator = comparator;
        }
        public int compare(T o1, T o2) {
            return comparator.compare(o2, o1);
        }
        @Override
        public boolean equals(Object o) {
            return o instanceof ReverseComparatorWithComparator
                    && ((ReverseComparatorWithComparator) o).comparator
                            .equals(comparator);
        }
        @Override
        public int hashCode() {
            return ~comparator.hashCode();
        }
    }
    private static final class SingletonSet<E> extends AbstractSet<E> implements
            Serializable {
        private static final long serialVersionUID = 3193687207550431679L;
        final E element;
        SingletonSet(E object) {
            element = object;
        }
        @Override
        public boolean contains(Object object) {
            return element == null ? object == null : element.equals(object);
        }
        @Override
        public int size() {
            return 1;
        }
        @Override
        public Iterator<E> iterator() {
            return new Iterator<E>() {
                boolean hasNext = true;
                public boolean hasNext() {
                    return hasNext;
                }
                public E next() {
                    if (hasNext) {
                        hasNext = false;
                        return element;
                    }
                    throw new NoSuchElementException();
                }
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
    private static final class SingletonList<E> extends AbstractList<E>
            implements Serializable {
        private static final long serialVersionUID = 3093736618740652951L;
        final E element;
        SingletonList(E object) {
            element = object;
        }
        @Override
        public boolean contains(Object object) {
            return element == null ? object == null : element.equals(object);
        }
        @Override
        public E get(int location) {
            if (location == 0) {
                return element;
            }
            throw new IndexOutOfBoundsException();
        }
        @Override
        public int size() {
            return 1;
        }
    }
    private static final class SingletonMap<K, V> extends AbstractMap<K, V>
            implements Serializable {
        private static final long serialVersionUID = -6979724477215052911L;
        final K k;
        final V v;
        SingletonMap(K key, V value) {
            k = key;
            v = value;
        }
        @Override
        public boolean containsKey(Object key) {
            return k == null ? key == null : k.equals(key);
        }
        @Override
        public boolean containsValue(Object value) {
            return v == null ? value == null : v.equals(value);
        }
        @Override
        public V get(Object key) {
            if (containsKey(key)) {
                return v;
            }
            return null;
        }
        @Override
        public int size() {
            return 1;
        }
        @Override
        public Set<Map.Entry<K, V>> entrySet() {
            return new AbstractSet<Map.Entry<K, V>>() {
                @Override
                public boolean contains(Object object) {
                    if (object instanceof Map.Entry) {
                        Map.Entry<?, ?> entry = (Map.Entry<?, ?>) object;
                        return containsKey(entry.getKey())
                                && containsValue(entry.getValue());
                    }
                    return false;
                }
                @Override
                public int size() {
                    return 1;
                }
                @Override
                public Iterator<Map.Entry<K, V>> iterator() {
                    return new Iterator<Map.Entry<K, V>>() {
                        boolean hasNext = true;
                        public boolean hasNext() {
                            return hasNext;
                        }
                        public Map.Entry<K, V> next() {
                            if (!hasNext) {
                                throw new NoSuchElementException();
                            }
                            hasNext = false;
                            return new MapEntry<K, V>(k, v) {
                                @Override
                                public V setValue(V value) {
                                    throw new UnsupportedOperationException();
                                }
                            };
                        }
                        public void remove() {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
            };
        }
    }
    static class SynchronizedCollection<E> implements Collection<E>,
            Serializable {
        private static final long serialVersionUID = 3053995032091335093L;
        final Collection<E> c;
        final Object mutex;
        SynchronizedCollection(Collection<E> collection) {
            c = collection;
            mutex = this;
        }
        SynchronizedCollection(Collection<E> collection, Object mutex) {
            c = collection;
            this.mutex = mutex;
        }
        public boolean add(E object) {
            synchronized (mutex) {
                return c.add(object);
            }
        }
        public boolean addAll(Collection<? extends E> collection) {
            synchronized (mutex) {
                return c.addAll(collection);
            }
        }
        public void clear() {
            synchronized (mutex) {
                c.clear();
            }
        }
        public boolean contains(Object object) {
            synchronized (mutex) {
                return c.contains(object);
            }
        }
        public boolean containsAll(Collection<?> collection) {
            synchronized (mutex) {
                return c.containsAll(collection);
            }
        }
        public boolean isEmpty() {
            synchronized (mutex) {
                return c.isEmpty();
            }
        }
        public Iterator<E> iterator() {
            synchronized (mutex) {
                return c.iterator();
            }
        }
        public boolean remove(Object object) {
            synchronized (mutex) {
                return c.remove(object);
            }
        }
        public boolean removeAll(Collection<?> collection) {
            synchronized (mutex) {
                return c.removeAll(collection);
            }
        }
        public boolean retainAll(Collection<?> collection) {
            synchronized (mutex) {
                return c.retainAll(collection);
            }
        }
        public int size() {
            synchronized (mutex) {
                return c.size();
            }
        }
        public java.lang.Object[] toArray() {
            synchronized (mutex) {
                return c.toArray();
            }
        }
        @Override
        public String toString() {
            synchronized (mutex) {
                return c.toString();
            }
        }
        public <T> T[] toArray(T[] array) {
            synchronized (mutex) {
                return c.toArray(array);
            }
        }
        private void writeObject(ObjectOutputStream stream) throws IOException {
            synchronized (mutex) {
                stream.defaultWriteObject();
            }
        }
    }
    static class SynchronizedRandomAccessList<E> extends SynchronizedList<E>
            implements RandomAccess {
        private static final long serialVersionUID = 1530674583602358482L;
        SynchronizedRandomAccessList(List<E> l) {
            super(l);
        }
        SynchronizedRandomAccessList(List<E> l, Object mutex) {
            super(l, mutex);
        }
        @Override
        public List<E> subList(int start, int end) {
            synchronized (mutex) {
                return new SynchronizedRandomAccessList<E>(list.subList(start,
                        end), mutex);
            }
        }
        private Object writeReplace() {
            return new SynchronizedList<E>(list);
        }
    }
    static class SynchronizedList<E> extends SynchronizedCollection<E>
            implements List<E> {
        private static final long serialVersionUID = -7754090372962971524L;
        final List<E> list;
        SynchronizedList(List<E> l) {
            super(l);
            list = l;
        }
        SynchronizedList(List<E> l, Object mutex) {
            super(l, mutex);
            list = l;
        }
        public void add(int location, E object) {
            synchronized (mutex) {
                list.add(location, object);
            }
        }
        public boolean addAll(int location, Collection<? extends E> collection) {
            synchronized (mutex) {
                return list.addAll(location, collection);
            }
        }
        @Override
        public boolean equals(Object object) {
            synchronized (mutex) {
                return list.equals(object);
            }
        }
        public E get(int location) {
            synchronized (mutex) {
                return list.get(location);
            }
        }
        @Override
        public int hashCode() {
            synchronized (mutex) {
                return list.hashCode();
            }
        }
        public int indexOf(Object object) {
            final int size;
            final Object[] array;
            synchronized (mutex) {
                size = list.size();
                array = new Object[size];
                list.toArray(array);
            }
            if (null != object)
                for (int i = 0; i < size; i++) {
                    if (object.equals(array[i])) {
                        return i;
                    }
                }
            else {
                for (int i = 0; i < size; i++) {
                    if (null == array[i]) {
                        return i;
                    }
                }
            }
            return -1;
        }
        public int lastIndexOf(Object object) {
            final int size;
            final Object[] array;
            synchronized (mutex) {
                size = list.size();
                array = new Object[size];
                list.toArray(array);
            }
            if (null != object)
                for (int i = size - 1; i >= 0; i--) {
                    if (object.equals(array[i])) {
                        return i;
                    }
                }
            else {
                for (int i = size - 1; i >= 0; i--) {
                    if (null == array[i]) {
                        return i;
                    }
                }
            }
            return -1;
        }
        public ListIterator<E> listIterator() {
            synchronized (mutex) {
                return list.listIterator();
            }
        }
        public ListIterator<E> listIterator(int location) {
            synchronized (mutex) {
                return list.listIterator(location);
            }
        }
        public E remove(int location) {
            synchronized (mutex) {
                return list.remove(location);
            }
        }
        public E set(int location, E object) {
            synchronized (mutex) {
                return list.set(location, object);
            }
        }
        public List<E> subList(int start, int end) {
            synchronized (mutex) {
                return new SynchronizedList<E>(list.subList(start, end), mutex);
            }
        }
        private void writeObject(ObjectOutputStream stream) throws IOException {
            synchronized (mutex) {
                stream.defaultWriteObject();
            }
        }
        private Object readResolve() {
            if (list instanceof RandomAccess) {
                return new SynchronizedRandomAccessList<E>(list, mutex);
            }
            return this;
        }
    }
    static class SynchronizedMap<K, V> implements Map<K, V>, Serializable {
        private static final long serialVersionUID = 1978198479659022715L;
        private final Map<K, V> m;
        final Object mutex;
        SynchronizedMap(Map<K, V> map) {
            m = map;
            mutex = this;
        }
        SynchronizedMap(Map<K, V> map, Object mutex) {
            m = map;
            this.mutex = mutex;
        }
        public void clear() {
            synchronized (mutex) {
                m.clear();
            }
        }
        public boolean containsKey(Object key) {
            synchronized (mutex) {
                return m.containsKey(key);
            }
        }
        public boolean containsValue(Object value) {
            synchronized (mutex) {
                return m.containsValue(value);
            }
        }
        public Set<Map.Entry<K, V>> entrySet() {
            synchronized (mutex) {
                return new SynchronizedSet<Map.Entry<K, V>>(m.entrySet(), mutex);
            }
        }
        @Override
        public boolean equals(Object object) {
            synchronized (mutex) {
                return m.equals(object);
            }
        }
        public V get(Object key) {
            synchronized (mutex) {
                return m.get(key);
            }
        }
        @Override
        public int hashCode() {
            synchronized (mutex) {
                return m.hashCode();
            }
        }
        public boolean isEmpty() {
            synchronized (mutex) {
                return m.isEmpty();
            }
        }
        public Set<K> keySet() {
            synchronized (mutex) {
                return new SynchronizedSet<K>(m.keySet(), mutex);
            }
        }
        public V put(K key, V value) {
            synchronized (mutex) {
                return m.put(key, value);
            }
        }
        public void putAll(Map<? extends K, ? extends V> map) {
            synchronized (mutex) {
                m.putAll(map);
            }
        }
        public V remove(Object key) {
            synchronized (mutex) {
                return m.remove(key);
            }
        }
        public int size() {
            synchronized (mutex) {
                return m.size();
            }
        }
        public Collection<V> values() {
            synchronized (mutex) {
                return new SynchronizedCollection<V>(m.values(), mutex);
            }
        }
        @Override
        public String toString() {
            synchronized (mutex) {
                return m.toString();
            }
        }
        private void writeObject(ObjectOutputStream stream) throws IOException {
            synchronized (mutex) {
                stream.defaultWriteObject();
            }
        }
    }
    static class SynchronizedSet<E> extends SynchronizedCollection<E> implements
            Set<E> {
        private static final long serialVersionUID = 487447009682186044L;
        SynchronizedSet(Set<E> set) {
            super(set);
        }
        SynchronizedSet(Set<E> set, Object mutex) {
            super(set, mutex);
        }
        @Override
        public boolean equals(Object object) {
            synchronized (mutex) {
                return c.equals(object);
            }
        }
        @Override
        public int hashCode() {
            synchronized (mutex) {
                return c.hashCode();
            }
        }
        private void writeObject(ObjectOutputStream stream) throws IOException {
            synchronized (mutex) {
                stream.defaultWriteObject();
            }
        }
    }
    static class SynchronizedSortedMap<K, V> extends SynchronizedMap<K, V>
            implements SortedMap<K, V> {
        private static final long serialVersionUID = -8798146769416483793L;
        private final SortedMap<K, V> sm;
        SynchronizedSortedMap(SortedMap<K, V> map) {
            super(map);
            sm = map;
        }
        SynchronizedSortedMap(SortedMap<K, V> map, Object mutex) {
            super(map, mutex);
            sm = map;
        }
        public Comparator<? super K> comparator() {
            synchronized (mutex) {
                return sm.comparator();
            }
        }
        public K firstKey() {
            synchronized (mutex) {
                return sm.firstKey();
            }
        }
        public SortedMap<K, V> headMap(K endKey) {
            synchronized (mutex) {
                return new SynchronizedSortedMap<K, V>(sm.headMap(endKey),
                        mutex);
            }
        }
        public K lastKey() {
            synchronized (mutex) {
                return sm.lastKey();
            }
        }
        public SortedMap<K, V> subMap(K startKey, K endKey) {
            synchronized (mutex) {
                return new SynchronizedSortedMap<K, V>(sm.subMap(startKey,
                        endKey), mutex);
            }
        }
        public SortedMap<K, V> tailMap(K startKey) {
            synchronized (mutex) {
                return new SynchronizedSortedMap<K, V>(sm.tailMap(startKey),
                        mutex);
            }
        }
        private void writeObject(ObjectOutputStream stream) throws IOException {
            synchronized (mutex) {
                stream.defaultWriteObject();
            }
        }
    }
    static class SynchronizedSortedSet<E> extends SynchronizedSet<E> implements
            SortedSet<E> {
        private static final long serialVersionUID = 8695801310862127406L;
        private final SortedSet<E> ss;
        SynchronizedSortedSet(SortedSet<E> set) {
            super(set);
            ss = set;
        }
        SynchronizedSortedSet(SortedSet<E> set, Object mutex) {
            super(set, mutex);
            ss = set;
        }
        public Comparator<? super E> comparator() {
            synchronized (mutex) {
                return ss.comparator();
            }
        }
        public E first() {
            synchronized (mutex) {
                return ss.first();
            }
        }
        public SortedSet<E> headSet(E end) {
            synchronized (mutex) {
                return new SynchronizedSortedSet<E>(ss.headSet(end), mutex);
            }
        }
        public E last() {
            synchronized (mutex) {
                return ss.last();
            }
        }
        public SortedSet<E> subSet(E start, E end) {
            synchronized (mutex) {
                return new SynchronizedSortedSet<E>(ss.subSet(start, end),
                        mutex);
            }
        }
        public SortedSet<E> tailSet(E start) {
            synchronized (mutex) {
                return new SynchronizedSortedSet<E>(ss.tailSet(start), mutex);
            }
        }
        private void writeObject(ObjectOutputStream stream) throws IOException {
            synchronized (mutex) {
                stream.defaultWriteObject();
            }
        }
    }
    private static class UnmodifiableCollection<E> implements Collection<E>,
            Serializable {
        private static final long serialVersionUID = 1820017752578914078L;
        final Collection<E> c;
        UnmodifiableCollection(Collection<E> collection) {
            c = collection;
        }
        public boolean add(E object) {
            throw new UnsupportedOperationException();
        }
        public boolean addAll(Collection<? extends E> collection) {
            throw new UnsupportedOperationException();
        }
        public void clear() {
            throw new UnsupportedOperationException();
        }
        public boolean contains(Object object) {
            return c.contains(object);
        }
        public boolean containsAll(Collection<?> collection) {
            return c.containsAll(collection);
        }
        public boolean isEmpty() {
            return c.isEmpty();
        }
        public Iterator<E> iterator() {
            return new Iterator<E>() {
                Iterator<E> iterator = c.iterator();
                public boolean hasNext() {
                    return iterator.hasNext();
                }
                public E next() {
                    return iterator.next();
                }
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
        public boolean remove(Object object) {
            throw new UnsupportedOperationException();
        }
        public boolean removeAll(Collection<?> collection) {
            throw new UnsupportedOperationException();
        }
        public boolean retainAll(Collection<?> collection) {
            throw new UnsupportedOperationException();
        }
        public int size() {
            return c.size();
        }
        public Object[] toArray() {
            return c.toArray();
        }
        public <T> T[] toArray(T[] array) {
            return c.toArray(array);
        }
        @Override
        public String toString() {
            return c.toString();
        }
    }
    private static class UnmodifiableRandomAccessList<E> extends
            UnmodifiableList<E> implements RandomAccess {
        private static final long serialVersionUID = -2542308836966382001L;
        UnmodifiableRandomAccessList(List<E> l) {
            super(l);
        }
        @Override
        public List<E> subList(int start, int end) {
            return new UnmodifiableRandomAccessList<E>(list.subList(start, end));
        }
        private Object writeReplace() {
            return new UnmodifiableList<E>(list);
        }
    }
    private static class UnmodifiableList<E> extends UnmodifiableCollection<E>
            implements List<E> {
        private static final long serialVersionUID = -283967356065247728L;
        final List<E> list;
        UnmodifiableList(List<E> l) {
            super(l);
            list = l;
        }
        public void add(int location, E object) {
            throw new UnsupportedOperationException();
        }
        public boolean addAll(int location, Collection<? extends E> collection) {
            throw new UnsupportedOperationException();
        }
        @Override
        public boolean equals(Object object) {
            return list.equals(object);
        }
        public E get(int location) {
            return list.get(location);
        }
        @Override
        public int hashCode() {
            return list.hashCode();
        }
        public int indexOf(Object object) {
            return list.indexOf(object);
        }
        public int lastIndexOf(Object object) {
            return list.lastIndexOf(object);
        }
        public ListIterator<E> listIterator() {
            return listIterator(0);
        }
        public ListIterator<E> listIterator(final int location) {
            return new ListIterator<E>() {
                ListIterator<E> iterator = list.listIterator(location);
                public void add(E object) {
                    throw new UnsupportedOperationException();
                }
                public boolean hasNext() {
                    return iterator.hasNext();
                }
                public boolean hasPrevious() {
                    return iterator.hasPrevious();
                }
                public E next() {
                    return iterator.next();
                }
                public int nextIndex() {
                    return iterator.nextIndex();
                }
                public E previous() {
                    return iterator.previous();
                }
                public int previousIndex() {
                    return iterator.previousIndex();
                }
                public void remove() {
                    throw new UnsupportedOperationException();
                }
                public void set(E object) {
                    throw new UnsupportedOperationException();
                }
            };
        }
        public E remove(int location) {
            throw new UnsupportedOperationException();
        }
        public E set(int location, E object) {
            throw new UnsupportedOperationException();
        }
        public List<E> subList(int start, int end) {
            return new UnmodifiableList<E>(list.subList(start, end));
        }
        private Object readResolve() {
            if (list instanceof RandomAccess) {
                return new UnmodifiableRandomAccessList<E>(list);
            }
            return this;
        }
    }
    private static class UnmodifiableMap<K, V> implements Map<K, V>,
            Serializable {
        private static final long serialVersionUID = -1034234728574286014L;
        private final Map<K, V> m;
        private static class UnmodifiableEntrySet<K, V> extends
                UnmodifiableSet<Map.Entry<K, V>> {
            private static final long serialVersionUID = 7854390611657943733L;
            private static class UnmodifiableMapEntry<K, V> implements
                    Map.Entry<K, V> {
                Map.Entry<K, V> mapEntry;
                UnmodifiableMapEntry(Map.Entry<K, V> entry) {
                    mapEntry = entry;
                }
                @Override
                public boolean equals(Object object) {
                    return mapEntry.equals(object);
                }
                public K getKey() {
                    return mapEntry.getKey();
                }
                public V getValue() {
                    return mapEntry.getValue();
                }
                @Override
                public int hashCode() {
                    return mapEntry.hashCode();
                }
                public V setValue(V object) {
                    throw new UnsupportedOperationException();
                }
                @Override
                public String toString() {
                    return mapEntry.toString();
                }
            }
            UnmodifiableEntrySet(Set<Map.Entry<K, V>> set) {
                super(set);
            }
            @Override
            public Iterator<Map.Entry<K, V>> iterator() {
                return new Iterator<Map.Entry<K, V>>() {
                    Iterator<Map.Entry<K, V>> iterator = c.iterator();
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }
                    public Map.Entry<K, V> next() {
                        return new UnmodifiableMapEntry<K, V>(iterator.next());
                    }
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
            @Override
            public Object[] toArray() {
                int length = c.size();
                Object[] result = new Object[length];
                Iterator<?> it = iterator();
                for (int i = length; --i >= 0;) {
                    result[i] = it.next();
                }
                return result;
            }
            @Override
            @SuppressWarnings("unchecked")
            public <T> T[] toArray(T[] contents) {
                int size = c.size(), index = 0;
                Iterator<Map.Entry<K, V>> it = iterator();
                if (size > contents.length) {
                    Class<?> ct = contents.getClass().getComponentType();
                    contents = (T[]) java.lang.reflect.Array.newInstance(ct, size);
                }
                while (index < size) {
                    contents[index++] = (T) it.next();
                }
                if (index < contents.length) {
                    contents[index] = null;
                }
                return contents;
            }
        }
        UnmodifiableMap(Map<K, V> map) {
            m = map;
        }
        public void clear() {
            throw new UnsupportedOperationException();
        }
        public boolean containsKey(Object key) {
            return m.containsKey(key);
        }
        public boolean containsValue(Object value) {
            return m.containsValue(value);
        }
        public Set<Map.Entry<K, V>> entrySet() {
            return new UnmodifiableEntrySet<K, V>(m.entrySet());
        }
        @Override
        public boolean equals(Object object) {
            return m.equals(object);
        }
        public V get(Object key) {
            return m.get(key);
        }
        @Override
        public int hashCode() {
            return m.hashCode();
        }
        public boolean isEmpty() {
            return m.isEmpty();
        }
        public Set<K> keySet() {
            return new UnmodifiableSet<K>(m.keySet());
        }
        public V put(K key, V value) {
            throw new UnsupportedOperationException();
        }
        public void putAll(Map<? extends K, ? extends V> map) {
            throw new UnsupportedOperationException();
        }
        public V remove(Object key) {
            throw new UnsupportedOperationException();
        }
        public int size() {
            return m.size();
        }
        public Collection<V> values() {
            return new UnmodifiableCollection<V>(m.values());
        }
        @Override
        public String toString() {
            return m.toString();
        }
    }
    private static class UnmodifiableSet<E> extends UnmodifiableCollection<E>
            implements Set<E> {
        private static final long serialVersionUID = -9215047833775013803L;
        UnmodifiableSet(Set<E> set) {
            super(set);
        }
        @Override
        public boolean equals(Object object) {
            return c.equals(object);
        }
        @Override
        public int hashCode() {
            return c.hashCode();
        }
    }
    private static class UnmodifiableSortedMap<K, V> extends
            UnmodifiableMap<K, V> implements SortedMap<K, V> {
        private static final long serialVersionUID = -8806743815996713206L;
        private final SortedMap<K, V> sm;
        UnmodifiableSortedMap(SortedMap<K, V> map) {
            super(map);
            sm = map;
        }
        public Comparator<? super K> comparator() {
            return sm.comparator();
        }
        public K firstKey() {
            return sm.firstKey();
        }
        public SortedMap<K, V> headMap(K before) {
            return new UnmodifiableSortedMap<K, V>(sm.headMap(before));
        }
        public K lastKey() {
            return sm.lastKey();
        }
        public SortedMap<K, V> subMap(K start, K end) {
            return new UnmodifiableSortedMap<K, V>(sm.subMap(start, end));
        }
        public SortedMap<K, V> tailMap(K after) {
            return new UnmodifiableSortedMap<K, V>(sm.tailMap(after));
        }
    }
    private static class UnmodifiableSortedSet<E> extends UnmodifiableSet<E>
            implements SortedSet<E> {
        private static final long serialVersionUID = -4929149591599911165L;
        private final SortedSet<E> ss;
        UnmodifiableSortedSet(SortedSet<E> set) {
            super(set);
            ss = set;
        }
        public Comparator<? super E> comparator() {
            return ss.comparator();
        }
        public E first() {
            return ss.first();
        }
        public SortedSet<E> headSet(E before) {
            return new UnmodifiableSortedSet<E>(ss.headSet(before));
        }
        public E last() {
            return ss.last();
        }
        public SortedSet<E> subSet(E start, E end) {
            return new UnmodifiableSortedSet<E>(ss.subSet(start, end));
        }
        public SortedSet<E> tailSet(E after) {
            return new UnmodifiableSortedSet<E>(ss.tailSet(after));
        }
    }
    private Collections() {
    }
    @SuppressWarnings("unchecked")
    public static <T> int binarySearch(
            List<? extends Comparable<? super T>> list, T object) {
        if (list == null) {
            throw new NullPointerException();
        }
        if (list.isEmpty()) {
            return -1;
        }
        if (!(list instanceof RandomAccess)) {
            ListIterator<? extends Comparable<? super T>> it = list.listIterator();
            while (it.hasNext()) {
                int result;
                if ((result = -it.next().compareTo(object)) <= 0) {
                    if (result == 0) {
                        return it.previousIndex();
                    }
                    return -it.previousIndex() - 1;
                }
            }
            return -list.size() - 1;
        }
        int low = 0, mid = list.size(), high = mid - 1, result = -1;
        while (low <= high) {
            mid = (low + high) >> 1;
            if ((result = -list.get(mid).compareTo(object)) > 0) {
                low = mid + 1;
            } else if (result == 0) {
                return mid;
            } else {
                high = mid - 1;
            }
        }
        return -mid - (result < 0 ? 1 : 2);
    }
    @SuppressWarnings("unchecked")
    public static <T> int binarySearch(List<? extends T> list, T object,
            Comparator<? super T> comparator) {
        if (comparator == null) {
            return Collections.binarySearch(
                    (List<? extends Comparable<? super T>>) list, object);
        }
        if (!(list instanceof RandomAccess)) {
            ListIterator<? extends T> it = list.listIterator();
            while (it.hasNext()) {
                int result;
                if ((result = -comparator.compare(it.next(), object)) <= 0) {
                    if (result == 0) {
                        return it.previousIndex();
                    }
                    return -it.previousIndex() - 1;
                }
            }
            return -list.size() - 1;
        }
        int low = 0, mid = list.size(), high = mid - 1, result = -1;
        while (low <= high) {
            mid = (low + high) >> 1;
            if ((result = -comparator.compare(list.get(mid),object)) > 0) {
                low = mid + 1;
            } else if (result == 0) {
                return mid;
            } else {
                high = mid - 1;
            }
        }
        return -mid - (result < 0 ? 1 : 2);
    }
    public static <T> void copy(List<? super T> destination,
            List<? extends T> source) {
        if (destination.size() < source.size()) {
            throw new ArrayIndexOutOfBoundsException(Msg.getString("K0032", source.size())); 
        }
        Iterator<? extends T> srcIt = source.iterator();
        ListIterator<? super T> destIt = destination.listIterator();
        while (srcIt.hasNext()) {
            try {
                destIt.next();
            } catch (NoSuchElementException e) {
                throw new ArrayIndexOutOfBoundsException(Msg.getString("K0032", source.size())); 
            }
            destIt.set(srcIt.next());
        }
    }
    public static <T> Enumeration<T> enumeration(Collection<T> collection) {
        final Collection<T> c = collection;
        return new Enumeration<T>() {
            Iterator<T> it = c.iterator();
            public boolean hasMoreElements() {
                return it.hasNext();
            }
            public T nextElement() {
                return it.next();
            }
        };
    }
    public static <T> void fill(List<? super T> list, T object) {
        ListIterator<? super T> it = list.listIterator();
        while (it.hasNext()) {
            it.next();
            it.set(object);
        }
    }
    public static <T extends Object & Comparable<? super T>> T max(
            Collection<? extends T> collection) {
        Iterator<? extends T> it = collection.iterator();
        T max = it.next();
        while (it.hasNext()) {
            T next = it.next();
            if (max.compareTo(next) < 0) {
                max = next;
            }
        }
        return max;
    }
    public static <T> T max(Collection<? extends T> collection,
            Comparator<? super T> comparator) {
        if (comparator == null) {
            @SuppressWarnings("unchecked") 
            T result = (T) max((Collection<Comparable>) collection);
            return result;
        }
        Iterator<? extends T> it = collection.iterator();
        T max = it.next();
        while (it.hasNext()) {
            T next = it.next();
            if (comparator.compare(max, next) < 0) {
                max = next;
            }
        }
        return max;
    }
    public static <T extends Object & Comparable<? super T>> T min(
            Collection<? extends T> collection) {
        Iterator<? extends T> it = collection.iterator();
        T min = it.next();
        while (it.hasNext()) {
            T next = it.next();
            if (min.compareTo(next) > 0) {
                min = next;
            }
        }
        return min;
    }
    public static <T> T min(Collection<? extends T> collection,
            Comparator<? super T> comparator) {
        if (comparator == null) {
            @SuppressWarnings("unchecked") 
            T result = (T) min((Collection<Comparable>) collection);
            return result;
        }
        Iterator<? extends T> it = collection.iterator();
        T min = it.next();
        while (it.hasNext()) {
            T next = it.next();
            if (comparator.compare(min, next) > 0) {
                min = next;
            }
        }
        return min;
    }
    public static <T> List<T> nCopies(final int length, T object) {
        return new CopiesList<T>(length, object);
    }
    @SuppressWarnings("unchecked")
    public static void reverse(List<?> list) {
        int size = list.size();
        ListIterator<Object> front = (ListIterator<Object>) list.listIterator();
        ListIterator<Object> back = (ListIterator<Object>) list
                .listIterator(size);
        for (int i = 0; i < size / 2; i++) {
            Object frontNext = front.next();
            Object backPrev = back.previous();
            front.set(backPrev);
            back.set(frontNext);
        }
    }
    @SuppressWarnings("unchecked")
    public static <T> Comparator<T> reverseOrder() {
        return (Comparator) ReverseComparator.INSTANCE;
    }
    public static <T> Comparator<T> reverseOrder(Comparator<T> c) {
        if (c == null) {
            return reverseOrder();
        }
        if (c instanceof ReverseComparatorWithComparator) {
            return ((ReverseComparatorWithComparator<T>) c).comparator;
        }
        return new ReverseComparatorWithComparator<T>(c);
    }
    public static void shuffle(List<?> list) {
        shuffle(list, new Random());
    }
    public static void shuffle(List<?> list, Random random) {
        @SuppressWarnings("unchecked") 
        final List<Object> objectList = (List<Object>) list;
        if (list instanceof RandomAccess) {
            for (int i = objectList.size() - 1; i > 0; i--) {
                int index = random.nextInt(i + 1);
                objectList.set(index, objectList.set(i, objectList.get(index)));
            }
        } else {
            Object[] array = objectList.toArray();
            for (int i = array.length - 1; i > 0; i--) {
                int index = random.nextInt(i + 1);
                Object temp = array[i];
                array[i] = array[index];
                array[index] = temp;
            }
            int i = 0;
            ListIterator<Object> it = objectList.listIterator();
            while (it.hasNext()) {
                it.next();
                it.set(array[i++]);
            }
        }
    }
    public static <E> Set<E> singleton(E object) {
        return new SingletonSet<E>(object);
    }
    public static <E> List<E> singletonList(E object) {
        return new SingletonList<E>(object);
    }
    public static <K, V> Map<K, V> singletonMap(K key, V value) {
        return new SingletonMap<K, V>(key, value);
    }
    @SuppressWarnings("unchecked")
    public static <T extends Comparable<? super T>> void sort(List<T> list) {
        Object[] array = list.toArray();
        Arrays.sort(array);
        int i = 0;
        ListIterator<T> it = list.listIterator();
        while (it.hasNext()) {
            it.next();
            it.set((T) array[i++]);
        }
    }
    @SuppressWarnings("unchecked")
    public static <T> void sort(List<T> list, Comparator<? super T> comparator) {
        T[] array = list.toArray((T[]) new Object[list.size()]);
        Arrays.sort(array, comparator);
        int i = 0;
        ListIterator<T> it = list.listIterator();
        while (it.hasNext()) {
            it.next();
            it.set(array[i++]);
        }
    }
    @SuppressWarnings("unchecked")
    public static void swap(List<?> list, int index1, int index2) {
        if (list == null) {
            throw new NullPointerException();
        }
        final int size = list.size();
        if (index1 < 0 || index1 >= size || index2 < 0 || index2 >= size) {
            throw new IndexOutOfBoundsException();
        }
        if (index1 == index2) {
            return;
        }
        List<Object> rawList = (List<Object>) list;
        rawList.set(index2, rawList.set(index1, rawList.get(index2)));
    }
    public static <T> boolean replaceAll(List<T> list, T obj, T obj2) {
        int index;
        boolean found = false;
        while ((index = list.indexOf(obj)) > -1) {
            found = true;
            list.set(index, obj2);
        }
        return found;
    }
    @SuppressWarnings("unchecked")
    public static void rotate(List<?> lst, int dist) {
        List<Object> list = (List<Object>) lst;
        int size = list.size();
        if (size == 0) {
            return;
        }
        int normdist;
        if (dist > 0) {
            normdist = dist % size;
        } else {
            normdist = size - ((dist % size) * (-1));
        }
        if (normdist == 0 || normdist == size) {
            return;
        }
        if (list instanceof RandomAccess) {
            Object temp = list.get(0);
            int index = 0, beginIndex = 0;
            for (int i = 0; i < size; i++) {
                index = (index + normdist) % size;
                temp = list.set(index, temp);
                if (index == beginIndex) {
                    index = ++beginIndex;
                    temp = list.get(beginIndex);
                }
            }
        } else {
            int divideIndex = (size - normdist) % size;
            List<Object> sublist1 = list.subList(0, divideIndex);
            List<Object> sublist2 = list.subList(divideIndex, size);
            reverse(sublist1);
            reverse(sublist2);
            reverse(list);
        }
    }
    public static int indexOfSubList(List<?> list, List<?> sublist) {
        int size = list.size();
        int sublistSize = sublist.size();
        if (sublistSize > size) {
            return -1;
        }
        if (sublistSize == 0) {
            return 0;
        }
        Object firstObj = sublist.get(0);
        int index = list.indexOf(firstObj);
        if (index == -1) {
            return -1;
        }
        while (index < size && (size - index >= sublistSize)) {
            ListIterator<?> listIt = list.listIterator(index);
            if ((firstObj == null) ? listIt.next() == null : firstObj
                    .equals(listIt.next())) {
                ListIterator<?> sublistIt = sublist.listIterator(1);
                boolean difFound = false;
                while (sublistIt.hasNext()) {
                    Object element = sublistIt.next();
                    if (!listIt.hasNext()) {
                        return -1;
                    }
                    if ((element == null) ? listIt.next() != null : !element
                            .equals(listIt.next())) {
                        difFound = true;
                        break;
                    }
                }
                if (!difFound) {
                    return index;
                }
            }
            index++;
        }
        return -1;
    }
    public static int lastIndexOfSubList(List<?> list, List<?> sublist) {
        int sublistSize = sublist.size();
        int size = list.size();
        if (sublistSize > size) {
            return -1;
        }
        if (sublistSize == 0) {
            return size;
        }
        Object lastObj = sublist.get(sublistSize - 1);
        int index = list.lastIndexOf(lastObj);
        while ((index > -1) && (index + 1 >= sublistSize)) {
            ListIterator<?> listIt = list.listIterator(index + 1);
            if ((lastObj == null) ? listIt.previous() == null : lastObj
                    .equals(listIt.previous())) {
                ListIterator<?> sublistIt = sublist
                        .listIterator(sublistSize - 1);
                boolean difFound = false;
                while (sublistIt.hasPrevious()) {
                    Object element = sublistIt.previous();
                    if (!listIt.hasPrevious()) {
                        return -1;
                    }
                    if ((element == null) ? listIt.previous() != null
                            : !element.equals(listIt.previous())) {
                        difFound = true;
                        break;
                    }
                }
                if (!difFound) {
                    return listIt.nextIndex();
                }
            }
            index--;
        }
        return -1;
    }
    public static <T> ArrayList<T> list(Enumeration<T> enumeration) {
        ArrayList<T> list = new ArrayList<T>();
        while (enumeration.hasMoreElements()) {
            list.add(enumeration.nextElement());
        }
        return list;
    }
    public static <T> Collection<T> synchronizedCollection(
            Collection<T> collection) {
        if (collection == null) {
            throw new NullPointerException();
        }
        return new SynchronizedCollection<T>(collection);
    }
    public static <T> List<T> synchronizedList(List<T> list) {
        if (list == null) {
            throw new NullPointerException();
        }
        if (list instanceof RandomAccess) {
            return new SynchronizedRandomAccessList<T>(list);
        }
        return new SynchronizedList<T>(list);
    }
    public static <K, V> Map<K, V> synchronizedMap(Map<K, V> map) {
        if (map == null) {
            throw new NullPointerException();
        }
        return new SynchronizedMap<K, V>(map);
    }
    public static <E> Set<E> synchronizedSet(Set<E> set) {
        if (set == null) {
            throw new NullPointerException();
        }
        return new SynchronizedSet<E>(set);
    }
    public static <K, V> SortedMap<K, V> synchronizedSortedMap(
            SortedMap<K, V> map) {
        if (map == null) {
            throw new NullPointerException();
        }
        return new SynchronizedSortedMap<K, V>(map);
    }
    public static <E> SortedSet<E> synchronizedSortedSet(SortedSet<E> set) {
        if (set == null) {
            throw new NullPointerException();
        }
        return new SynchronizedSortedSet<E>(set);
    }
    @SuppressWarnings("unchecked")
    public static <E> Collection<E> unmodifiableCollection(
            Collection<? extends E> collection) {
        if (collection == null) {
            throw new NullPointerException();
        }
        return new UnmodifiableCollection<E>((Collection<E>) collection);
    }
    @SuppressWarnings("unchecked")
    public static <E> List<E> unmodifiableList(List<? extends E> list) {
        if (list == null) {
            throw new NullPointerException();
        }
        if (list instanceof RandomAccess) {
            return new UnmodifiableRandomAccessList<E>((List<E>) list);
        }
        return new UnmodifiableList<E>((List<E>) list);
    }
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> unmodifiableMap(
            Map<? extends K, ? extends V> map) {
        if (map == null) {
            throw new NullPointerException();
        }
        return new UnmodifiableMap<K, V>((Map<K, V>) map);
    }
    @SuppressWarnings("unchecked")
    public static <E> Set<E> unmodifiableSet(Set<? extends E> set) {
        if (set == null) {
            throw new NullPointerException();
        }
        return new UnmodifiableSet<E>((Set<E>) set);
    }
    @SuppressWarnings("unchecked")
    public static <K, V> SortedMap<K, V> unmodifiableSortedMap(
            SortedMap<K, ? extends V> map) {
        if (map == null) {
            throw new NullPointerException();
        }
        return new UnmodifiableSortedMap<K, V>((SortedMap<K, V>) map);
    }
    public static <E> SortedSet<E> unmodifiableSortedSet(SortedSet<E> set) {
        if (set == null) {
            throw new NullPointerException();
        }
        return new UnmodifiableSortedSet<E>(set);
    }
    public static int frequency(Collection<?> c, Object o) {
        if (c == null) {
            throw new NullPointerException();
        }
        if (c.isEmpty()) {
            return 0;
        }
        int result = 0;
        Iterator<?> itr = c.iterator();
        while (itr.hasNext()) {
            Object e = itr.next();
            if (o == null ? e == null : o.equals(e)) {
                result++;
            }
        }
        return result;
    }
    @SuppressWarnings("unchecked")
    public static final <T> List<T> emptyList() {
        return EMPTY_LIST;
    }
    @SuppressWarnings("unchecked")
    public static final <T> Set<T> emptySet() {
        return EMPTY_SET;
    }
    @SuppressWarnings("unchecked")
    public static final <K, V> Map<K, V> emptyMap() {
        return EMPTY_MAP;
    }
    public static <E> Collection<E> checkedCollection(Collection<E> c,
            Class<E> type) {
        return new CheckedCollection<E>(c, type);
    }
    public static <K, V> Map<K, V> checkedMap(Map<K, V> m, Class<K> keyType,
            Class<V> valueType) {
        return new CheckedMap<K, V>(m, keyType, valueType);
    }
    public static <E> List<E> checkedList(List<E> list, Class<E> type) {
        if (list instanceof RandomAccess) {
            return new CheckedRandomAccessList<E>(list, type);
        }
        return new CheckedList<E>(list, type);
    }
    public static <E> Set<E> checkedSet(Set<E> s, Class<E> type) {
        return new CheckedSet<E>(s, type);
    }
    public static <K, V> SortedMap<K, V> checkedSortedMap(SortedMap<K, V> m,
            Class<K> keyType, Class<V> valueType) {
        return new CheckedSortedMap<K, V>(m, keyType, valueType);
    }
    public static <E> SortedSet<E> checkedSortedSet(SortedSet<E> s,
            Class<E> type) {
        return new CheckedSortedSet<E>(s, type);
    }
    public static <T> boolean addAll(Collection<? super T> c, T... a) {
        boolean modified = false;
        for (int i = 0; i < a.length; i++) {
            modified |= c.add(a[i]);
        }
        return modified;
    }
    public static boolean disjoint(Collection<?> c1, Collection<?> c2) {
        if ((c1 instanceof Set) && !(c2 instanceof Set)
                || (c2.size()) > c1.size()) {
            Collection<?> tmp = c1;
            c1 = c2;
            c2 = tmp;
        }
        Iterator<?> it = c1.iterator();
        while (it.hasNext()) {
            if (c2.contains(it.next())) {
                return false;
            }
        }
        return true;
    }
    static <E> E checkType(E obj, Class<? extends E> type) {
        if (obj != null && !type.isInstance(obj)) {
            throw new ClassCastException(Messages.getString(
                    "luni.05", obj.getClass(), type)); 
        }
        return obj;
    }
    private static class CheckedCollection<E> implements Collection<E>,
            Serializable {
        private static final long serialVersionUID = 1578914078182001775L;
        Collection<E> c;
        Class<E> type;
        public CheckedCollection(Collection<E> c, Class<E> type) {
            if (c == null || type == null) {
                throw new NullPointerException();
            }
            this.c = c;
            this.type = type;
        }
        public int size() {
            return c.size();
        }
        public boolean isEmpty() {
            return c.isEmpty();
        }
        public boolean contains(Object obj) {
            return c.contains(obj);
        }
        public Iterator<E> iterator() {
            Iterator<E> i = c.iterator();
            if (i instanceof ListIterator) {
                i = new CheckedListIterator<E>((ListIterator<E>) i, type);
            }
            return i;
        }
        public Object[] toArray() {
            return c.toArray();
        }
        public <T> T[] toArray(T[] arr) {
            return c.toArray(arr);
        }
        public boolean add(E obj) {
            return c.add(checkType(obj, type));
        }
        public boolean remove(Object obj) {
            return c.remove(obj);
        }
        public boolean containsAll(Collection<?> c1) {
            return c.containsAll(c1);
        }
        @SuppressWarnings("unchecked")
        public boolean addAll(Collection<? extends E> c1) {
            Object[] array = c1.toArray();
            for (Object o : array) {
                checkType(o, type);
            }
            return c.addAll((List<E>) Arrays.asList(array));
        }
        public boolean removeAll(Collection<?> c1) {
            return c.removeAll(c1);
        }
        public boolean retainAll(Collection<?> c1) {
            return c.retainAll(c1);
        }
        public void clear() {
            c.clear();
        }
        @Override
        public String toString() {
            return c.toString();
        }
    }
    private static class CheckedListIterator<E> implements ListIterator<E> {
        private ListIterator<E> i;
        private Class<E> type;
        public CheckedListIterator(ListIterator<E> i, Class<E> type) {
            this.i = i;
            this.type = type;
        }
        public boolean hasNext() {
            return i.hasNext();
        }
        public E next() {
            return i.next();
        }
        public void remove() {
            i.remove();
        }
        public boolean hasPrevious() {
            return i.hasPrevious();
        }
        public E previous() {
            return i.previous();
        }
        public int nextIndex() {
            return i.nextIndex();
        }
        public int previousIndex() {
            return i.previousIndex();
        }
        public void set(E obj) {
            i.set(checkType(obj, type));
        }
        public void add(E obj) {
            i.add(checkType(obj, type));
        }
    }
    private static class CheckedList<E> extends CheckedCollection<E> implements
            List<E> {
        private static final long serialVersionUID = 65247728283967356L;
        List<E> l;
        public CheckedList(List<E> l, Class<E> type) {
            super(l, type);
            this.l = l;
        }
        @SuppressWarnings("unchecked")
        public boolean addAll(int index, Collection<? extends E> c1) {
            Object[] array = c1.toArray();
            for (Object o : array) {
                checkType(o, type);
            }
            return l.addAll(index, (List<E>) Arrays.asList(array));
        }
        public E get(int index) {
            return l.get(index);
        }
        public E set(int index, E obj) {
            return l.set(index, checkType(obj, type));
        }
        public void add(int index, E obj) {
            l.add(index, checkType(obj, type));
        }
        public E remove(int index) {
            return l.remove(index);
        }
        public int indexOf(Object obj) {
            return l.indexOf(obj);
        }
        public int lastIndexOf(Object obj) {
            return l.lastIndexOf(obj);
        }
        public ListIterator<E> listIterator() {
            return new CheckedListIterator<E>(l.listIterator(), type);
        }
        public ListIterator<E> listIterator(int index) {
            return new CheckedListIterator<E>(l.listIterator(index), type);
        }
        public List<E> subList(int fromIndex, int toIndex) {
            return checkedList(l.subList(fromIndex, toIndex), type);
        }
        @Override
        public boolean equals(Object obj) {
            return l.equals(obj);
        }
        @Override
        public int hashCode() {
            return l.hashCode();
        }
    }
    private static class CheckedRandomAccessList<E> extends CheckedList<E>
            implements RandomAccess {
        private static final long serialVersionUID = 1638200125423088369L;
        public CheckedRandomAccessList(List<E> l, Class<E> type) {
            super(l, type);
        }
    }
    private static class CheckedSet<E> extends CheckedCollection<E> implements
            Set<E> {
        private static final long serialVersionUID = 4694047833775013803L;
        public CheckedSet(Set<E> s, Class<E> type) {
            super(s, type);
        }
        @Override
        public boolean equals(Object obj) {
            return c.equals(obj);
        }
        @Override
        public int hashCode() {
            return c.hashCode();
        }
    }
    private static class CheckedMap<K, V> implements Map<K, V>, Serializable {
        private static final long serialVersionUID = 5742860141034234728L;
        Map<K, V> m;
        Class<K> keyType;
        Class<V> valueType;
        private CheckedMap(Map<K, V> m, Class<K> keyType, Class<V> valueType) {
            if (m == null || keyType == null || valueType == null) {
                throw new NullPointerException();
            }
            this.m = m;
            this.keyType = keyType;
            this.valueType = valueType;
        }
        public int size() {
            return m.size();
        }
        public boolean isEmpty() {
            return m.isEmpty();
        }
        public boolean containsKey(Object key) {
            return m.containsKey(key);
        }
        public boolean containsValue(Object value) {
            return m.containsValue(value);
        }
        public V get(Object key) {
            return m.get(key);
        }
        public V put(K key, V value) {
            return m.put(checkType(key, keyType), checkType(value, valueType));
        }
        public V remove(Object key) {
            return m.remove(key);
        }
        @SuppressWarnings("unchecked")
        public void putAll(Map<? extends K, ? extends V> map) {
            int size = map.size();
            if (size == 0) {
                return;
            }
            Map.Entry<? extends K, ? extends V>[] entries = new Map.Entry[size];
            Iterator<? extends Map.Entry<? extends K, ? extends V>> it = map
                    .entrySet().iterator();
            for (int i = 0; i < size; i++) {
                Map.Entry<? extends K, ? extends V> e = it.next();
                checkType(e.getKey(), keyType);
                checkType(e.getValue(), valueType);
                entries[i] = e;
            }
            for (int i = 0; i < size; i++) {
                m.put(entries[i].getKey(), entries[i].getValue());
            }
        }
        public void clear() {
            m.clear();
        }
        public Set<K> keySet() {
            return m.keySet();
        }
        public Collection<V> values() {
            return m.values();
        }
        public Set<Map.Entry<K, V>> entrySet() {
            return new CheckedEntrySet<K, V>(m.entrySet(), valueType);
        }
        @Override
        public boolean equals(Object obj) {
            return m.equals(obj);
        }
        @Override
        public int hashCode() {
            return m.hashCode();
        }
        @Override
        public String toString() {
            return m.toString();
        }
        private static class CheckedEntry<K, V> implements Map.Entry<K, V> {
            Map.Entry<K, V> e;
            Class<V> valueType;
            public CheckedEntry(Map.Entry<K, V> e, Class<V> valueType) {
                if (e == null) {
                    throw new NullPointerException();
                }
                this.e = e;
                this.valueType = valueType;
            }
            public K getKey() {
                return e.getKey();
            }
            public V getValue() {
                return e.getValue();
            }
            public V setValue(V obj) {
                return e.setValue(checkType(obj, valueType));
            }
            @Override
            public boolean equals(Object obj) {
                return e.equals(obj);
            }
            @Override
            public int hashCode() {
                return e.hashCode();
            }
        }
        private static class CheckedEntrySet<K, V> implements
                Set<Map.Entry<K, V>> {
            Set<Map.Entry<K, V>> s;
            Class<V> valueType;
            public CheckedEntrySet(Set<Map.Entry<K, V>> s, Class<V> valueType) {
                this.s = s;
                this.valueType = valueType;
            }
            public Iterator<Map.Entry<K, V>> iterator() {
                return new CheckedEntryIterator<K, V>(s.iterator(), valueType);
            }
            public Object[] toArray() {
                int thisSize = size();
                Object[] array = new Object[thisSize];
                Iterator<?> it = iterator();
                for (int i = 0; i < thisSize; i++) {
                    array[i] = it.next();
                }
                return array;
            }
            @SuppressWarnings("unchecked")
            public <T> T[] toArray(T[] array) {
                int thisSize = size();
                if (array.length < thisSize) {
                    Class<?> ct = array.getClass().getComponentType();
                    array = (T[]) Array.newInstance(ct, thisSize);
                }
                Iterator<?> it = iterator();
                for (int i = 0; i < thisSize; i++) {
                    array[i] = (T) it.next();
                }
                if (thisSize < array.length) {
                    array[thisSize] = null;
                }
                return array;
            }
            public boolean retainAll(Collection<?> c) {
                return s.retainAll(c);
            }
            public boolean removeAll(Collection<?> c) {
                return s.removeAll(c);
            }
            public boolean containsAll(Collection<?> c) {
                return s.containsAll(c);
            }
            public boolean addAll(Collection<? extends Map.Entry<K, V>> c) {
                throw new UnsupportedOperationException();
            }
            public boolean remove(Object o) {
                return s.remove(o);
            }
            public boolean contains(Object o) {
                return s.contains(o);
            }
            public boolean add(Map.Entry<K, V> o) {
                throw new UnsupportedOperationException();
            }
            public boolean isEmpty() {
                return s.isEmpty();
            }
            public void clear() {
                s.clear();
            }
            public int size() {
                return s.size();
            }
            @Override
            public int hashCode() {
                return s.hashCode();
            }
            @Override
            public boolean equals(Object object) {
                return s.equals(object);
            }
            private static class CheckedEntryIterator<K, V> implements
                    Iterator<Map.Entry<K, V>> {
                Iterator<Map.Entry<K, V>> i;
                Class<V> valueType;
                public CheckedEntryIterator(Iterator<Map.Entry<K, V>> i,
                        Class<V> valueType) {
                    this.i = i;
                    this.valueType = valueType;
                }
                public boolean hasNext() {
                    return i.hasNext();
                }
                public void remove() {
                    i.remove();
                }
                public Map.Entry<K, V> next() {
                    return new CheckedEntry<K, V>(i.next(), valueType);
                }
            }
        }
    }
    private static class CheckedSortedSet<E> extends CheckedSet<E> implements
            SortedSet<E> {
        private static final long serialVersionUID = 1599911165492914959L;
        private SortedSet<E> ss;
        public CheckedSortedSet(SortedSet<E> s, Class<E> type) {
            super(s, type);
            this.ss = s;
        }
        public Comparator<? super E> comparator() {
            return ss.comparator();
        }
        public SortedSet<E> subSet(E fromElement, E toElement) {
            return new CheckedSortedSet<E>(ss.subSet(fromElement, toElement),
                    type);
        }
        public SortedSet<E> headSet(E toElement) {
            return new CheckedSortedSet<E>(ss.headSet(toElement), type);
        }
        public SortedSet<E> tailSet(E fromElement) {
            return new CheckedSortedSet<E>(ss.tailSet(fromElement), type);
        }
        public E first() {
            return ss.first();
        }
        public E last() {
            return ss.last();
        }
    }
    private static class CheckedSortedMap<K, V> extends CheckedMap<K, V>
            implements SortedMap<K, V> {
        private static final long serialVersionUID = 1599671320688067438L;
        SortedMap<K, V> sm;
        CheckedSortedMap(SortedMap<K, V> m, Class<K> keyType, Class<V> valueType) {
            super(m, keyType, valueType);
            this.sm = m;
        }
        public Comparator<? super K> comparator() {
            return sm.comparator();
        }
        public SortedMap<K, V> subMap(K fromKey, K toKey) {
            return new CheckedSortedMap<K, V>(sm.subMap(fromKey, toKey),
                    keyType, valueType);
        }
        public SortedMap<K, V> headMap(K toKey) {
            return new CheckedSortedMap<K, V>(sm.headMap(toKey), keyType,
                    valueType);
        }
        public SortedMap<K, V> tailMap(K fromKey) {
            return new CheckedSortedMap<K, V>(sm.tailMap(fromKey), keyType,
                    valueType);
        }
        public K firstKey() {
            return sm.firstKey();
        }
        public K lastKey() {
            return sm.lastKey();
        }
    }
}
