public class IdentityHashMap<K, V> extends AbstractMap<K, V> implements
        Map<K, V>, Serializable, Cloneable {
    private static final long serialVersionUID = 8188218128353913216L;
    transient Object[] elementData;
    int size;
    transient int threshold;
    private static final int DEFAULT_MAX_SIZE = 21;
    private static final int loadFactor = 7500;
    transient int modCount = 0;
    private static final Object NULL_OBJECT = new Object();  
    static class IdentityHashMapEntry<K, V> extends MapEntry<K, V> {
        IdentityHashMapEntry(K theKey, V theValue) {
            super(theKey, theValue);
        }
        @Override
        public Object clone() {
            return super.clone();
        }
        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object instanceof Map.Entry) {
                Map.Entry<?, ?> entry = (Map.Entry) object;
                return (key == entry.getKey()) && (value == entry.getValue());
            }
            return false;
        }
        @Override
        public int hashCode() {
            return System.identityHashCode(key)
                    ^ System.identityHashCode(value);
        }
        @Override
        public String toString() {
            return key + "=" + value; 
        }
    }
    static class IdentityHashMapIterator<E, KT, VT> implements Iterator<E> {
        private int position = 0; 
        private int lastPosition = 0;
        final IdentityHashMap<KT, VT> associatedMap;
        int expectedModCount;
        final MapEntry.Type<E, KT, VT> type;
        boolean canRemove = false;
        IdentityHashMapIterator(MapEntry.Type<E, KT, VT> value,
                IdentityHashMap<KT, VT> hm) {
            associatedMap = hm;
            type = value;
            expectedModCount = hm.modCount;
        }
        public boolean hasNext() {
            while (position < associatedMap.elementData.length) {
                if (associatedMap.elementData[position] == null) {
                    position += 2;
                } else {
                    return true;
                }
            }
            return false;
        }
        void checkConcurrentMod() throws ConcurrentModificationException {
            if (expectedModCount != associatedMap.modCount) {
                throw new ConcurrentModificationException();
            }
        }
        public E next() {
            checkConcurrentMod();
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            IdentityHashMapEntry<KT, VT> result = associatedMap
                    .getEntry(position);
            lastPosition = position;
            position += 2;
            canRemove = true;
            return type.get(result);
        }
        public void remove() {
            checkConcurrentMod();
            if (!canRemove) {
                throw new IllegalStateException();
            }
            canRemove = false;
            associatedMap.remove(associatedMap.elementData[lastPosition]);
            position = lastPosition;
            expectedModCount++;
        }
    }
    static class IdentityHashMapEntrySet<KT, VT> extends
            AbstractSet<Map.Entry<KT, VT>> {
        private final IdentityHashMap<KT, VT> associatedMap;
        public IdentityHashMapEntrySet(IdentityHashMap<KT, VT> hm) {
            associatedMap = hm;
        }
        IdentityHashMap<KT, VT> hashMap() {
            return associatedMap;
        }
        @Override
        public int size() {
            return associatedMap.size;
        }
        @Override
        public void clear() {
            associatedMap.clear();
        }
        @Override
        public boolean remove(Object object) {
            if (contains(object)) {
                associatedMap.remove(((Map.Entry) object).getKey());
                return true;
            }
            return false;
        }
        @Override
        public boolean contains(Object object) {
            if (object instanceof Map.Entry) {
                IdentityHashMapEntry<?, ?> entry = associatedMap
                        .getEntry(((Map.Entry) object).getKey());
                return entry != null && entry.equals(object);
            }
            return false;
        }
        @Override
        public Iterator<Map.Entry<KT, VT>> iterator() {
            return new IdentityHashMapIterator<Map.Entry<KT, VT>, KT, VT>(
                    new MapEntry.Type<Map.Entry<KT, VT>, KT, VT>() {
                        public Map.Entry<KT, VT> get(MapEntry<KT, VT> entry) {
                            return entry;
                        }
                    }, associatedMap);
        }
    }
    public IdentityHashMap() {
        this(DEFAULT_MAX_SIZE);
    }
    public IdentityHashMap(int maxSize) {
        if (maxSize >= 0) {
            this.size = 0;
            threshold = getThreshold(maxSize);
            elementData = newElementArray(computeElementArraySize());
        } else {
            throw new IllegalArgumentException();
        }
    }
    private int getThreshold(int maxSize) {
        return maxSize > 3 ? maxSize : 3;
    }
    private int computeElementArraySize() {
        int arraySize = (int) (((long) threshold * 10000) / loadFactor) * 2;
        return arraySize < 0 ? -arraySize : arraySize;
    }
    private Object[] newElementArray(int s) {
        return new Object[s];
    }
    public IdentityHashMap(Map<? extends K, ? extends V> map) {
        this(map.size() < 6 ? 11 : map.size() * 2);
        putAllImpl(map);
    }
    @SuppressWarnings("unchecked")
    private V massageValue(Object value) {
        return (V) ((value == NULL_OBJECT) ? null : value);
    }
    @Override
    public void clear() {
        size = 0;
        for (int i = 0; i < elementData.length; i++) {
            elementData[i] = null;
        }
        modCount++;
    }
    @Override
    public boolean containsKey(Object key) {
        if (key == null) {
            key = NULL_OBJECT;
        }
        int index = findIndex(key, elementData);
        return elementData[index] == key;
    }
    @Override
    public boolean containsValue(Object value) {
        if (value == null) {
            value = NULL_OBJECT;
        }
        for (int i = 1; i < elementData.length; i = i + 2) {
            if (elementData[i] == value) {
                return true;
            }
        }
        return false;
    }
    @Override
    public V get(Object key) {
        if (key == null) {
            key = NULL_OBJECT;
        }
        int index = findIndex(key, elementData);
        if (elementData[index] == key) {
            Object result = elementData[index + 1];
            return massageValue(result);
        }
        return null;
    }
    private IdentityHashMapEntry<K, V> getEntry(Object key) {
        if (key == null) {
            key = NULL_OBJECT;
        }
        int index = findIndex(key, elementData);
        if (elementData[index] == key) {
            return getEntry(index);
        }
        return null;
    }
    @SuppressWarnings("unchecked")
    private IdentityHashMapEntry<K, V> getEntry(int index) {
        Object key = elementData[index];
        Object value = elementData[index + 1];
        if (key == NULL_OBJECT) {
            key = null;
        }
        if (value == NULL_OBJECT) {
            value = null;
        }
        return new IdentityHashMapEntry<K, V>((K) key, (V) value);
    }
    private int findIndex(Object key, Object[] array) {
        int length = array.length;
        int index = getModuloHash(key, length);
        int last = (index + length - 2) % length;
        while (index != last) {
            if (array[index] == key || (array[index] == null)) {
                break;
            }
            index = (index + 2) % length;
        }
        return index;
    }
    private int getModuloHash(Object key, int length) {
        return ((System.identityHashCode(key) & 0x7FFFFFFF) % (length / 2)) * 2;
    }
    @Override
    public V put(K key, V value) {
        Object _key = key;
        Object _value = value;
        if (_key == null) {
            _key = NULL_OBJECT;
        }
        if (_value == null) {
            _value = NULL_OBJECT;
        }
        int index = findIndex(_key, elementData);
        if (elementData[index] != _key) {
            modCount++;
            if (++size > threshold) {
                rehash();
                index = findIndex(_key, elementData);
            }
            elementData[index] = _key;
            elementData[index + 1] = null;
        }
        Object result = elementData[index + 1];
        elementData[index + 1] = _value;
        return massageValue(result);
    }
    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        putAllImpl(map);
    }
    private void rehash() {
        int newlength = elementData.length << 1;
        if (newlength == 0) {
            newlength = 1;
        }
        Object[] newData = newElementArray(newlength);
        for (int i = 0; i < elementData.length; i = i + 2) {
            Object key = elementData[i];
            if (key != null) {
                int index = findIndex(key, newData);
                newData[index] = key;
                newData[index + 1] = elementData[i + 1];
            }
        }
        elementData = newData;
        computeMaxSize();
    }
    private void computeMaxSize() {
        threshold = (int) ((long) (elementData.length / 2) * loadFactor / 10000);
    }
    @Override
    public V remove(Object key) {
        if (key == null) {
            key = NULL_OBJECT;
        }
        boolean hashedOk;
        int index, next, hash;
        Object result, object;
        index = next = findIndex(key, elementData);
        if (elementData[index] != key) {
            return null;
        }
        result = elementData[index + 1];
        int length = elementData.length;
        while (true) {
            next = (next + 2) % length;
            object = elementData[next];
            if (object == null) {
                break;
            }
            hash = getModuloHash(object, length);
            hashedOk = hash > index;
            if (next < index) {
                hashedOk = hashedOk || (hash <= next);
            } else {
                hashedOk = hashedOk && (hash <= next);
            }
            if (!hashedOk) {
                elementData[index] = object;
                elementData[index + 1] = elementData[next + 1];
                index = next;
            }
        }
        size--;
        modCount++;
        elementData[index] = null;
        elementData[index + 1] = null;
        return massageValue(result);
    }
    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return new IdentityHashMapEntrySet<K, V>(this);
    }
    @Override
    public Set<K> keySet() {
        if (keySet == null) {
            keySet = new AbstractSet<K>() {
                @Override
                public boolean contains(Object object) {
                    return containsKey(object);
                }
                @Override
                public int size() {
                    return IdentityHashMap.this.size();
                }
                @Override
                public void clear() {
                    IdentityHashMap.this.clear();
                }
                @Override
                public boolean remove(Object key) {
                    if (containsKey(key)) {
                        IdentityHashMap.this.remove(key);
                        return true;
                    }
                    return false;
                }
                @Override
                public Iterator<K> iterator() {
                    return new IdentityHashMapIterator<K, K, V>(
                            new MapEntry.Type<K, K, V>() {
                                public K get(MapEntry<K, V> entry) {
                                    return entry.key;
                                }
                            }, IdentityHashMap.this);
                }
            };
        }
        return keySet;
    }
    @Override
    public Collection<V> values() {
        if (valuesCollection == null) {
            valuesCollection = new AbstractCollection<V>() {
                @Override
                public boolean contains(Object object) {
                    return containsValue(object);
                }
                @Override
                public int size() {
                    return IdentityHashMap.this.size();
                }
                @Override
                public void clear() {
                    IdentityHashMap.this.clear();
                }
                @Override
                public Iterator<V> iterator() {
                    return new IdentityHashMapIterator<V, K, V>(
                            new MapEntry.Type<V, K, V>() {
                                public V get(MapEntry<K, V> entry) {
                                    return entry.value;
                                }
                            }, IdentityHashMap.this);
                }
                @Override
                public boolean remove(Object object) {
                    Iterator<?> it = iterator();
                    while (it.hasNext()) {
                        if (object == it.next()) {
                            it.remove();
                            return true;
                        }
                    }
                    return false;
                }
            };
        }
        return valuesCollection;
    }
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof Map) {
            Map<?, ?> map = (Map) object;
            if (size() != map.size()) {
                return false;
            }
            Set<Map.Entry<K, V>> set = entrySet();
            return set.equals(map.entrySet());
        }
        return false;
    }
    @Override
    public Object clone() {
        try {
            IdentityHashMap<K, V> cloneHashMap = (IdentityHashMap<K, V>) super
                    .clone();
            cloneHashMap.elementData = newElementArray(elementData.length);
            System.arraycopy(elementData, 0, cloneHashMap.elementData, 0,
                    elementData.length);
            return cloneHashMap;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); 
        }
    }
    @Override
    public boolean isEmpty() {
        return size == 0;
    }
    @Override
    public int size() {
        return size;
    }
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        stream.writeInt(size);
        Iterator<?> iterator = entrySet().iterator();
        while (iterator.hasNext()) {
            MapEntry<?, ?> entry = (MapEntry) iterator.next();
            stream.writeObject(entry.key);
            stream.writeObject(entry.value);
        }
    }
    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
        int savedSize = stream.readInt();
        threshold = getThreshold(DEFAULT_MAX_SIZE);
        elementData = newElementArray(computeElementArraySize());
        for (int i = savedSize; --i >= 0;) {
            K key = (K) stream.readObject();
            put(key, (V) stream.readObject());
        }
        size = savedSize;
    }
    private void putAllImpl(Map<? extends K, ? extends V> map) {
        if (map.entrySet() != null) {
            super.putAll(map);
        }
    }
}
