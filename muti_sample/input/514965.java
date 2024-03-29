@TestTargetClass(LinkedHashMap.class)
public class LinkedHashMapTest extends junit.framework.TestCase {
    LinkedHashMap hm;
    final static int hmSize = 1000;
    Object[] objArray;
    Object[] objArray2;
    static final class CacheMap extends LinkedHashMap {
        protected boolean removeEldestEntry(Map.Entry e) {
            return size() > 5;
        }
    }
    private static class MockMapNull extends AbstractMap {
        @Override
        public Set entrySet() {
            return null;
        }
        @Override
        public int size() {
            return 10;
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "LinkedHashMap",
        args = {}
    )
    public void test_Constructor() {
        new Support_MapTest2(new LinkedHashMap()).runTest();
        LinkedHashMap hm2 = new LinkedHashMap();
        assertEquals("Created incorrect LinkedHashMap", 0, hm2.size());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Need to improve code.",
        method = "LinkedHashMap",
        args = {int.class}
    )
    public void test_ConstructorI() {
        LinkedHashMap hm2 = new LinkedHashMap(5);
        assertEquals("Created incorrect LinkedHashMap", 0, hm2.size());
        try {
            new LinkedHashMap(-1);
            fail("Failed to throw IllegalArgumentException for initial " +
                    "capacity < 0");
        } catch (IllegalArgumentException e) {
        }
        LinkedHashMap empty = new LinkedHashMap(0);
        assertNull("Empty LinkedHashMap access", empty.get("nothing"));
        empty.put("something", "here");
        assertTrue("cannot get element", empty.get("something") == "here");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Need to improve code.",
        method = "LinkedHashMap",
        args = {int.class, float.class}
    )
    public void test_ConstructorIF() {
        LinkedHashMap hm2 = new LinkedHashMap(5, (float) 0.5);
        assertEquals("Created incorrect LinkedHashMap", 0, hm2.size());
        try {
            new LinkedHashMap(0, 0);
            fail("Failed to throw IllegalArgumentException for initial " +
                    "load factor <= 0");
        } catch (IllegalArgumentException e) {
        }
        LinkedHashMap empty = new LinkedHashMap(0, 0.75f);
        assertNull("Empty hashtable access", empty.get("nothing"));
        empty.put("something", "here");
        assertTrue("cannot get element", empty.get("something") == "here");
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify IllegalArgumentException.",
        method = "LinkedHashMap",
        args = {java.util.Map.class}
    )
    public void test_ConstructorLjava_util_Map() {
        Map myMap = new TreeMap();
        for (int counter = 0; counter < hmSize; counter++)
            myMap.put(objArray2[counter], objArray[counter]);
        LinkedHashMap hm2 = new LinkedHashMap(myMap);
        for (int counter = 0; counter < hmSize; counter++)
            assertTrue("Failed to construct correct LinkedHashMap", hm
                    .get(objArray2[counter]) == hm2.get(objArray2[counter]));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify ClassCastException, NullPointerException.",
        method = "get",
        args = {java.lang.Object.class}
    )
    public void test_getLjava_lang_Object() {
        assertNull("Get returned non-null for non existent key",
                hm.get("T"));
        hm.put("T", "HELLO");
        assertEquals("Get returned incorecct value for existing key", "HELLO", hm.get("T")
                );
        LinkedHashMap m = new LinkedHashMap();
        m.put(null, "test");
        assertEquals("Failed with null key", "test", m.get(null));
        assertNull("Failed with missing key matching null hash", m
                .get(new Integer(0)));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify that put returns null if there was no mapping for key, or associated null with the specified key.",
        method = "put",
        args = {java.lang.Object.class, java.lang.Object.class}
    )
    public void test_putLjava_lang_ObjectLjava_lang_Object() {
        hm.put("KEY", "VALUE");
        assertEquals("Failed to install key/value pair",
                "VALUE", hm.get("KEY"));
        LinkedHashMap m = new LinkedHashMap();
        m.put(new Short((short) 0), "short");
        m.put(null, "test");
        m.put(new Integer(0), "int");
        assertEquals("Failed adding to bucket containing null", "short", m.get(
                new Short((short) 0)));
        assertEquals("Failed adding to bucket containing null2", "int", m.get(
                new Integer(0)));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "test that put on an already present key causes entry to move to tail.",
        method = "put",
        args = {java.lang.Object.class, java.lang.Object.class}
    )
    public void test_putPresent() {
        Map<String, String> m = new LinkedHashMap<String, String>(8, .75f, true);
        m.put("KEY", "VALUE");
        m.put("WOMBAT", "COMBAT");
        m.put("KEY", "VALUE");
        Map.Entry newest = null;
        for (Map.Entry<String, String> e : m.entrySet()) {
            newest = e;
        }
        assertEquals("KEY", newest.getKey());
        assertEquals("VALUE", newest.getValue());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "putAll",
        args = {java.util.Map.class}
    )
    public void test_putAllLjava_util_Map() {
        LinkedHashMap hm2 = new LinkedHashMap();
        hm2.putAll(hm);
        for (int i = 0; i < 1000; i++)
            assertTrue("Failed to clear all elements", hm2.get(
                    new Integer(i).toString()).equals((new Integer(i))));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies NullPointerException.",
        method = "putAll",
        args = {java.util.Map.class}
    )
    public void test_putAll_Ljava_util_Map_Null() {
        LinkedHashMap linkedHashMap = new LinkedHashMap();
        try {
            linkedHashMap.putAll(new MockMapNull());
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
        try {
            linkedHashMap = new LinkedHashMap(new MockMapNull());
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "entrySet",
        args = {}
    )
    public void test_entrySet() {
        Set s = hm.entrySet();
        Iterator i = s.iterator();
        assertTrue("Returned set of incorrect size", hm.size() == s.size());
        while (i.hasNext()) {
            Map.Entry m = (Map.Entry) i.next();
            assertTrue("Returned incorrect entry set", hm.containsKey(m
                    .getKey())
                    && hm.containsValue(m.getValue()));
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "Test that remove removes the entry from the linked list",
        method = "entrySet",
        args = {}
    )
    public void test_entrySetRemove() {
        entrySetRemoveHelper("military", "intelligence");
        entrySetRemoveHelper(null, "hypothesis");
    }
    private void entrySetRemoveHelper(String key, String value) {
        Map<String, String> m1 = new LinkedHashMap<String, String>();
        m1.put(key, value);
        m1.put("jumbo", "shrimp");
        LinkedHashMap<String, String> m2 = new LinkedHashMap<String, String>(m1);
        Set<Map.Entry<String, String>> s1 = m1.entrySet();
        s1.remove(m2.entrySet().iterator().next());
        assertEquals("jumbo", s1.iterator().next().getKey());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "keySet",
        args = {}
    )
    public void test_keySet() {
        Set s = hm.keySet();
        assertTrue("Returned set of incorrect size()", s.size() == hm.size());
        for (int i = 0; i < objArray.length; i++)
            assertTrue("Returned set does not contain all keys", s
                    .contains(objArray[i].toString()));
        LinkedHashMap m = new LinkedHashMap();
        m.put(null, "test");
        assertTrue("Failed with null key", m.keySet().contains(null));
        assertNull("Failed with null key", m.keySet().iterator().next());
        Map map = new LinkedHashMap(101);
        map.put(new Integer(1), "1");
        map.put(new Integer(102), "102");
        map.put(new Integer(203), "203");
        Iterator it = map.keySet().iterator();
        Integer remove1 = (Integer) it.next();
        it.hasNext();
        it.remove();
        Integer remove2 = (Integer) it.next();
        it.remove();
        ArrayList list = new ArrayList(Arrays.asList(new Integer[] {
                new Integer(1), new Integer(102), new Integer(203) }));
        list.remove(remove1);
        list.remove(remove2);
        assertTrue("Wrong result", it.next().equals(list.get(0)));
        assertEquals("Wrong size", 1, map.size());
        assertTrue("Wrong contents", map.keySet().iterator().next().equals(
                list.get(0)));
        Map map2 = new LinkedHashMap(101);
        map2.put(new Integer(1), "1");
        map2.put(new Integer(4), "4");
        Iterator it2 = map2.keySet().iterator();
        Integer remove3 = (Integer) it2.next();
        Integer next;
        if (remove3.intValue() == 1)
            next = new Integer(4);
        else
            next = new Integer(1);
        it2.hasNext();
        it2.remove();
        assertTrue("Wrong result 2", it2.next().equals(next));
        assertEquals("Wrong size 2", 1, map2.size());
        assertTrue("Wrong contents 2", map2.keySet().iterator().next().equals(
                next));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "values",
        args = {}
    )
    public void test_values() {
        Collection c = hm.values();
        assertTrue("Returned collection of incorrect size()", c.size() == hm
                .size());
        for (int i = 0; i < objArray.length; i++)
            assertTrue("Returned collection does not contain all keys", c
                    .contains(objArray[i]));
        LinkedHashMap myLinkedHashMap = new LinkedHashMap();
        for (int i = 0; i < 100; i++)
            myLinkedHashMap.put(objArray2[i], objArray[i]);
        Collection values = myLinkedHashMap.values();
        new Support_UnmodifiableCollectionTest(
                "Test Returned Collection From LinkedHashMap.values()", values)
                .runTest();
        values.remove(new Integer(0));
        assertTrue(
                "Removing from the values collection should remove from the original map",
                !myLinkedHashMap.containsValue(new Integer(0)));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "remove",
        args = {java.lang.Object.class}
    )
    public void test_removeLjava_lang_Object() {
        int size = hm.size();
        Integer y = new Integer(9);
        Integer x = ((Integer) hm.remove(y.toString()));
        assertTrue("Remove returned incorrect value", x.equals(new Integer(9)));
        assertNull("Failed to remove given key", hm.get(new Integer(9)));
        assertTrue("Failed to decrement size", hm.size() == (size - 1));
        assertNull("Remove of non-existent key returned non-null", hm
                .remove("LCLCLC"));
        LinkedHashMap m = new LinkedHashMap();
        m.put(null, "test");
        assertNull("Failed with same hash as null",
                m.remove(new Integer(0)));
        assertEquals("Failed with null key", "test", m.remove(null));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clear",
        args = {}
    )
    public void test_clear() {
        hm.clear();
        assertEquals("Clear failed to reset size", 0, hm.size());
        for (int i = 0; i < hmSize; i++)
            assertNull("Failed to clear all elements",
                    hm.get(objArray2[i]));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
    public void test_clone() {
        LinkedHashMap hm2 = (LinkedHashMap) hm.clone();
        assertTrue("Clone answered equivalent LinkedHashMap", hm2 != hm);
        for (int counter = 0; counter < hmSize; counter++)
            assertTrue("Clone answered unequal LinkedHashMap", hm
                    .get(objArray2[counter]) == hm2.get(objArray2[counter]));
        LinkedHashMap map = new LinkedHashMap();
        map.put("key", "value");
        Set keys = map.keySet();
        Collection values = map.values();
        assertEquals("values() does not work",
                "value", values.iterator().next());
        assertEquals("keySet() does not work",
                "key", keys.iterator().next());
        AbstractMap map2 = (AbstractMap) map.clone();
        map2.put("key", "value2");
        Collection values2 = map2.values();
        assertTrue("values() is identical", values2 != values);
        assertEquals("values() was not cloned",
                "value2", values2.iterator().next());
        map2.clear();
        map2.put("key2", "value3");
        Set key2 = map2.keySet();
        assertTrue("keySet() is identical", key2 != keys);
        assertEquals("keySet() was not cloned",
                "key2", key2.iterator().next());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
    public void test_clone_ordered() {
        LinkedHashMap<String, String> hm1 = new LinkedHashMap<String, String>(10, 0.75f, true);
        hm1.put("a", "a");
        hm1.put("b", "b");
        hm1.put("c", "c");
        LinkedHashMap<String, String> hm2 = (LinkedHashMap<String, String>) hm1.clone();
        hm1.get("a");
        Map.Entry<String, String>[] set = new Map.Entry[3];
        Iterator<Map.Entry<String,String>> iterator = hm1.entrySet().iterator();
        assertEquals("b", iterator.next().getKey());
        assertEquals("c", iterator.next().getKey());
        assertEquals("a", iterator.next().getKey());
        iterator = hm2.entrySet().iterator();
        assertEquals("a", iterator.next().getKey());
        assertEquals("b", iterator.next().getKey());
        assertEquals("c", iterator.next().getKey());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Regression test.",
        method = "clone",
        args = {}
    )
    public void test_clone_Mock() {
        LinkedHashMap hashMap = new MockMap();
        String value = "value a";
        hashMap.put("key", value);
        MockMap cloneMap = (MockMap) hashMap.clone();
        assertEquals(value, cloneMap.get("key"));
        assertEquals(hashMap, cloneMap);
        assertEquals(1, cloneMap.num);
        hashMap.put("key", "value b");
        assertFalse(hashMap.equals(cloneMap));
    }
    class MockMap extends LinkedHashMap {
        int num;
        public Object put(Object k, Object v) {
            num++;
            return super.put(k, v);
        }
        protected boolean removeEldestEntry(Map.Entry e) {
            return num > 1;
        }
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Regression for bug Google Bug 2121546",
        method = "get",
        args = {java.lang.Object.class}
    )
    public void test_removeEldestFromSameBucketAsNewEntry() {
        LinkedHashMap<String, String> map
                = new LinkedHashMap<String, String>(6, 0.75F, true) {
            @Override
            protected boolean removeEldestEntry(Entry<String, String> e) {
                return true;
            }
        };
        map.put("N", "E");
        map.put("F", "I");
        assertEquals(null, map.get("N"));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify ClassCastException, NullPointerException.",
        method = "containsKey",
        args = {java.lang.Object.class}
    )
    public void test_containsKeyLjava_lang_Object() {
        assertTrue("Returned false for valid key", hm.containsKey(new Integer(
                876).toString()));
        assertTrue("Returned true for invalid key", !hm.containsKey("KKDKDKD"));
        LinkedHashMap m = new LinkedHashMap();
        m.put(null, "test");
        assertTrue("Failed with null key", m.containsKey(null));
        assertTrue("Failed with missing key matching null hash", !m
                .containsKey(new Integer(0)));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify ClassCastException, NullPointerException.",
        method = "containsValue",
        args = {java.lang.Object.class}
    )
    public void test_containsValueLjava_lang_Object() {
        assertTrue("Returned false for valid value", hm
                .containsValue(new Integer(875)));
        assertTrue("Returned true for invalid valie", !hm
                .containsValue(new Integer(-9)));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isEmpty",
        args = {}
    )
    public void test_isEmpty() {
        assertTrue("Returned false for new map", new LinkedHashMap().isEmpty());
        assertTrue("Returned true for non-empty", !hm.isEmpty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "size",
        args = {}
    )
    public void test_size() {
        assertTrue("Returned incorrect size",
                hm.size() == (objArray.length + 2));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "entrySet",
        args = {}
    )
    public void test_ordered_entrySet() {
        int i;
        int sz = 100;
        LinkedHashMap lhm = new LinkedHashMap();
        for (i = 0; i < sz; i++) {
            Integer ii = new Integer(i);
            lhm.put(ii, ii.toString());
        }
        Set s1 = lhm.entrySet();
        Iterator it1 = s1.iterator();
        assertTrue("Returned set of incorrect size 1", lhm.size() == s1.size());
        for (i = 0; it1.hasNext(); i++) {
            Map.Entry m = (Map.Entry) it1.next();
            Integer jj = (Integer) m.getKey();
            assertTrue("Returned incorrect entry set 1", jj.intValue() == i);
        }
        LinkedHashMap lruhm = new LinkedHashMap(200, .75f, true);
        for (i = 0; i < sz; i++) {
            Integer ii = new Integer(i);
            lruhm.put(ii, ii.toString());
        }
        Set s3 = lruhm.entrySet();
        Iterator it3 = s3.iterator();
        assertTrue("Returned set of incorrect size 2", lruhm.size() == s3
                .size());
        for (i = 0; i < sz && it3.hasNext(); i++) {
            Map.Entry m = (Map.Entry) it3.next();
            Integer jj = (Integer) m.getKey();
            assertTrue("Returned incorrect entry set 2", jj.intValue() == i);
        }
        int p = 0;
        for (i = 0; i < sz; i += 2) {
            String ii = (String) lruhm.get(new Integer(i));
            p = p + Integer.parseInt(ii);
        }
        assertEquals("invalid sum of even numbers", 2450, p);
        Set s2 = lruhm.entrySet();
        Iterator it2 = s2.iterator();
        assertTrue("Returned set of incorrect size 3", lruhm.size() == s2
                .size());
        for (i = 1; i < sz && it2.hasNext(); i += 2) {
            Map.Entry m = (Map.Entry) it2.next();
            Integer jj = (Integer) m.getKey();
            assertTrue("Returned incorrect entry set 3", jj.intValue() == i);
        }
        for (i = 0; i < sz && it2.hasNext(); i += 2) {
            Map.Entry m = (Map.Entry) it2.next();
            Integer jj = (Integer) m.getKey();
            assertTrue("Returned incorrect entry set 4", jj.intValue() == i);
        }
        assertTrue("Entries left to iterate on", !it2.hasNext());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "keySet",
        args = {}
    )
    public void test_ordered_keySet() {
        int i;
        int sz = 100;
        LinkedHashMap lhm = new LinkedHashMap();
        for (i = 0; i < sz; i++) {
            Integer ii = new Integer(i);
            lhm.put(ii, ii.toString());
        }
        Set s1 = lhm.keySet();
        Iterator it1 = s1.iterator();
        assertTrue("Returned set of incorrect size", lhm.size() == s1.size());
        for (i = 0; it1.hasNext(); i++) {
            Integer jj = (Integer) it1.next();
            assertTrue("Returned incorrect entry set", jj.intValue() == i);
        }
        LinkedHashMap lruhm = new LinkedHashMap(200, .75f, true);
        for (i = 0; i < sz; i++) {
            Integer ii = new Integer(i);
            lruhm.put(ii, ii.toString());
        }
        Set s3 = lruhm.keySet();
        Iterator it3 = s3.iterator();
        assertTrue("Returned set of incorrect size", lruhm.size() == s3.size());
        for (i = 0; i < sz && it3.hasNext(); i++) {
            Integer jj = (Integer) it3.next();
            assertTrue("Returned incorrect entry set", jj.intValue() == i);
        }
        int p = 0;
        for (i = 0; i < sz; i += 2) {
            String ii = (String) lruhm.get(new Integer(i));
            p = p + Integer.parseInt(ii);
        }
        assertEquals("invalid sum of even numbers", 2450, p);
        Set s2 = lruhm.keySet();
        Iterator it2 = s2.iterator();
        assertTrue("Returned set of incorrect size", lruhm.size() == s2.size());
        for (i = 1; i < sz && it2.hasNext(); i += 2) {
            Integer jj = (Integer) it2.next();
            assertTrue("Returned incorrect entry set", jj.intValue() == i);
        }
        for (i = 0; i < sz && it2.hasNext(); i += 2) {
            Integer jj = (Integer) it2.next();
            assertTrue("Returned incorrect entry set", jj.intValue() == i);
        }
        assertTrue("Entries left to iterate on", !it2.hasNext());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "values",
        args = {}
    )
    public void test_ordered_values() {
        int i;
        int sz = 100;
        LinkedHashMap lhm = new LinkedHashMap();
        for (i = 0; i < sz; i++) {
            Integer ii = new Integer(i);
            lhm.put(ii, new Integer(i * 2));
        }
        Collection s1 = lhm.values();
        Iterator it1 = s1.iterator();
        assertTrue("Returned set of incorrect size 1", lhm.size() == s1.size());
        for (i = 0; it1.hasNext(); i++) {
            Integer jj = (Integer) it1.next();
            assertTrue("Returned incorrect entry set 1", jj.intValue() == i * 2);
        }
        LinkedHashMap lruhm = new LinkedHashMap(200, .75f, true);
        for (i = 0; i < sz; i++) {
            Integer ii = new Integer(i);
            lruhm.put(ii, new Integer(i * 2));
        }
        Collection s3 = lruhm.values();
        Iterator it3 = s3.iterator();
        assertTrue("Returned set of incorrect size", lruhm.size() == s3.size());
        for (i = 0; i < sz && it3.hasNext(); i++) {
            Integer jj = (Integer) it3.next();
            assertTrue("Returned incorrect entry set", jj.intValue() == i * 2);
        }
        int p = 0;
        for (i = 0; i < sz; i += 2) {
            Integer ii = (Integer) lruhm.get(new Integer(i));
            p = p + ii.intValue();
        }
        assertTrue("invalid sum of even numbers", p == 2450 * 2);
        Collection s2 = lruhm.values();
        Iterator it2 = s2.iterator();
        assertTrue("Returned set of incorrect size", lruhm.size() == s2.size());
        for (i = 1; i < sz && it2.hasNext(); i += 2) {
            Integer jj = (Integer) it2.next();
            assertTrue("Returned incorrect entry set", jj.intValue() == i * 2);
        }
        for (i = 0; i < sz && it2.hasNext(); i += 2) {
            Integer jj = (Integer) it2.next();
            assertTrue("Returned incorrect entry set", jj.intValue() == i * 2);
        }
        assertTrue("Entries left to iterate on", !it2.hasNext());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "removeEldestEntry",
        args = {java.util.Map.Entry.class}
    )
    public void test_remove_eldest() {
        int i;
        int sz = 10;
        CacheMap lhm = new CacheMap();
        for (i = 0; i < sz; i++) {
            Integer ii = new Integer(i);
            lhm.put(ii, new Integer(i * 2));
        }
        Collection s1 = lhm.values();
        Iterator it1 = s1.iterator();
        assertTrue("Returned set of incorrect size 1", lhm.size() == s1.size());
        for (i = 5; it1.hasNext(); i++) {
            Integer jj = (Integer) it1.next();
            assertTrue("Returned incorrect entry set 1", jj.intValue() == i * 2);
        }
        assertTrue("Entries left in map", !it1.hasNext());
    }
    protected void setUp() {
        objArray = new Object[hmSize];
        objArray2 = new Object[hmSize];
        for (int i = 0; i < objArray.length; i++) {
            objArray[i] = new Integer(i);
            objArray2[i] = objArray[i].toString();
        }
        hm = new LinkedHashMap();
        for (int i = 0; i < objArray.length; i++)
            hm.put(objArray2[i], objArray[i]);
        hm.put("test", null);
        hm.put(null, "test");
    }
    protected void tearDown() {
        objArray = null;
        objArray2 = null;
        hm = null;
    }
}
