public class TabularDataSupport
    implements TabularData, Map<Object,Object>,
               Cloneable, Serializable {
    static final long serialVersionUID = 5720150593236309827L;
    private Map<Object,CompositeData> dataMap;
    private final TabularType tabularType;
    private transient String[] indexNamesArray;
    public TabularDataSupport(TabularType tabularType) {
        this(tabularType, 16, 0.75f);
    }
    public TabularDataSupport(TabularType tabularType, int initialCapacity, float loadFactor) {
        if (tabularType == null) {
            throw new IllegalArgumentException("Argument tabularType cannot be null.");
        }
        this.tabularType = tabularType;
        List<String> tmpNames = tabularType.getIndexNames();
        this.indexNamesArray = tmpNames.toArray(new String[tmpNames.size()]);
        String useHashMapProp = AccessController.doPrivileged(
                new GetPropertyAction("jmx.tabular.data.hash.map"));
        boolean useHashMap = "true".equalsIgnoreCase(useHashMapProp);
        this.dataMap = useHashMap ?
            new HashMap<Object,CompositeData>(initialCapacity, loadFactor) :
            new LinkedHashMap<Object, CompositeData>(initialCapacity, loadFactor);
    }
    public TabularType getTabularType() {
        return tabularType;
    }
    public Object[] calculateIndex(CompositeData value) {
        checkValueType(value);
        return internalCalculateIndex(value).toArray();
    }
    public boolean containsKey(Object key) {
        Object[] k;
        try {
            k = (Object[]) key;
        } catch (ClassCastException e) {
            return false;
        }
        return  this.containsKey(k);
    }
    public boolean containsKey(Object[] key) {
        return  ( key == null ? false : dataMap.containsKey(Arrays.asList(key)));
    }
    public boolean containsValue(CompositeData value) {
        return dataMap.containsValue(value);
    }
    public boolean containsValue(Object value) {
        return dataMap.containsValue(value);
    }
    public Object get(Object key) {
        return get((Object[]) key);
    }
    public CompositeData get(Object[] key) {
        checkKeyType(key);
        return dataMap.get(Arrays.asList(key));
    }
    public Object put(Object key, Object value) {
        internalPut((CompositeData) value);
        return value; 
    }
    public void put(CompositeData value) {
        internalPut(value);
    }
    private CompositeData internalPut(CompositeData value) {
        List<?> index = checkValueAndIndex(value);
        return dataMap.put(index, value);
    }
    public Object remove(Object key) {
        return remove((Object[]) key);
    }
    public CompositeData remove(Object[] key) {
        checkKeyType(key);
        return dataMap.remove(Arrays.asList(key));
    }
    public void putAll(Map<?,?> t) {
        if ( (t == null) || (t.size() == 0) ) {
            return;
        }
        CompositeData[] values;
        try {
            values =
                t.values().toArray(new CompositeData[t.size()]);
        } catch (java.lang.ArrayStoreException e) {
            throw new ClassCastException("Map argument t contains values which are not instances of <tt>CompositeData</tt>");
        }
        putAll(values);
    }
    public void putAll(CompositeData[] values) {
        if ( (values == null) || (values.length == 0) ) {
            return;
        }
        List<List<?>> indexes =
            new ArrayList<List<?>>(values.length + 1);
        List<?> index;
        for (int i=0; i<values.length; i++) {
            index = checkValueAndIndex(values[i]);
            if (indexes.contains(index)) {
                throw new KeyAlreadyExistsException("Argument elements values["+ i +"] and values["+ indexes.indexOf(index) +
                                                    "] have the same indexes, "+
                                                    "calculated according to this TabularData instance's tabularType.");
            }
            indexes.add(index);
        }
        for (int i=0; i<values.length; i++) {
            dataMap.put(indexes.get(i), values[i]);
        }
    }
    public void clear() {
        dataMap.clear();
    }
    public int size() {
        return dataMap.size();
    }
    public boolean isEmpty() {
        return (this.size() == 0);
    }
    public Set<Object> keySet() {
        return dataMap.keySet() ;
    }
    @SuppressWarnings("unchecked")  
    public Collection<Object> values() {
        return Util.cast(dataMap.values());
    }
    @SuppressWarnings("unchecked")  
    public Set<Map.Entry<Object,Object>> entrySet() {
        return Util.cast(dataMap.entrySet());
    }
    public Object clone() {
        try {
            TabularDataSupport c = (TabularDataSupport) super.clone();
            c.dataMap = new HashMap<Object,CompositeData>(c.dataMap);
            return c;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        TabularData other;
        try {
            other = (TabularData) obj;
        } catch (ClassCastException e) {
            return false;
        }
        if ( ! this.getTabularType().equals(other.getTabularType()) ) {
            return false;
        }
        if (this.size() != other.size()) {
            return false;
        }
        for (CompositeData value : dataMap.values()) {
            if ( ! other.containsValue(value) ) {
                return false;
            }
        }
        return true;
    }
   public int hashCode() {
        int result = 0;
        result += this.tabularType.hashCode();
        for (Object value : values())
            result += value.hashCode();
        return result;
    }
    public String toString() {
        return new StringBuilder()
            .append(this.getClass().getName())
            .append("(tabularType=")
            .append(tabularType.toString())
            .append(",contents=")
            .append(dataMap.toString())
            .append(")")
            .toString();
    }
    private List<?> internalCalculateIndex(CompositeData value) {
        return Collections.unmodifiableList(Arrays.asList(value.getAll(this.indexNamesArray)));
    }
    private void checkKeyType(Object[] key) {
        if ( (key == null) || (key.length == 0) ) {
            throw new NullPointerException("Argument key cannot be null or empty.");
        }
        if (key.length != this.indexNamesArray.length) {
            throw new InvalidKeyException("Argument key's length="+ key.length +
                                          " is different from the number of item values, which is "+ indexNamesArray.length +
                                          ", specified for the indexing rows in this TabularData instance.");
        }
        OpenType<?> keyElementType;
        for (int i=0; i<key.length; i++) {
            keyElementType = tabularType.getRowType().getType(this.indexNamesArray[i]);
            if ( (key[i] != null) && (! keyElementType.isValue(key[i])) ) {
                throw new InvalidKeyException("Argument element key["+ i +"] is not a value for the open type expected for "+
                                              "this element of the index, whose name is \""+ indexNamesArray[i] +
                                              "\" and whose open type is "+ keyElementType);
            }
        }
    }
    private void checkValueType(CompositeData value) {
        if (value == null) {
            throw new NullPointerException("Argument value cannot be null.");
        }
        if (!tabularType.getRowType().isValue(value)) {
            throw new InvalidOpenTypeException("Argument value's composite type ["+ value.getCompositeType() +
                                               "] is not assignable to "+
                                               "this TabularData instance's row type ["+ tabularType.getRowType() +"].");
        }
    }
    private List<?> checkValueAndIndex(CompositeData value) {
        checkValueType(value);
        List<?> index = internalCalculateIndex(value);
        if (dataMap.containsKey(index)) {
            throw new KeyAlreadyExistsException("Argument value's index, calculated according to this TabularData "+
                                                "instance's tabularType, already refers to a value in this table.");
        }
        return index;
    }
    private void readObject(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
      in.defaultReadObject();
      List<String> tmpNames = tabularType.getIndexNames();
      indexNamesArray = tmpNames.toArray(new String[tmpNames.size()]);
    }
}
