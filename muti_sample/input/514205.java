public class TreeMapPerformanceTest extends PerformanceTestBase {
    public static final int ITERATIONS = 1000;
    public static TreeMap<String, Integer> sMap;
    public static String[] sKeys;
    @Override
    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        sMap = new TreeMap();
        sKeys = new String[ITERATIONS];
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            sKeys[i] = Integer.toString(i, 16);
            sMap.put(sKeys[i], i);
        }
    }
    @Override
    public int startPerformance(PerformanceTestCase.Intermediates intermediates) {
        intermediates.setInternalIterations(ITERATIONS);
        return 0;
    }
    @SuppressWarnings("unchecked")
    public void testTreeMapPut() {
        TreeMap map = new TreeMap();
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            map.put(i, i);
            map.put(i, i);
            map.put(i, i);
            map.put(i, i);
            map.put(i, i);
            map.put(i, i);
            map.put(i, i);
            map.put(i, i);
            map.put(i, i);
            map.put(i, i);
        }
    }
    public void testTreeMapGet() {
        int value;
        TreeMap<String, Integer> map = sMap;
        String[] keys = sKeys;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            value = map.get(keys[i]);
            value = map.get(keys[i]);
            value = map.get(keys[i]);
            value = map.get(keys[i]);
            value = map.get(keys[i]);
            value = map.get(keys[i]);
            value = map.get(keys[i]);
            value = map.get(keys[i]);
            value = map.get(keys[i]);
            value = map.get(keys[i]);
        }
    }
    public void testTreeMapFirstKey() {
        String key;
        TreeMap<String, Integer> map = sMap;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            key = map.firstKey();
            key = map.firstKey();
            key = map.firstKey();
            key = map.firstKey();
            key = map.firstKey();
            key = map.firstKey();
            key = map.firstKey();
            key = map.firstKey();
            key = map.firstKey();
            key = map.firstKey();
        }
    }
    public void testTreeMapKeySet() {
        Set keyset;
        TreeMap<String, Integer> map = sMap;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            keyset = map.keySet();
            keyset = map.keySet();
            keyset = map.keySet();
            keyset = map.keySet();
            keyset = map.keySet();
            keyset = map.keySet();
            keyset = map.keySet();
            keyset = map.keySet();
            keyset = map.keySet();
            keyset = map.keySet();
        }
    }
    public void testTreeMapEntrySet() {
        Set keyset;
        TreeMap<String, Integer> map = sMap;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            keyset = map.entrySet();
            keyset = map.entrySet();
            keyset = map.entrySet();
            keyset = map.entrySet();
            keyset = map.entrySet();
            keyset = map.entrySet();
            keyset = map.entrySet();
            keyset = map.entrySet();
            keyset = map.entrySet();
            keyset = map.entrySet();
        }
    }
    public void testTreeMapValues() {
        Collection collection;
        TreeMap<String, Integer> map = sMap;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            collection = map.values();
            collection = map.values();
            collection = map.values();
            collection = map.values();
            collection = map.values();
            collection = map.values();
            collection = map.values();
            collection = map.values();
            collection = map.values();
            collection = map.values();
        }
    }
    public void testTreeMapSize() {
        int len;
        TreeMap<String, Integer> map = sMap;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            len = map.size();
            len = map.size();
            len = map.size();
            len = map.size();
            len = map.size();
            len = map.size();
            len = map.size();
            len = map.size();
            len = map.size();
            len = map.size();
        }
    }
    public void testTreeMapContainsKey() {
        boolean flag;
        String key = sKeys[525];
        TreeMap<String, Integer> map = sMap;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            flag = map.containsKey(key);
            flag = map.containsKey(key);
            flag = map.containsKey(key);
            flag = map.containsKey(key);
            flag = map.containsKey(key);
            flag = map.containsKey(key);
            flag = map.containsKey(key);
            flag = map.containsKey(key);
            flag = map.containsKey(key);
            flag = map.containsKey(key);
        }
    }
    public void testTreeMapContainsValue() {
        boolean flag;
        TreeMap<String, Integer> map = sMap;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            flag = map.containsValue(i);
            flag = map.containsValue(i);
            flag = map.containsValue(i);
            flag = map.containsValue(i);
            flag = map.containsValue(i);
            flag = map.containsValue(i);
            flag = map.containsValue(i);
            flag = map.containsValue(i);
            flag = map.containsValue(i);
            flag = map.containsValue(i);
        }
    }
    public void testTreeMapHeadMap() {
        SortedMap map;
        String str = sKeys[100];
        TreeMap<String, Integer> tMap = sMap;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            map = tMap.headMap(str);
            map = tMap.headMap(str);
            map = tMap.headMap(str);
            map = tMap.headMap(str);
            map = tMap.headMap(str);
            map = tMap.headMap(str);
            map = tMap.headMap(str);
            map = tMap.headMap(str);
            map = tMap.headMap(str);
            map = tMap.headMap(str);
        }
    }
    public void testTreeMapSubMap() {
        String str1 = sKeys[400];
        String str2 = sKeys[500];
        SortedMap map;
        TreeMap<String, Integer> tMap = sMap;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            map = tMap.subMap(str1, str2);
            map = tMap.subMap(str1, str2);
            map = tMap.subMap(str1, str2);
            map = tMap.subMap(str1, str2);
            map = tMap.subMap(str1, str2);
            map = tMap.subMap(str1, str2);
            map = tMap.subMap(str1, str2);
            map = tMap.subMap(str1, str2);
            map = tMap.subMap(str1, str2);
            map = tMap.subMap(str1, str2);
        }
    }
    public void testTreeMapTailMap() {
        String str = sKeys[900];
        TreeMap<String, Integer> tMap = sMap;
        SortedMap map;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            map = tMap.tailMap(str);
            map = tMap.tailMap(str);
            map = tMap.tailMap(str);
            map = tMap.tailMap(str);
            map = tMap.tailMap(str);
            map = tMap.tailMap(str);
            map = tMap.tailMap(str);
            map = tMap.tailMap(str);
            map = tMap.tailMap(str);
            map = tMap.tailMap(str);
        }
    }
    @SuppressWarnings("unchecked")
    public void testTreeMapRemove() {
        TreeMap<String, Integer> tMap = new TreeMap(sMap);
        String[] keys = sKeys;
        for (int i = ITERATIONS - 1; i >= 0; i--) {
            tMap.remove(keys[i]);
            tMap.remove(keys[i]);
            tMap.remove(keys[i]);
            tMap.remove(keys[i]);
            tMap.remove(keys[i]);
            tMap.remove(keys[i]);
            tMap.remove(keys[i]);
            tMap.remove(keys[i]);
            tMap.remove(keys[i]);
            tMap.remove(keys[i]);
        }
    }
}
