class PrimaryKeyEncoderActivationContextEncoder<T, K extends Serializable> implements ActivationContextEncoder<T> {
    private final Class<K> primaryKeyType;
    private final PrimaryKeyEncoder<K, T> primaryKeyEncoder;
    @SuppressWarnings("unchecked")
    public PrimaryKeyEncoderActivationContextEncoder(PrimaryKeyEncoder<K, T> primaryKeyEncoder, Class<K> primaryKeyType) {
        if (primaryKeyType == null) {
            throw new IllegalArgumentException("Parameter primaryKeyType cannot be null");
        }
        if (primaryKeyEncoder == null) {
            throw new IllegalArgumentException("Parameter primaryKeyEncoder cannot be null");
        }
        this.primaryKeyEncoder = primaryKeyEncoder;
        this.primaryKeyType = primaryKeyType;
    }
    public Object toActivationContext(T object) {
        return primaryKeyEncoder.toKey(object);
    }
    public T toObject(EventContext value) {
        return primaryKeyEncoder.toValue(value.get(primaryKeyType, 0));
    }
}
