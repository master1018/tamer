public final class EnumBiMap<K extends Enum<K>, V extends Enum<V>>
    extends AbstractBiMap<K, V> {
  private transient Class<K> keyType;
  private transient Class<V> valueType;
  public static <K extends Enum<K>, V extends Enum<V>> EnumBiMap<K, V>
      create(Class<K> keyType, Class<V> valueType) {
    return new EnumBiMap<K, V>(keyType, valueType);
  }
  public static <K extends Enum<K>, V extends Enum<V>> EnumBiMap<K, V>
      create(Map<K, V> map) {
    EnumBiMap<K, V> bimap = create(inferKeyType(map), inferValueType(map));
    bimap.putAll(map);
    return bimap;
  }
  private EnumBiMap(Class<K> keyType, Class<V> valueType) {
    super(new EnumMap<K, V>(keyType), new EnumMap<V, K>(valueType));
    this.keyType = keyType;
    this.valueType = valueType;
  }
  static <K extends Enum<K>> Class<K> inferKeyType(Map<K, ?> map) {
    if (map instanceof EnumBiMap) {
      return ((EnumBiMap<K, ?>) map).keyType();
    }
    if (map instanceof EnumHashBiMap) {
      return ((EnumHashBiMap<K, ?>) map).keyType();
    }
    checkArgument(!map.isEmpty());
    return map.keySet().iterator().next().getDeclaringClass();
  }
  private static <V extends Enum<V>> Class<V> inferValueType(Map<?, V> map) {
    if (map instanceof EnumBiMap) {
      return ((EnumBiMap<?, V>) map).valueType;
    }
    checkArgument(!map.isEmpty());
    return map.values().iterator().next().getDeclaringClass();
  }
  public Class<K> keyType() {
    return keyType;
  }
  public Class<V> valueType() {
    return valueType;
  }
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    stream.writeObject(keyType);
    stream.writeObject(valueType);
    Serialization.writeMap(this, stream);
  }
  @SuppressWarnings("unchecked") 
  private void readObject(ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    keyType = (Class<K>) stream.readObject();
    valueType = (Class<V>) stream.readObject();
    setDelegates(new EnumMap<K, V>(keyType), new EnumMap<V, K>(valueType));
    Serialization.populateMap(this, stream);
  }
  private static final long serialVersionUID = 0;
}
