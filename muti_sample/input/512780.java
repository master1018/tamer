public final class EnumHashBiMap<K extends Enum<K>, V>
    extends AbstractBiMap<K, V> {
  private transient Class<K> keyType;
  public static <K extends Enum<K>, V> EnumHashBiMap<K, V>
      create(Class<K> keyType) {
    return new EnumHashBiMap<K, V>(keyType);
  }
  public static <K extends Enum<K>, V> EnumHashBiMap<K, V>
      create(Map<K, ? extends V> map) {
    EnumHashBiMap<K, V> bimap = create(EnumBiMap.inferKeyType(map));
    bimap.putAll(map);
    return bimap;
  }
  private EnumHashBiMap(Class<K> keyType) {
    super(new EnumMap<K, V>(keyType), Maps.<V, K>newHashMapWithExpectedSize(
        keyType.getEnumConstants().length));
    this.keyType = keyType;
  }
  @Override public V put(K key, @Nullable V value) {
    return super.put(key, value);
  }
  @Override public V forcePut(K key, @Nullable V value) {
    return super.forcePut(key, value);
  }
  public Class<K> keyType() {
    return keyType;
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    stream.writeObject(keyType);
    Serialization.writeMap(this, stream);
  }
  @SuppressWarnings("unchecked") 
  private void readObject(ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    keyType = (Class<K>) stream.readObject();
    setDelegates(new EnumMap<K, V>(keyType),
        new HashMap<V, K>(keyType.getEnumConstants().length * 3 / 2));
    Serialization.populateMap(this, stream);
  }
  private static final long serialVersionUID = 0;
}
