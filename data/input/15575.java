public class CompositeDataSupport
    implements CompositeData, Serializable {
    static final long serialVersionUID = 8003518976613702244L;
    private final SortedMap<String, Object> contents;
    private final CompositeType compositeType;
    public CompositeDataSupport(
            CompositeType compositeType, String[] itemNames, Object[] itemValues)
            throws OpenDataException {
        this(makeMap(itemNames, itemValues), compositeType);
    }
    private static SortedMap<String, Object> makeMap(
            String[] itemNames, Object[] itemValues)
            throws OpenDataException {
        if (itemNames == null || itemValues == null)
            throw new IllegalArgumentException("Null itemNames or itemValues");
        if (itemNames.length == 0 || itemValues.length == 0)
            throw new IllegalArgumentException("Empty itemNames or itemValues");
        if (itemNames.length != itemValues.length) {
            throw new IllegalArgumentException(
                    "Different lengths: itemNames[" + itemNames.length +
                    "], itemValues[" + itemValues.length + "]");
        }
        SortedMap<String, Object> map = new TreeMap<String, Object>();
        for (int i = 0; i < itemNames.length; i++) {
            String name = itemNames[i];
            if (name == null || name.equals(""))
                throw new IllegalArgumentException("Null or empty item name");
            if (map.containsKey(name))
                throw new OpenDataException("Duplicate item name " + name);
            map.put(itemNames[i], itemValues[i]);
        }
        return map;
    }
    public CompositeDataSupport(CompositeType compositeType,
                                Map<String,?> items)
            throws OpenDataException {
        this(makeMap(items), compositeType);
    }
    private static SortedMap<String, Object> makeMap(Map<String, ?> items) {
        if (items == null || items.isEmpty())
            throw new IllegalArgumentException("Null or empty items map");
        SortedMap<String, Object> map = new TreeMap<String, Object>();
        for (Object key : items.keySet()) {
            if (key == null || key.equals(""))
                throw new IllegalArgumentException("Null or empty item name");
            if (!(key instanceof String)) {
                throw new ArrayStoreException("Item name is not string: " + key);
            }
            map.put((String) key, items.get(key));
        }
        return map;
    }
    private CompositeDataSupport(
            SortedMap<String, Object> items, CompositeType compositeType)
            throws OpenDataException {
        if (compositeType == null) {
            throw new IllegalArgumentException("Argument compositeType cannot be null.");
        }
        Set<String> namesFromType = compositeType.keySet();
        Set<String> namesFromItems = items.keySet();
        if (!namesFromType.equals(namesFromItems)) {
            Set<String> extraFromType = new TreeSet<String>(namesFromType);
            extraFromType.removeAll(namesFromItems);
            Set<String> extraFromItems = new TreeSet<String>(namesFromItems);
            extraFromItems.removeAll(namesFromType);
            if (!extraFromType.isEmpty() || !extraFromItems.isEmpty()) {
                throw new OpenDataException(
                        "Item names do not match CompositeType: " +
                        "names in items but not in CompositeType: " + extraFromItems +
                        "; names in CompositeType but not in items: " + extraFromType);
            }
        }
        for (String name : namesFromType) {
            Object value = items.get(name);
            if (value != null) {
                OpenType<?> itemType = compositeType.getType(name);
                if (!itemType.isValue(value)) {
                    throw new OpenDataException(
                            "Argument value of wrong type for item " + name +
                            ": value " + value + ", type " + itemType);
                }
            }
        }
        this.compositeType = compositeType;
        this.contents = items;
    }
    public CompositeType getCompositeType() {
        return compositeType;
    }
    public Object get(String key) {
        if ( (key == null) || (key.trim().equals("")) ) {
            throw new IllegalArgumentException("Argument key cannot be a null or empty String.");
        }
        if ( ! contents.containsKey(key.trim())) {
            throw new InvalidKeyException("Argument key=\""+ key.trim() +"\" is not an existing item name for this CompositeData instance.");
        }
        return contents.get(key.trim());
    }
    public Object[] getAll(String[] keys) {
        if ( (keys == null) || (keys.length == 0) ) {
            return new Object[0];
        }
        Object[] results = new Object[keys.length];
        for (int i=0; i<keys.length; i++) {
            results[i] = this.get(keys[i]);
        }
        return results;
    }
    public boolean containsKey(String key) {
        if ( (key == null) || (key.trim().equals("")) ) {
            return false;
        }
        return contents.containsKey(key);
    }
    public boolean containsValue(Object value) {
        return contents.containsValue(value);
    }
    public Collection<?> values() {
        return Collections.unmodifiableCollection(contents.values());
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof CompositeData)) {
            return false;
        }
        CompositeData other = (CompositeData) obj;
        if (!this.getCompositeType().equals(other.getCompositeType()) ) {
            return false;
        }
        if (contents.size() != other.values().size()) {
            return false;
        }
        for (Map.Entry<String,Object> entry : contents.entrySet()) {
            Object e1 = entry.getValue();
            Object e2 = other.get(entry.getKey());
            if (e1 == e2)
                continue;
            if (e1 == null)
                return false;
            boolean eq = e1.getClass().isArray() ?
                Arrays.deepEquals(new Object[] {e1}, new Object[] {e2}) :
                e1.equals(e2);
            if (!eq)
                return false;
        }
        return true;
    }
    @Override
    public int hashCode() {
        int hashcode = compositeType.hashCode();
        for (Object o : contents.values()) {
            if (o instanceof Object[])
                hashcode += Arrays.deepHashCode((Object[]) o);
            else if (o instanceof byte[])
                hashcode += Arrays.hashCode((byte[]) o);
            else if (o instanceof short[])
                hashcode += Arrays.hashCode((short[]) o);
            else if (o instanceof int[])
                hashcode += Arrays.hashCode((int[]) o);
            else if (o instanceof long[])
                hashcode += Arrays.hashCode((long[]) o);
            else if (o instanceof char[])
                hashcode += Arrays.hashCode((char[]) o);
            else if (o instanceof float[])
                hashcode += Arrays.hashCode((float[]) o);
            else if (o instanceof double[])
                hashcode += Arrays.hashCode((double[]) o);
            else if (o instanceof boolean[])
                hashcode += Arrays.hashCode((boolean[]) o);
            else if (o != null)
                hashcode += o.hashCode();
        }
        return hashcode;
    }
    @Override
    public String toString() {
        return new StringBuilder()
            .append(this.getClass().getName())
            .append("(compositeType=")
            .append(compositeType.toString())
            .append(",contents=")
            .append(contentString())
            .append(")")
            .toString();
    }
    private String contentString() {
        StringBuilder sb = new StringBuilder("{");
        String sep = "";
        for (Map.Entry<String, Object> entry : contents.entrySet()) {
            sb.append(sep).append(entry.getKey()).append("=");
            String s = Arrays.deepToString(new Object[] {entry.getValue()});
            sb.append(s.substring(1, s.length() - 1));
            sep = ", ";
        }
        sb.append("}");
        return sb.toString();
    }
}
