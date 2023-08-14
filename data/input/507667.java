final class Serialization {
  private Serialization() {}
  @GwtIncompatible("java.io.ObjectInputStream")
  public static int readCount(ObjectInputStream stream) throws IOException {
    return stream.readInt();
  }
  @GwtIncompatible("java.io.ObjectOutputStream")
  public static <K, V> void writeMap(Map<K, V> map, ObjectOutputStream stream)
      throws IOException {
    stream.writeInt(map.size());
    for (Map.Entry<K, V> entry : map.entrySet()) {
      stream.writeObject(entry.getKey());
      stream.writeObject(entry.getValue());
    }
  }
  @GwtIncompatible("java.io.ObjectInputStream")
  public static <K, V> void populateMap(Map<K, V> map, ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
    int size = stream.readInt();
    populateMap(map, stream, size);
  }
  @GwtIncompatible("java.io.ObjectInputStream")
  public static <K, V> void populateMap(Map<K, V> map, ObjectInputStream stream,
      int size) throws IOException, ClassNotFoundException {
    for (int i = 0; i < size; i++) {
      @SuppressWarnings("unchecked") 
      K key = (K) stream.readObject();
      @SuppressWarnings("unchecked") 
      V value = (V) stream.readObject();
      map.put(key, value);
    }
  }
  @GwtIncompatible("java.io.ObjectOutputStream")
  public static <E> void writeMultiset(
      Multiset<E> multiset, ObjectOutputStream stream) throws IOException {
    int entryCount = multiset.entrySet().size();
    stream.writeInt(entryCount);
    for (Multiset.Entry<E> entry : multiset.entrySet()) {
      stream.writeObject(entry.getElement());
      stream.writeInt(entry.getCount());
    }
  }
  @GwtIncompatible("java.io.ObjectInputStream")
  public static <E> void populateMultiset(
      Multiset<E> multiset, ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
    int distinctElements = stream.readInt();
    populateMultiset(multiset, stream, distinctElements);
  }
  @GwtIncompatible("java.io.ObjectInputStream")
  public static <E> void populateMultiset(
      Multiset<E> multiset, ObjectInputStream stream, int distinctElements)
      throws IOException, ClassNotFoundException {
    for (int i = 0; i < distinctElements; i++) {
      @SuppressWarnings("unchecked") 
      E element = (E) stream.readObject();
      int count = stream.readInt();
      multiset.add(element, count);
    }
  }
  @GwtIncompatible("java.io.ObjectOutputStream")
  public static <K, V> void writeMultimap(
      Multimap<K, V> multimap, ObjectOutputStream stream) throws IOException {
    stream.writeInt(multimap.asMap().size());
    for (Map.Entry<K, Collection<V>> entry : multimap.asMap().entrySet()) {
      stream.writeObject(entry.getKey());
      stream.writeInt(entry.getValue().size());
      for (V value : entry.getValue()) {
        stream.writeObject(value);
      }
    }
  }
  @GwtIncompatible("java.io.ObjectInputStream")
  public static <K, V> void populateMultimap(
      Multimap<K, V> multimap, ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
    int distinctKeys = stream.readInt();
    populateMultimap(multimap, stream, distinctKeys);
  }
  @GwtIncompatible("java.io.ObjectInputStream")
  public static <K, V> void populateMultimap(
      Multimap<K, V> multimap, ObjectInputStream stream, int distinctKeys)
      throws IOException, ClassNotFoundException {
    for (int i = 0; i < distinctKeys; i++) {
      @SuppressWarnings("unchecked") 
      K key = (K) stream.readObject();
      Collection<V> values = multimap.get(key);
      int valueCount = stream.readInt();
      for (int j = 0; j < valueCount; j++) {
        @SuppressWarnings("unchecked") 
        V value = (V) stream.readObject();
        values.add(value);
      }
    }
  }
  @GwtIncompatible("java.lang.reflect.Field")
  static <T> FieldSetter<T> getFieldSetter(
      final Class<T> clazz, String fieldName) {
    try {
      Field field = clazz.getDeclaredField(fieldName);
      return new FieldSetter<T>(field);
    } catch (NoSuchFieldException e) {
      throw new AssertionError(e); 
    }
  }
  @GwtCompatible(emulated = true) 
  static final class FieldSetter<T> {
    private final Field field;
    private FieldSetter(Field field) {
      this.field = field;
      field.setAccessible(true);
    }
    @GwtIncompatible("java.lang.reflect.Field")
    void set(T instance, Object value) {
      try {
        field.set(instance, value);
      } catch (IllegalAccessException impossible) {
        throw new AssertionError(impossible);
      }
    }
    @GwtIncompatible("java.lang.reflect.Field")
    void set(T instance, int value) {
      try {
        field.set(instance, value);
      } catch (IllegalAccessException impossible) {
        throw new AssertionError(impossible);
      }
    }
  }
}
