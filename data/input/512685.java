public class ConcurrentHashMapTest extends JSR166TestCase{
    public static void main(String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
        return new TestSuite(ConcurrentHashMapTest.class);
    }
    private static ConcurrentHashMap map5() {
        ConcurrentHashMap map = new ConcurrentHashMap(5);
        assertTrue(map.isEmpty());
        map.put(one, "A");
        map.put(two, "B");
        map.put(three, "C");
        map.put(four, "D");
        map.put(five, "E");
        assertFalse(map.isEmpty());
        assertEquals(5, map.size());
        return map;
    }
    public void testClear() {
        ConcurrentHashMap map = map5();
        map.clear();
        assertEquals(map.size(), 0);
    }
    public void testEquals() {
        ConcurrentHashMap map1 = map5();
        ConcurrentHashMap map2 = map5();
        assertEquals(map1, map2);
        assertEquals(map2, map1);
        map1.clear();
        assertFalse(map1.equals(map2));
        assertFalse(map2.equals(map1));
    }
    public void testContains() {
        ConcurrentHashMap map = map5();
        assertTrue(map.contains("A"));
        assertFalse(map.contains("Z"));
    }
    public void testContainsKey() {
        ConcurrentHashMap map = map5();
        assertTrue(map.containsKey(one));
        assertFalse(map.containsKey(zero));
    }
    public void testContainsValue() {
        ConcurrentHashMap map = map5();
	assertTrue(map.containsValue("A"));
        assertFalse(map.containsValue("Z"));
    }
    public void testEnumeration() {
        ConcurrentHashMap map = map5();
        Enumeration e = map.elements();
        int count = 0;
        while(e.hasMoreElements()){
            count++;
            e.nextElement();
        }
        assertEquals(5, count);
    }
    public void testGet() {
        ConcurrentHashMap map = map5();
        assertEquals("A", (String)map.get(one));
        ConcurrentHashMap empty = new ConcurrentHashMap();
        assertNull(map.get("anything"));
    }
    public void testIsEmpty() {
        ConcurrentHashMap empty = new ConcurrentHashMap();
        ConcurrentHashMap map = map5();
        assertTrue(empty.isEmpty());
        assertFalse(map.isEmpty());
    }
    public void testKeys() {
        ConcurrentHashMap map = map5();
        Enumeration e = map.keys();
        int count = 0;
        while(e.hasMoreElements()){
            count++;
            e.nextElement();
        }
        assertEquals(5, count);
    }
    public void testKeySet() {
        ConcurrentHashMap map = map5();
        Set s = map.keySet();
        assertEquals(5, s.size());
        assertTrue(s.contains(one));
        assertTrue(s.contains(two));
        assertTrue(s.contains(three));
        assertTrue(s.contains(four));
        assertTrue(s.contains(five));
    }
    public void testKeySetToArray() {
        ConcurrentHashMap map = map5();
	Set s = map.keySet();
        Object[] ar = s.toArray();
        assertTrue(s.containsAll(Arrays.asList(ar)));
	assertEquals(5, ar.length);
        ar[0] = m10;
        assertFalse(s.containsAll(Arrays.asList(ar)));
    }
    public void testValuesToArray() {
        ConcurrentHashMap map = map5();
	Collection v = map.values();
        Object[] ar = v.toArray();
        ArrayList s = new ArrayList(Arrays.asList(ar));
	assertEquals(5, ar.length);
	assertTrue(s.contains("A"));
	assertTrue(s.contains("B"));
	assertTrue(s.contains("C"));
	assertTrue(s.contains("D"));
	assertTrue(s.contains("E"));
    }
    public void testEntrySetToArray() {
        ConcurrentHashMap map = map5();
	Set s = map.entrySet();
        Object[] ar = s.toArray();
        assertEquals(5, ar.length);
        for (int i = 0; i < 5; ++i) {
            assertTrue(map.containsKey(((Map.Entry)(ar[i])).getKey()));
            assertTrue(map.containsValue(((Map.Entry)(ar[i])).getValue()));
        }
    }
    public void testValues() {
        ConcurrentHashMap map = map5();
        Collection s = map.values();
        assertEquals(5, s.size());
        assertTrue(s.contains("A"));
        assertTrue(s.contains("B"));
        assertTrue(s.contains("C"));
        assertTrue(s.contains("D"));
        assertTrue(s.contains("E"));
    }
    public void testEntrySet() {
        ConcurrentHashMap map = map5();
        Set s = map.entrySet();
        assertEquals(5, s.size());
        Iterator it = s.iterator();
        while (it.hasNext()) {
            Map.Entry e = (Map.Entry) it.next();
            assertTrue(
                       (e.getKey().equals(one) && e.getValue().equals("A")) ||
                       (e.getKey().equals(two) && e.getValue().equals("B")) ||
                       (e.getKey().equals(three) && e.getValue().equals("C")) ||
                       (e.getKey().equals(four) && e.getValue().equals("D")) ||
                       (e.getKey().equals(five) && e.getValue().equals("E")));
        }
    }
    public void testPutAll() {
        ConcurrentHashMap empty = new ConcurrentHashMap();
        ConcurrentHashMap map = map5();
        empty.putAll(map);
        assertEquals(5, empty.size());
        assertTrue(empty.containsKey(one));
        assertTrue(empty.containsKey(two));
        assertTrue(empty.containsKey(three));
        assertTrue(empty.containsKey(four));
        assertTrue(empty.containsKey(five));
    }
    public void testPutIfAbsent() {
        ConcurrentHashMap map = map5();
        map.putIfAbsent(six, "Z");
        assertTrue(map.containsKey(six));
    }
    public void testPutIfAbsent2() {
        ConcurrentHashMap map = map5();
        assertEquals("A", map.putIfAbsent(one, "Z"));
    }
    public void testReplace() {
        ConcurrentHashMap map = map5();
        assertNull(map.replace(six, "Z"));
        assertFalse(map.containsKey(six));
    }
    public void testReplace2() {
        ConcurrentHashMap map = map5();
        assertNotNull(map.replace(one, "Z"));
        assertEquals("Z", map.get(one));
    }
    public void testReplaceValue() {
        ConcurrentHashMap map = map5();
        assertEquals("A", map.get(one));
        assertFalse(map.replace(one, "Z", "Z"));
        assertEquals("A", map.get(one));
    }
    public void testReplaceValue2() {
        ConcurrentHashMap map = map5();
        assertEquals("A", map.get(one));
        assertTrue(map.replace(one, "A", "Z"));
        assertEquals("Z", map.get(one));
    }
    public void testRemove() {
        ConcurrentHashMap map = map5();
        map.remove(five);
        assertEquals(4, map.size());
        assertFalse(map.containsKey(five));
    }
    public void testRemove2() {
        ConcurrentHashMap map = map5();
        map.remove(five, "E");
        assertEquals(4, map.size());
        assertFalse(map.containsKey(five));
        map.remove(four, "A");
        assertEquals(4, map.size());
        assertTrue(map.containsKey(four));
    }
    public void testSize() {
        ConcurrentHashMap map = map5();
        ConcurrentHashMap empty = new ConcurrentHashMap();
        assertEquals(0, empty.size());
        assertEquals(5, map.size());
    }
    public void testToString() {
        ConcurrentHashMap map = map5();
        String s = map.toString();
        for (int i = 1; i <= 5; ++i) {
            assertTrue(s.indexOf(String.valueOf(i)) >= 0);
        }
    }
    public void testConstructor1() {
        try {
            new ConcurrentHashMap(-1,0,1);
            shouldThrow();
        } catch(IllegalArgumentException e){}
    }
    public void testConstructor2() {
        try {
            new ConcurrentHashMap(1,0,-1);
            shouldThrow();
        } catch(IllegalArgumentException e){}
    }
    public void testConstructor3() {
        try {
            new ConcurrentHashMap(-1);
            shouldThrow();
        } catch(IllegalArgumentException e){}
    }
    public void testGet_NullPointerException() {
        try {
            ConcurrentHashMap c = new ConcurrentHashMap(5);
            c.get(null);
            shouldThrow();
        } catch(NullPointerException e){}
    }
    public void testContainsKey_NullPointerException() {
        try {
            ConcurrentHashMap c = new ConcurrentHashMap(5);
            c.containsKey(null);
            shouldThrow();
        } catch(NullPointerException e){}
    }
    public void testContainsValue_NullPointerException() {
        try {
            ConcurrentHashMap c = new ConcurrentHashMap(5);
            c.containsValue(null);
            shouldThrow();
        } catch(NullPointerException e){}
    }
    public void testContains_NullPointerException() {
        try {
            ConcurrentHashMap c = new ConcurrentHashMap(5);
            c.contains(null);
            shouldThrow();
        } catch(NullPointerException e){}
    }
    public void testPut1_NullPointerException() {
        try {
            ConcurrentHashMap c = new ConcurrentHashMap(5);
            c.put(null, "whatever");
            shouldThrow();
        } catch(NullPointerException e){}
    }
    public void testPut2_NullPointerException() {
        try {
            ConcurrentHashMap c = new ConcurrentHashMap(5);
            c.put("whatever", null);
            shouldThrow();
        } catch(NullPointerException e){}
    }
    public void testPutIfAbsent1_NullPointerException() {
        try {
            ConcurrentHashMap c = new ConcurrentHashMap(5);
            c.putIfAbsent(null, "whatever");
            shouldThrow();
        } catch(NullPointerException e){}
    }
    public void testReplace_NullPointerException() {
        try {
            ConcurrentHashMap c = new ConcurrentHashMap(5);
            c.replace(null, "whatever");
            shouldThrow();
        } catch(NullPointerException e){}
    }
    public void testReplaceValue_NullPointerException() {
        try {
            ConcurrentHashMap c = new ConcurrentHashMap(5);
            c.replace(null, one, "whatever");
            shouldThrow();
        } catch(NullPointerException e){}
    }
    public void testPutIfAbsent2_NullPointerException() {
        try {
            ConcurrentHashMap c = new ConcurrentHashMap(5);
            c.putIfAbsent("whatever", null);
            shouldThrow();
        } catch(NullPointerException e){}
    }
    public void testReplace2_NullPointerException() {
        try {
            ConcurrentHashMap c = new ConcurrentHashMap(5);
            c.replace("whatever", null);
            shouldThrow();
        } catch(NullPointerException e){}
    }
    public void testReplaceValue2_NullPointerException() {
        try {
            ConcurrentHashMap c = new ConcurrentHashMap(5);
            c.replace("whatever", null, "A");
            shouldThrow();
        } catch(NullPointerException e){}
    }
    public void testReplaceValue3_NullPointerException() {
        try {
            ConcurrentHashMap c = new ConcurrentHashMap(5);
            c.replace("whatever", one, null);
            shouldThrow();
        } catch(NullPointerException e){}
    }
    public void testRemove1_NullPointerException() {
        try {
            ConcurrentHashMap c = new ConcurrentHashMap(5);
            c.put("sadsdf", "asdads");
            c.remove(null);
            shouldThrow();
        } catch(NullPointerException e){}
    }
    public void testRemove2_NullPointerException() {
        try {
            ConcurrentHashMap c = new ConcurrentHashMap(5);
            c.put("sadsdf", "asdads");
            c.remove(null, "whatever");
            shouldThrow();
        } catch(NullPointerException e){}
    }
    public void testRemove3() {
        try {
            ConcurrentHashMap c = new ConcurrentHashMap(5);
            c.put("sadsdf", "asdads");
            assertFalse(c.remove("sadsdf", null));
        } catch(NullPointerException e){
            fail();
        }
    }
    public void testSerialization() {
        ConcurrentHashMap q = map5();
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(bout));
            out.writeObject(q);
            out.close();
            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(bin));
            ConcurrentHashMap r = (ConcurrentHashMap)in.readObject();
            assertEquals(q.size(), r.size());
            assertTrue(q.equals(r));
            assertTrue(r.equals(q));
        } catch(Exception e){
            e.printStackTrace();
            unexpectedException();
        }
    }
    public void testSetValueWriteThrough() {
        ConcurrentHashMap map = new ConcurrentHashMap(2, 5.0f, 1);
        assertTrue(map.isEmpty());
        for (int i = 0; i < 20; i++)
            map.put(new Integer(i), new Integer(i));
        assertFalse(map.isEmpty());
        Map.Entry entry1 = (Map.Entry)map.entrySet().iterator().next();
        assertTrue("entry is 16, test not valid",
                   !entry1.getKey().equals(new Integer(16)));
        map.remove(new Integer(16));
        entry1.setValue("XYZ");
        assertTrue(map.containsValue("XYZ")); 
    }
}
