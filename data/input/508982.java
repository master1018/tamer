@TestTargetClass(Map.class)
public class Support_UnmodifiableMapTest extends TestCase {
    Map<String, Integer> map;
    public Support_UnmodifiableMapTest(String p1) {
        super(p1);
    }
    public Support_UnmodifiableMapTest(String p1, Map<String, Integer> m) {
        super(p1);
        map = m;
    }
    @Override
    public void runTest() {
        assertTrue("UnmodifiableMapTest - Should contain the key \"0\"", map
                .containsKey("0"));
        assertTrue("UnmodifiableMapTest - Should contain the key \"50\"", map
                .containsKey("50"));
        assertTrue("UnmodifiableMapTest - Should not contain the key \"100\"",
                !map.containsKey("100"));
        assertTrue("UnmodifiableMapTest - Should contain the value 0", map
                .containsValue(new Integer(0)));
        assertTrue("UnmodifiableMapTest - Should contain the value 50", map
                .containsValue(new Integer(50)));
        assertTrue("UnmodifiableMapTest - Should not contain value 100", !map
                .containsValue(new Integer(100)));
        Set<?> entrySet = map.entrySet();
        Iterator<?> entrySetIterator = entrySet.iterator();
        int myCounter = 0;
        while (entrySetIterator.hasNext()) {
            Map.Entry<?, ?> me = (Map.Entry<?, ?>) entrySetIterator.next();
            assertTrue("UnmodifiableMapTest - Incorrect Map.Entry returned",
                    map.get(me.getKey()).equals(me.getValue()));
            myCounter++;
        }
        assertEquals("UnmodifiableMapTest - Incorrect number of map entries returned",
                100, myCounter);
        assertTrue("UnmodifiableMapTest - getting \"0\" didn't return 0",
                map.get("0").intValue() == 0);
        assertTrue("UnmodifiableMapTest - getting \"50\" didn't return 0",
                map.get("0").intValue() == 0);
        assertNull("UnmodifiableMapTest - getting \"100\" didn't return null",
                map.get("100"));
        assertTrue(
                "UnmodifiableMapTest - should have returned false to isEmpty",
                !map.isEmpty());
        Set<?> keySet = map.keySet();
        t_KeySet(keySet);
        assertTrue("Size should return 100, returned: " + map.size(), map
                .size() == 100);
        new Support_UnmodifiableCollectionTest("Unmod--from map test", map
                .values());
    }
    void t_KeySet(Set<?> keySet) {
        assertTrue("UnmodifiableMapTest - keySetTest - should contain \"0\"",
                keySet.contains("0"));
        assertTrue("UnmodifiableMapTest - keySetTest - should contain \"50\"",
                keySet.contains("50"));
        assertTrue(
                "UnmodifiableMapTest - keySetTest - should not contain \"100\"",
                !keySet.contains("100"));
        HashSet<String> hs = new HashSet<String>();
        hs.add("0");
        hs.add("25");
        hs.add("99");
        assertTrue(
                "UnmodifiableMapTest - keySetTest - should contain set of \"0\", \"25\", and \"99\"",
                keySet.containsAll(hs));
        hs.add("100");
        assertTrue(
                "UnmodifiableMapTest - keySetTest - should not contain set of \"0\", \"25\", \"99\" and \"100\"",
                !keySet.containsAll(hs));
        assertTrue("UnmodifiableMapTest - keySetTest - should not be empty",
                !keySet.isEmpty());
        Iterator<?> it = keySet.iterator();
        while (it.hasNext()) {
            assertTrue(
                    "UnmodifiableMapTest - keySetTest - Iterator returned wrong values",
                    keySet.contains(it.next()));
        }
        assertTrue(
                "UnmodifiableMapTest - keySetTest - returned wrong size.  Wanted 100, got: "
                        + keySet.size(), keySet.size() == 100);
        Object[] objArray;
        objArray = keySet.toArray();
        for (int counter = 0; it.hasNext(); counter++) {
            assertTrue(
                    "UnmodifiableMapTest - keySetTest - toArray returned incorrect array",
                    objArray[counter] == it.next());
        }
        objArray = new Object[100];
        keySet.toArray(objArray);
        for (int counter = 0; it.hasNext(); counter++) {
            assertTrue(
                    "UnmodifiableMapTest - keySetTest - toArray(Object) filled array incorrectly",
                    objArray[counter] == it.next());
        }
    }
}
