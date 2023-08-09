public abstract class WeakClassHashMap<V> {
    private Map<Class<?>,ValueCell<V>> internalMap =
        new WeakHashMap<Class<?>,ValueCell<V>>();
    protected WeakClassHashMap() { }
    public V get(Class<?> remoteClass) {
        ValueCell<V> valueCell;
        synchronized (internalMap) {
            valueCell = internalMap.get(remoteClass);
            if (valueCell == null) {
                valueCell = new ValueCell<V>();
                internalMap.put(remoteClass, valueCell);
            }
        }
        synchronized (valueCell) {
            V value = null;
            if (valueCell.ref != null) {
                value = valueCell.ref.get();
            }
            if (value == null) {
                value = computeValue(remoteClass);
                valueCell.ref = new SoftReference<V>(value);
            }
            return value;
        }
    }
    protected abstract V computeValue(Class<?> remoteClass);
    private static class ValueCell<T> {
        Reference<T> ref = null;
        ValueCell() { }
    }
}
