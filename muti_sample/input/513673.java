public class _ListIntSet extends TestCase {
    public void test_basic() {
        ListIntSet set = new ListIntSet();
        assertEquals(0, set.elements());
        set.add(31);
        set.add(0);
        set.add(1);
        assertTrue(set.has(0));
        assertTrue(set.has(1));
        assertTrue(set.has(31));
        assertEquals(3, set.elements());
        assertFalse(set.has(2));
        assertFalse(set.has(7));
        assertFalse(set.has(30));
    }
    public void test_iterator() {
        ListIntSet set = new ListIntSet();
        set.add(0);
        set.add(0);
        set.add(1);
        set.add(1);
        set.add(31);
        set.add(31);
        IntIterator iter = set.iterator();
        assertTrue(iter.hasNext());
        assertEquals(iter.next(), 0);
        assertTrue(iter.hasNext());
        assertEquals(iter.next(), 1);
        assertTrue(iter.hasNext());
        assertEquals(iter.next(), 31);
        assertFalse(iter.hasNext());
        try {
            iter.next();
            fail();
        } catch (NoSuchElementException ex) {
        }
    }
    public void test_empty() {
        ListIntSet set = new ListIntSet();
        IntIterator iter = set.iterator();
        assertFalse(iter.hasNext());        
    }
    public void test_remove() {
        ListIntSet set = new ListIntSet();
        set.add(0);
        set.add(1);
        set.add(31);
        assertTrue(set.has(0));
        assertTrue(set.has(1));
        assertTrue(set.has(31));
        assertFalse(set.has(2));
        assertFalse(set.has(7));
        assertFalse(set.has(30));
        set.remove(0);
        assertFalse(set.has(0));
        assertTrue(set.has(1));
        assertTrue(set.has(31));
    }
    public void test_mergeA() {
        ListIntSet setA = new ListIntSet();
        int[] valuesA = {0, 1, 31};
        for (int i = 0; i < valuesA.length; i++) {
            setA.add(valuesA[i]);
        }
        ListIntSet setB = new ListIntSet();
        int[] valuesB = {0, 5, 6, 32, 127, 128};
        for (int i = 0; i < valuesB.length; i++) {
            setB.add(valuesB[i]);
        }
        setA.merge(setB);
        for (int i = 0; i < valuesA.length; i++) {
            assertTrue(setA.has(valuesA[i]));
        }
        for (int i = 0; i < valuesB.length; i++) {
            assertTrue(setA.has(valuesB[i]));
        }
    }
    public void test_mergeB() {
        ListIntSet setA = new ListIntSet();
        int[] valuesA = {0, 1, 31, 129, 130};
        for (int i = 0; i < valuesA.length; i++) {
            setA.add(valuesA[i]);
        }
        ListIntSet setB = new ListIntSet();
        int[] valuesB = {0, 5, 6, 32, 127,128};
        for (int i = 0; i < valuesB.length; i++) {
            setB.add(valuesB[i]);
        }
        setA.merge(setB);
        for (int i = 0; i < valuesA.length; i++) {
            assertTrue(setA.has(valuesA[i]));
        }
        for (int i = 0; i < valuesB.length; i++) {
            assertTrue(setA.has(valuesB[i]));
        }
    }
    public void test_mergeWithBitIntSet() {
        ListIntSet setA = new ListIntSet();
        int[] valuesA = {0, 1, 31, 129, 130};
        for (int i = 0; i < valuesA.length; i++) {
            setA.add(valuesA[i]);
        }
        BitIntSet setB = new BitIntSet(129);
        int[] valuesB = {0, 5, 6, 32, 127,128};
        for (int i = 0; i < valuesB.length; i++) {
            setB.add(valuesB[i]);
        }
        setA.merge(setB);
        for (int i = 0; i < valuesA.length; i++) {
            assertTrue(setA.has(valuesA[i]));
        }
        for (int i = 0; i < valuesB.length; i++) {
            assertTrue(setA.has(valuesB[i]));
        }
    }
    public void test_toString() {
        ListIntSet set = new ListIntSet();
        assertEquals(set.toString(), "{}");
        set.add(1);
        assertEquals(set.toString(), "{1}");
        set.add(2);
        assertEquals(set.toString(), "{1, 2}");
    }
}
