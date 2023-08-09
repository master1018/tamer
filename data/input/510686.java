public class ArrayListTest extends TestCase {
    @SuppressWarnings("unchecked")
    @SmallTest
    public void testArrayList() throws Exception {
        ArrayList array = new ArrayList();
        assertEquals(0, array.size());
        assertTrue(array.isEmpty());
        array.add(new Integer(0));
        array.add(0, new Integer(1));
        array.add(1, new Integer(2));
        array.add(new Integer(3));
        array.add(new Integer(1));
        assertEquals(5, array.size());
        assertFalse(array.isEmpty());
        assertEquals(1, ((Integer) array.get(0)).intValue());
        assertEquals(2, ((Integer) array.get(1)).intValue());
        assertEquals(0, ((Integer) array.get(2)).intValue());
        assertEquals(3, ((Integer) array.get(3)).intValue());
        assertEquals(1, ((Integer) array.get(4)).intValue());
        assertFalse(array.contains(null));
        assertTrue(array.contains(new Integer(2)));
        assertEquals(0, array.indexOf(new Integer(1)));
        assertEquals(4, array.lastIndexOf(new Integer(1)));
        assertTrue(array.indexOf(new Integer(5)) < 0);
        assertTrue(array.lastIndexOf(new Integer(5)) < 0);
        array.remove(1);
        array.remove(1);
        assertEquals(3, array.size());
        assertFalse(array.isEmpty());
        assertEquals(1, ((Integer) array.get(0)).intValue());
        assertEquals(3, ((Integer) array.get(1)).intValue());
        assertEquals(1, ((Integer) array.get(2)).intValue());
        assertFalse(array.contains(null));
        assertFalse(array.contains(new Integer(2)));
        assertEquals(0, array.indexOf(new Integer(1)));
        assertEquals(2, array.lastIndexOf(new Integer(1)));
        assertTrue(array.indexOf(new Integer(5)) < 0);
        assertTrue(array.lastIndexOf(new Integer(5)) < 0);
        array.clear();
        assertEquals(0, array.size());
        assertTrue(array.isEmpty());
        assertTrue(array.indexOf(new Integer(5)) < 0);
        assertTrue(array.lastIndexOf(new Integer(5)) < 0);
        ArrayList al = new ArrayList();
        assertFalse(al.remove(null));
        assertFalse(al.remove("string"));
        al.add("string");
        al.add(null);
        assertTrue(al.remove(null));
        assertTrue(al.remove("string"));
    }
}
