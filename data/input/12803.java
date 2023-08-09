final class PropMap implements SortedMap<Object, Object>  {
    private final TreeMap<Object, Object> theMap = new TreeMap<>();;
    private final List<PropertyChangeListener> listenerList = new ArrayList<>(1);
    void addListener(PropertyChangeListener listener) {
        listenerList.add(listener);
    }
    void removeListener(PropertyChangeListener listener) {
        listenerList.remove(listener);
    }
    void addListeners(ArrayList<PropertyChangeListener> listeners) {
        listenerList.addAll(listeners);
    }
    void removeListeners(ArrayList<PropertyChangeListener> listeners) {
        listenerList.removeAll(listeners);
    }
    public Object put(Object key, Object value) {
        Object oldValue = theMap.put(key, value);
        if (value != oldValue && !listenerList.isEmpty()) {
            PropertyChangeEvent event =
                new PropertyChangeEvent(this, (String) key,
                                        oldValue, value);
            for (PropertyChangeListener listener : listenerList) {
                listener.propertyChange(event);
            }
        }
        return oldValue;
    }
    private static Map<Object, Object> defaultProps;
    static {
        Properties props = new Properties();
        props.put(Utils.DEBUG_DISABLE_NATIVE,
                  String.valueOf(Boolean.getBoolean(Utils.DEBUG_DISABLE_NATIVE)));
        props.put(Utils.DEBUG_VERBOSE,
                  String.valueOf(Integer.getInteger(Utils.DEBUG_VERBOSE,0)));
        props.put(Utils.PACK_DEFAULT_TIMEZONE,
                  String.valueOf(Boolean.getBoolean(Utils.PACK_DEFAULT_TIMEZONE)));
        props.put(Pack200.Packer.SEGMENT_LIMIT, "-1");
        props.put(Pack200.Packer.KEEP_FILE_ORDER, Pack200.Packer.TRUE);
        props.put(Pack200.Packer.MODIFICATION_TIME, Pack200.Packer.KEEP);
        props.put(Pack200.Packer.DEFLATE_HINT, Pack200.Packer.KEEP);
        props.put(Pack200.Packer.UNKNOWN_ATTRIBUTE, Pack200.Packer.PASS);
        props.put(Pack200.Packer.EFFORT, "5");
        String propFile = "intrinsic.properties";
        try (InputStream propStr = PackerImpl.class.getResourceAsStream(propFile)) {
            if (propStr == null) {
                throw new RuntimeException(propFile + " cannot be loaded");
            }
            props.load(propStr);
        } catch (IOException ee) {
            throw new RuntimeException(ee);
        }
        for (Map.Entry<Object, Object> e : props.entrySet()) {
            String key = (String) e.getKey();
            String val = (String) e.getValue();
            if (key.startsWith("attribute.")) {
                e.setValue(Attribute.normalizeLayoutString(val));
            }
        }
        defaultProps = (new HashMap<>(props));  
    }
    PropMap() {
        theMap.putAll(defaultProps);
    }
    SortedMap<Object, Object> prefixMap(String prefix) {
        int len = prefix.length();
        if (len == 0)
            return this;
        char nextch = (char)(prefix.charAt(len-1) + 1);
        String limit = prefix.substring(0, len-1)+nextch;
        return subMap(prefix, limit);
    }
    String getProperty(String s) {
        return (String) get(s);
    }
    String getProperty(String s, String defaultVal) {
        String val = getProperty(s);
        if (val == null)
            return defaultVal;
        return val;
    }
    String setProperty(String s, String val) {
        return (String) put(s, val);
    }
    List getProperties(String prefix) {
        Collection<Object> values = prefixMap(prefix).values();
        List<Object> res = new ArrayList<>(values.size());
        res.addAll(values);
        while (res.remove(null));
        return res;
    }
    private boolean toBoolean(String val) {
        return Boolean.valueOf(val).booleanValue();
    }
    boolean getBoolean(String s) {
        return toBoolean(getProperty(s));
    }
    boolean setBoolean(String s, boolean val) {
        return toBoolean(setProperty(s, String.valueOf(val)));
    }
    int toInteger(String val) {
        if (val == null)  return 0;
        if (Pack200.Packer.TRUE.equals(val))   return 1;
        if (Pack200.Packer.FALSE.equals(val))  return 0;
        return Integer.parseInt(val);
    }
    int getInteger(String s) {
        return toInteger(getProperty(s));
    }
    int setInteger(String s, int val) {
        return toInteger(setProperty(s, String.valueOf(val)));
    }
    long toLong(String val) {
        try {
            return val == null ? 0 : Long.parseLong(val);
        } catch (java.lang.NumberFormatException nfe) {
            throw new IllegalArgumentException("Invalid value");
        }
    }
    long getLong(String s) {
        return toLong(getProperty(s));
    }
    long setLong(String s, long val) {
        return toLong(setProperty(s, String.valueOf(val)));
    }
    int getTime(String s) {
        String sval = getProperty(s, "0");
        if (Utils.NOW.equals(sval)) {
            return (int)((System.currentTimeMillis()+500)/1000);
        }
        long lval = toLong(sval);
        final long recentSecondCount = 1000000000;
        if (lval < recentSecondCount*10 && !"0".equals(sval))
            Utils.log.warning("Supplied modtime appears to be seconds rather than milliseconds: "+sval);
        return (int)((lval+500)/1000);
    }
    void list(PrintStream out) {
        PrintWriter outw = new PrintWriter(out);
        list(outw);
        outw.flush();
    }
    void list(PrintWriter out) {
        out.println("#"+Utils.PACK_ZIP_ARCHIVE_MARKER_COMMENT+"[");
        Set defaults = defaultProps.entrySet();
        for (Map.Entry e : theMap.entrySet()) {
            if (defaults.contains(e))  continue;
            out.println("  " + e.getKey() + " = " + e.getValue());
        }
        out.println("#]");
    }
    @Override
    public int size() {
        return theMap.size();
    }
    @Override
    public boolean isEmpty() {
        return theMap.isEmpty();
    }
    @Override
    public boolean containsKey(Object key) {
        return theMap.containsKey(key);
    }
    @Override
    public boolean containsValue(Object value) {
        return theMap.containsValue(value);
    }
    @Override
    public Object get(Object key) {
        return theMap.get(key);
    }
    @Override
    public Object remove(Object key) {
       return theMap.remove(key);
    }
    @Override
    @SuppressWarnings("unchecked")
    public void putAll(Map m) {
       theMap.putAll(m);
    }
    @Override
    public void clear() {
        theMap.clear();
    }
    @Override
    public Set<Object> keySet() {
       return theMap.keySet();
    }
    @Override
    public Collection<Object> values() {
       return theMap.values();
    }
    @Override
    public Set<Map.Entry<Object, Object>> entrySet() {
        return theMap.entrySet();
    }
    @Override
    @SuppressWarnings("unchecked")
    public Comparator<Object> comparator() {
        return (Comparator<Object>) theMap.comparator();
    }
    @Override
    public SortedMap<Object, Object> subMap(Object fromKey, Object toKey) {
        return theMap.subMap(fromKey, toKey);
    }
    @Override
    public SortedMap<Object, Object> headMap(Object toKey) {
        return theMap.headMap(toKey);
    }
    @Override
    public SortedMap<Object, Object> tailMap(Object fromKey) {
        return theMap.tailMap(fromKey);
    }
    @Override
    public Object firstKey() {
        return theMap.firstKey();
    }
    @Override
    public Object lastKey() {
       return theMap.lastKey();
    }
}
