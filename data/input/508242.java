@TestTargetClass(IdentityHashMap.class) 
public class IdentityHashMapTest extends junit.framework.TestCase {
    private static final String ID = "hello";
    class MockMap extends AbstractMap {
        public Set entrySet() {
            return null;
        }
        public int size(){
            return 0;
        }
    }
    IdentityHashMap hm;
    final static int hmSize = 1000;
    Object[] objArray;
    Object[] objArray2;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IdentityHashMap",
        args = {}
    )
    public void test_Constructor() {
        new Support_MapTest2(new IdentityHashMap()).runTest();
        IdentityHashMap hm2 = new IdentityHashMap();
        assertEquals("Created incorrect IdentityHashMap", 0, hm2.size());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IdentityHashMap",
        args = {int.class}
    )
    public void test_ConstructorI() {
        IdentityHashMap hm2 = new IdentityHashMap(5);
        assertEquals("Created incorrect IdentityHashMap", 0, hm2.size());
        try {
            new IdentityHashMap(-1);
            fail("Failed to throw IllegalArgumentException for initial capacity < 0");
        } catch (IllegalArgumentException e) {
        }
        IdentityHashMap empty = new IdentityHashMap(0);
        assertNull("Empty IdentityHashMap access", empty.get("nothing"));
        empty.put("something", "here");
        assertTrue("cannot get element", empty.get("something") == "here");
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "IdentityHashMap",
        args = {java.util.Map.class}
    )
    public void test_ConstructorLjava_util_Map() {
        Map myMap = new TreeMap();
        for (int counter = 0; counter < hmSize; counter++)
            myMap.put(objArray2[counter], objArray[counter]);
        IdentityHashMap hm2 = new IdentityHashMap(myMap);
        for (int counter = 0; counter < hmSize; counter++)
            assertTrue("Failed to construct correct IdentityHashMap", hm
                    .get(objArray2[counter]) == hm2.get(objArray2[counter]));
        Map mockMap = new MockMap();
        hm2 = new IdentityHashMap(mockMap);
        assertEquals("Size should be 0", 0, hm2.size());
        try {
            new IdentityHashMap(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
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
        IdentityHashMap hm2 = (IdentityHashMap) hm.clone();
        assertTrue("Clone answered equivalent IdentityHashMap", hm2 != hm);
        for (int counter = 0; counter < hmSize; counter++)
            assertTrue("Clone answered unequal IdentityHashMap", hm
                    .get(objArray2[counter]) == hm2.get(objArray2[counter]));
        IdentityHashMap map = new IdentityHashMap();
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
        level = TestLevel.COMPLETE,
        notes = "",
        method = "containsKey",
        args = {java.lang.Object.class}
    )
    public void test_containsKeyLjava_lang_Object() {
        assertTrue("Returned false for valid key", hm
                .containsKey(objArray2[23]));
        assertTrue("Returned true for copy of valid key", !hm
                .containsKey(new Integer(23).toString()));
        assertTrue("Returned true for invalid key", !hm.containsKey("KKDKDKD"));
        IdentityHashMap m = new IdentityHashMap();
        m.put(null, "test");
        assertTrue("Failed with null key", m.containsKey(null));
        assertTrue("Failed with missing key matching null hash", !m
                .containsKey(new Integer(0)));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "containsValue",
        args = {java.lang.Object.class}
    )
    public void test_containsValueLjava_lang_Object() {
        assertTrue("Returned false for valid value", hm
                .containsValue(objArray[19]));
        assertTrue("Returned true for invalid valie", !hm
                .containsValue(new Integer(-9)));
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
        notes = "",
        method = "get",
        args = {java.lang.Object.class}
    )
    public void test_getLjava_lang_Object() {
        assertNull("Get returned non-null for non existent key",
                hm.get("T"));
        hm.put("T", "HELLO");
        assertEquals("Get returned incorecct value for existing key", "HELLO", hm.get("T")
                );
        IdentityHashMap m = new IdentityHashMap();
        m.put(null, "test");
        assertEquals("Failed with null key", "test", m.get(null));
        assertNull("Failed with missing key matching null hash", m
                .get(new Integer(0)));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isEmpty",
        args = {}
    )
    public void test_isEmpty() {
        assertTrue("Returned false for new map", new IdentityHashMap()
                .isEmpty());
        assertTrue("Returned true for non-empty", !hm.isEmpty());
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
        for (int i = 0; i < objArray.length; i++) {
            assertTrue("Returned set does not contain all keys", s
                    .contains(objArray2[i]));
        }
        IdentityHashMap m = new IdentityHashMap();
        m.put(null, "test");
        assertTrue("Failed with null key", m.keySet().contains(null));
        assertNull("Failed with null key", m.keySet().iterator().next());
        Map map = new IdentityHashMap(101);
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
        Map map2 = new IdentityHashMap(101);
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
        method = "put",
        args = {java.lang.Object.class, java.lang.Object.class}
    )
    public void test_putLjava_lang_ObjectLjava_lang_Object() {
        hm.put("KEY", "VALUE");
        assertEquals("Failed to install key/value pair", 
                "VALUE", hm.get("KEY"));
        IdentityHashMap m = new IdentityHashMap();
        Short s0 = new Short((short) 0);
        m.put(s0, "short");
        m.put(null, "test");
        Integer i0 = new Integer(0);
        m.put(i0, "int");
        assertEquals("Failed adding to bucket containing null", 
                "short", m.get(s0));
        assertEquals("Failed adding to bucket containing null2", "int", m.get(i0)
                );
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "putAll",
        args = {java.util.Map.class}
    )
    public void test_putAllLjava_util_Map() {
        IdentityHashMap hm2 = new IdentityHashMap();
        hm2.putAll(hm);
        for (int i = 0; i < 1000; i++)
            assertTrue("Failed to clear all elements", hm2.get(objArray2[i])
                    .equals((new Integer(i))));
        hm2 = new IdentityHashMap();
        Map mockMap = new MockMap();
        hm2.putAll(mockMap);
        assertEquals("Size should be 0", 0, hm2.size());
        try {
            hm2.putAll(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "remove",
        args = {java.lang.Object.class}
    )
    public void test_removeLjava_lang_Object() {
        int size = hm.size();
        Integer x = ((Integer) hm.remove(objArray2[9]));
        assertTrue("Remove returned incorrect value", x.equals(new Integer(9)));
        assertNull("Failed to remove given key", hm.get(objArray2[9]));
        assertTrue("Failed to decrement size", hm.size() == (size - 1));
        assertNull("Remove of non-existent key returned non-null", hm
                .remove("LCLCLC"));
        IdentityHashMap m = new IdentityHashMap();
        m.put(null, "test");
        assertNull("Failed with same hash as null",
                m.remove(objArray[0]));
        assertEquals("Failed with null key", "test", m.remove(null));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "size",
        args = {}
    )
    public void test_size() {
        assertEquals("Returned incorrect size, ", (objArray.length + 2), hm
                .size());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "equals",
        args = {java.lang.Object.class}
    )
    public void test_equalsLjava_lang_Object() {
        IdentityHashMap mapOne = new IdentityHashMap();
        IdentityHashMap mapTwo = new IdentityHashMap();
        IdentityHashMap mapThree = new IdentityHashMap();
        IdentityHashMap mapFour = new IdentityHashMap();
        String one = "one";
        String alsoOne = new String(one); 
        String two = "two";
        String alsoTwo = new String(two); 
        mapOne.put(one, two);
        mapFour.put(one, two);
        mapTwo.put(alsoOne, two);
        mapThree.put(one, alsoTwo);
        assertEquals("failure of equality of IdentityHashMaps", mapOne, mapFour);
        assertTrue("failure of non-equality of IdentityHashMaps one and two",
                !mapOne.equals(mapTwo));
        assertTrue("failure of non-equality of IdentityHashMaps one and three",
                !mapOne.equals(mapThree));
        assertTrue("failure of non-equality of IdentityHashMaps two and three",
                !mapTwo.equals(mapThree));
        HashMap hashMapTwo = new HashMap();
        HashMap hashMapThree = new HashMap();
        hashMapTwo.put(alsoOne, two);
        hashMapThree.put(one, alsoTwo);
        assertTrue(
                "failure of non-equality of IdentityHashMaps one and Hashmap two",
                !mapOne.equals(hashMapTwo));
        assertTrue(
                "failure of non-equality of IdentityHashMaps one and Hashmap three",
                !mapOne.equals(hashMapThree));
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
        IdentityHashMap myIdentityHashMap = new IdentityHashMap();
        for (int i = 0; i < 100; i++)
            myIdentityHashMap.put(objArray2[i], objArray[i]);
        Collection values = myIdentityHashMap.values();
        values.remove(objArray[0]);
        assertTrue(
                "Removing from the values collection should remove from the original map",
                !myIdentityHashMap.containsValue(objArray2[0]));
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization/deserialization compatibility.",
            method = "!SerializationSelf",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "Verifies serialization/deserialization compatibility.",
            method = "!SerializationGolden",
            args = {}
        )
    })
    public void test_Serialization() throws Exception {
        IdentityHashMap<String, String> map = new IdentityHashMap<String, String>();
        map.put(ID, "world");
        map.put(null, "null");
        SerializationTest.verifySelf(map, comparator);
        SerializationTest.verifyGolden(this, map, comparator);
    }
    protected void setUp() {
        objArray = new Object[hmSize];
        objArray2 = new Object[hmSize];
        for (int i = 0; i < objArray.length; i++) {
            objArray[i] = new Integer(i);
            objArray2[i] = new String(objArray[i].toString());
        }
        hm = new IdentityHashMap();
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
    private static final SerializationTest.SerializableAssert comparator = new 
                             SerializationTest.SerializableAssert() {
        public void assertDeserialized(Serializable initial, Serializable deserialized) {
            IdentityHashMap<String, String> initialMap = (IdentityHashMap<String, String>) initial;
            IdentityHashMap<String, String> deseriaMap = (IdentityHashMap<String, String>) deserialized;
            assertEquals("should be equal", initialMap.size(), deseriaMap.size());
        }
    };
}
