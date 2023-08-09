public class Support_MapTest2 extends TestCase {
    Map<String, String> map;
    public Support_MapTest2(Map<String, String> m) {
        super();
        map = m;
        if (!map.isEmpty()) {
            fail("Map must be empty");
        }
    }
    @Override
    public void runTest() {
        try {
            map.put("one", "1");
            assertEquals("size should be one", 1, map.size());
            map.clear();
            assertEquals("size should be zero", 0, map.size());
            assertTrue("Should not have entries", !map.entrySet().iterator()
                    .hasNext());
            assertTrue("Should not have keys", !map.keySet().iterator()
                    .hasNext());
            assertTrue("Should not have values", !map.values().iterator()
                    .hasNext());
        } catch (UnsupportedOperationException e) {
        }
        try {
            map.put("one", "1");
            assertEquals("size should be one", 1, map.size());
            map.remove("one");
            assertEquals("size should be zero", 0, map.size());
            assertTrue("Should not have entries", !map.entrySet().iterator()
                    .hasNext());
            assertTrue("Should not have keys", !map.keySet().iterator()
                    .hasNext());
            assertTrue("Should not have values", !map.values().iterator()
                    .hasNext());
        } catch (UnsupportedOperationException e) {
        }
    }
}
