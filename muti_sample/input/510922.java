 class ContentValuesBuilder {
    private final ContentValues mContentValues;
    public ContentValuesBuilder(final ContentValues contentValues) {
        mContentValues = contentValues;
    }
    public ContentValuesBuilder put(String key, String value) {
        mContentValues.put(key, value);
        return this;
    }
    public ContentValuesBuilder put(String key, Byte value) {
        mContentValues.put(key, value);
        return this;
    }
    public ContentValuesBuilder put(String key, Short value) {
        mContentValues.put(key, value);
        return this;
    }
    public ContentValuesBuilder put(String key, Integer value) {
        mContentValues.put(key, value);
        return this;
    }
    public ContentValuesBuilder put(String key, Long value) {
        mContentValues.put(key, value);
        return this;
    }
    public ContentValuesBuilder put(String key, Float value) {
        mContentValues.put(key, value);
        return this;
    }
    public ContentValuesBuilder put(String key, Double value) {
        mContentValues.put(key, value);
        return this;
    }
    public ContentValuesBuilder put(String key, Boolean value) {
        mContentValues.put(key, value);
        return this;
    }
    public ContentValuesBuilder put(String key, byte[] value) {
        mContentValues.put(key, value);
        return this;
    }
    public ContentValuesBuilder putNull(String key) {
        mContentValues.putNull(key);
        return this;
    }
}
