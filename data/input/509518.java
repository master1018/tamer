@TestTargetClass(WeakHashMap.class) 
public class WeakHashMapTest extends junit.framework.TestCase {
    class MockMap extends AbstractMap {
        public Set entrySet() {
            return null;
        }
        public int size(){
            return 0;
        }
    }
    Object[] keyArray = new Object[100];
    Object[] valueArray = new Object[100];
    WeakHashMap whm;
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "WeakHashMap",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "get",
            args = {java.lang.Object.class}
        )
    })
    public void test_Constructor() {
        new Support_MapTest2(new WeakHashMap()).runTest();
        whm = new WeakHashMap();
        for (int i = 0; i < 100; i++)
            whm.put(keyArray[i], valueArray[i]);
        for (int i = 0; i < 100; i++)
            assertTrue("Incorrect value retrieved",
                    whm.get(keyArray[i]) == valueArray[i]);
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "WeakHashMap",
            args = {int.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "get",
            args = {java.lang.Object.class}
        )
    })
    public void test_ConstructorI() {
        whm = new WeakHashMap(50);
        for (int i = 0; i < 100; i++)
            whm.put(keyArray[i], valueArray[i]);
        for (int i = 0; i < 100; i++)
            assertTrue("Incorrect value retrieved",
                    whm.get(keyArray[i]) == valueArray[i]);
        WeakHashMap empty = new WeakHashMap(0);
        assertNull("Empty weakhashmap access", empty.get("nothing"));
        empty.put("something", "here");
        assertTrue("cannot get element", empty.get("something") == "here");
        try {
            new WeakHashMap(-50);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "WeakHashMap",
            args = {int.class, float.class}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "get",
            args = {java.lang.Object.class}
        )
    })
    public void test_ConstructorIF() {
        whm = new WeakHashMap(50, 0.5f);
        for (int i = 0; i < 100; i++)
            whm.put(keyArray[i], valueArray[i]);
        for (int i = 0; i < 100; i++)
            assertTrue("Incorrect value retrieved",
                    whm.get(keyArray[i]) == valueArray[i]);
        WeakHashMap empty = new WeakHashMap(0, 0.75f);
        assertNull("Empty hashtable access", empty.get("nothing"));
        empty.put("something", "here");
        assertTrue("cannot get element", empty.get("something") == "here");
        try {
            new WeakHashMap(50, -0.5f);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            new WeakHashMap(-50, 0.5f);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "WeakHashMap",
        args = {java.util.Map.class}
    )
    public void test_ConstructorLjava_util_Map() {
        Map mockMap = new MockMap();
        WeakHashMap map = new WeakHashMap(mockMap);
        assertEquals("Size should be 0", 0, map.size());
        try {
            new WeakHashMap(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "clear",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "get",
            args = {java.lang.Object.class}
        )
    })
    public void test_clear() {
        whm = new WeakHashMap();
        for (int i = 0; i < 100; i++)
            whm.put(keyArray[i], valueArray[i]);
        whm.clear();
        assertTrue("Cleared map should be empty", whm.isEmpty());
        for (int i = 0; i < 100; i++)
            assertNull("Cleared map should only return null", whm
                    .get(keyArray[i]));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "containsKey",
        args = {java.lang.Object.class}
    )
    public void test_containsKeyLjava_lang_Object() {
        whm = new WeakHashMap();
        for (int i = 0; i < 100; i++)
            whm.put(keyArray[i], valueArray[i]);
        for (int i = 0; i < 100; i++)
            assertTrue("Should contain referenced key", whm
                    .containsKey(keyArray[i]));
        keyArray[25] = null;
        keyArray[50] = null;
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "containsValue",
        args = {java.lang.Object.class}
    )
    public void test_containsValueLjava_lang_Object() {
        whm = new WeakHashMap();
        for (int i = 0; i < 100; i++)
            whm.put(keyArray[i], valueArray[i]);
        for (int i = 0; i < 100; i++)
            assertTrue("Should contain referenced value", whm
                    .containsValue(valueArray[i]));
        keyArray[25] = null;
        keyArray[50] = null;
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "entrySet",
        args = {}
    )
    public void test_entrySet() {
        whm = new WeakHashMap();
        for (int i = 0; i < 100; i++)
            whm.put(keyArray[i], valueArray[i]);
        List keys = Arrays.asList(keyArray);
        List values = Arrays.asList(valueArray);
        Set entrySet = whm.entrySet();
        assertTrue("Incorrect number of entries returned--wanted 100, got: "
                + entrySet.size(), entrySet.size() == 100);
        Iterator it = entrySet.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            assertTrue("Invalid map entry returned--bad key", keys
                    .contains(entry.getKey()));
            assertTrue("Invalid map entry returned--bad key", values
                    .contains(entry.getValue()));
        }
        keys = null;
        values = null;
        keyArray[50] = null;
        int count = 0;
        do {
            System.gc();
            System.gc();
            Runtime.getRuntime().runFinalization();
            count++;
        } while (count <= 5 && entrySet.size() == 100);
        assertTrue(
                "Incorrect number of entries returned after gc--wanted 99, got: "
                        + entrySet.size(), entrySet.size() == 99);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isEmpty",
        args = {}
    )
    public void test_isEmpty() {
        whm = new WeakHashMap();
        assertTrue("New map should be empty", whm.isEmpty());
        Object myObject = new Object();
        whm.put(myObject, myObject);
        assertTrue("Map should not be empty", !whm.isEmpty());
        whm.remove(myObject);
        assertTrue("Map with elements removed should be empty", whm.isEmpty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "put",
        args = {java.lang.Object.class, java.lang.Object.class}
    )
    public void test_putLjava_lang_ObjectLjava_lang_Object() {
        WeakHashMap map = new WeakHashMap();
        map.put(null, "value"); 
        System.gc();
        System.runFinalization();
        map.remove("nothing"); 
        assertEquals("null key was removed", 1, map.size());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "putAll",
        args = {java.util.Map.class}
    )
    public void test_putAllLjava_util_Map() {
        Map mockMap=new MockMap();
        WeakHashMap map = new WeakHashMap();
        map.putAll(mockMap);
        assertEquals("Size should be 0", 0, map.size());
        try {
            map.putAll(null);
            fail("NullPointerException exected");
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
        whm = new WeakHashMap();
        for (int i = 0; i < 100; i++)
            whm.put(keyArray[i], valueArray[i]);
        assertTrue("Remove returned incorrect value",
                whm.remove(keyArray[25]) == valueArray[25]);
        assertNull("Remove returned incorrect value",
                whm.remove(keyArray[25]));
        assertEquals("Size should be 99 after remove", 99, whm.size());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "size",
        args = {}
    )
    public void test_size() {
        whm = new WeakHashMap();
        assertEquals(0, whm.size());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "keySet",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "size",
            args = {}
        )
    })
    public void test_keySet() {
        whm = new WeakHashMap();
        for (int i = 0; i < 100; i++)
            whm.put(keyArray[i], valueArray[i]);
        List keys = Arrays.asList(keyArray);
        List values = Arrays.asList(valueArray);
        Set keySet = whm.keySet();
        assertEquals("Incorrect number of keys returned,", 100, keySet.size());
        Iterator it = keySet.iterator();
        while (it.hasNext()) {
            Object key = it.next();
            assertTrue("Invalid map entry returned--bad key", keys
                    .contains(key));
        }
        keys = null;
        values = null;
        keyArray[50] = null;
        int count = 0;
        do {
            System.gc();
            System.gc();
            Runtime.getRuntime().runFinalization();
            count++;
        } while (count <= 5 && keySet.size() == 100);
        assertEquals("Incorrect number of keys returned after gc,", 99, keySet
                .size());
    }
    @TestTargets({
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "values",
            args = {}
        ),
        @TestTargetNew(
            level = TestLevel.COMPLETE,
            notes = "",
            method = "size",
            args = {}
        )
    })
    public void test_values() {
        whm = new WeakHashMap();
        for (int i = 0; i < 100; i++)
            whm.put(keyArray[i], valueArray[i]);
        List keys = Arrays.asList(keyArray);
        List values = Arrays.asList(valueArray);
        Collection valuesCollection = whm.values();
        assertEquals("Incorrect number of keys returned,", 100,
                valuesCollection.size());
        Iterator it = valuesCollection.iterator();
        while (it.hasNext()) {
            Object value = it.next();
            assertTrue("Invalid map entry returned--bad value", values
                    .contains(value));
        }
        keys = null;
        values = null;
        keyArray[50] = null;
        int count = 0;
        do {
            System.gc();
            System.gc();
            Runtime.getRuntime().runFinalization();
            count++;
        } while (count <= 5 && valuesCollection.size() == 100);
        assertEquals("Incorrect number of keys returned after gc,", 99,
                valuesCollection.size());
    }
    protected void setUp() {
        for (int i = 0; i < 100; i++) {
            keyArray[i] = new Object();
            valueArray[i] = new Object();
        }
    }
    protected void tearDown() {
    }
}
