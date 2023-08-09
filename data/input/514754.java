class MapEntry<K, V> implements Map.Entry<K, V>, Cloneable {
    K key;
    V value;
    interface Type<RT, KT, VT> {
        RT get(MapEntry<KT, VT> entry);
    }
    MapEntry(K theKey) {
        key = theKey;
    }
    MapEntry(K theKey, V theValue) {
        key = theKey;
        value = theValue;
    }
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); 
        }
    }
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof Map.Entry) {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) object;
            return (key == null ? entry.getKey() == null : key.equals(entry
                    .getKey()))
                    && (value == null ? entry.getValue() == null : value
                            .equals(entry.getValue()));
        }
        return false;
    }
    public K getKey() {
        return key;
    }
    public V getValue() {
        return value;
    }
    @Override
    public int hashCode() {
        return (key == null ? 0 : key.hashCode())
                ^ (value == null ? 0 : value.hashCode());
    }
    public V setValue(V object) {
        V result = value;
        value = object;
        return result;
    }
    @Override
    public String toString() {
        return key + "=" + value;
    }
}
