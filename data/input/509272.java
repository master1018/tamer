@TestTargetClass(java.util.LinkedHashSet.class)
public class LinkedHashSetTest extends junit.framework.TestCase {
    LinkedHashSet hs;
    Object[] objArray;
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "LinkedHashSet",
        args = {}
    )
    public void test_Constructor() {
        LinkedHashSet hs2 = new LinkedHashSet();
        assertEquals("Created incorrect LinkedHashSet", 0, hs2.size());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "LinkedHashSet",
        args = {int.class}
    )
    public void test_ConstructorI() {
        LinkedHashSet hs2 = new LinkedHashSet(5);
        assertEquals("Created incorrect LinkedHashSet", 0, hs2.size());
        try {
            new LinkedHashSet(-1);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "LinkedHashSet",
        args = {int.class, float.class}
    )
    public void test_ConstructorIF() {
        LinkedHashSet hs2 = new LinkedHashSet(5, (float) 0.5);
        assertEquals("Created incorrect LinkedHashSet", 0, hs2.size());
        try {
            new LinkedHashSet(-1, 0.5f);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            new LinkedHashSet(1, -0.5f);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
        try {
            new LinkedHashSet(1, 0f);
            fail("IllegalArgumentException expected");
        } catch (IllegalArgumentException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "LinkedHashSet",
        args = {java.util.Collection.class}
    )
    public void test_ConstructorLjava_util_Collection() {
        LinkedHashSet hs2 = new LinkedHashSet(Arrays.asList(objArray));
        for (int counter = 0; counter < objArray.length; counter++)
            assertTrue("LinkedHashSet does not contain correct elements", hs
                    .contains(objArray[counter]));
        assertTrue("LinkedHashSet created from collection incorrect size", hs2
                .size() == objArray.length);
        try {
            new LinkedHashSet(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "add",
        args = {java.lang.Object.class}
    )
    public void test_addLjava_lang_Object() {
        int size = hs.size();
        hs.add(new Integer(8));
        assertTrue("Added element already contained by set", hs.size() == size);
        hs.add(new Integer(-9));
        assertTrue("Failed to increment set size after add",
                hs.size() == size + 1);
        assertTrue("Failed to add element to set", hs.contains(new Integer(-9)));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clear",
        args = {}
    )
    public void test_clear() {
        Set orgSet = (Set) hs.clone();
        hs.clear();
        Iterator i = orgSet.iterator();
        assertEquals("Returned non-zero size after clear", 0, hs.size());
        while (i.hasNext())
            assertTrue("Failed to clear set", !hs.contains(i.next()));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "clone",
        args = {}
    )
    public void test_clone() {
        LinkedHashSet hs2 = (LinkedHashSet) hs.clone();
        assertTrue("clone returned an equivalent LinkedHashSet", hs != hs2);
        assertTrue("clone did not return an equal LinkedHashSet", hs
                .equals(hs2));
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Verifies null as a parameter.",
        method = "contains",
        args = {java.lang.Object.class}
    )
    public void test_containsLjava_lang_Object() {
        assertTrue("Returned false for valid object", hs.contains(objArray[90]));
        assertTrue("Returned true for invalid Object", !hs
                .contains(new Object()));
        LinkedHashSet s = new LinkedHashSet();
        s.add(null);
        assertTrue("Cannot handle null", s.contains(null));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "isEmpty",
        args = {}
    )
    public void test_isEmpty() {
        assertTrue("Empty set returned false", new LinkedHashSet().isEmpty());
        assertTrue("Non-empty set returned true", !hs.isEmpty());
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "iterator",
        args = {}
    )
    public void test_iterator() {
        Iterator i = hs.iterator();
        int x = 0;
        int j;
        for (j = 0; i.hasNext(); j++) {
            Object oo = i.next();
            if (oo != null) {
                Integer ii = (Integer) oo;
                assertTrue("Incorrect element found", ii.intValue() == j);
            } else {
                assertTrue("Cannot find null", hs.contains(oo));
            }
            ++x;
        }
        assertTrue("Returned iteration of incorrect size", hs.size() == x);
        LinkedHashSet s = new LinkedHashSet();
        s.add(null);
        assertNull("Cannot handle null", s.iterator().next());
    }
    @TestTargetNew(
        level = TestLevel.PARTIAL_COMPLETE,
        notes = "Doesn't verify ClassCastException, NullPointerException, UnsupportedOperationException.",
        method = "remove",
        args = {java.lang.Object.class}
    )
    public void test_removeLjava_lang_Object() {
        int size = hs.size();
        hs.remove(new Integer(98));
        assertTrue("Failed to remove element", !hs.contains(new Integer(98)));
        assertTrue("Failed to decrement set size", hs.size() == size - 1);
        LinkedHashSet s = new LinkedHashSet();
        s.add(null);
        assertTrue("Cannot handle null", s.remove(null));
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "size",
        args = {}
    )
    public void test_size() {
        assertTrue("Returned incorrect size", hs.size() == (objArray.length + 1));
        hs.clear();
        assertEquals("Cleared set returned non-zero size", 0, hs.size());
    }
    class Mock_LinkedHashSet extends LinkedHashSet {
        @Override
        public boolean retainAll(Collection c) {
            throw new UnsupportedOperationException();
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "retainAll",
        args = {java.util.Collection.class}
    )
    public void test_retainAllLjava_util_Collection() {
        LinkedHashSet<Integer> lhs = new LinkedHashSet<Integer>();
        Vector v = new Vector<Float>();
        v.add(new Float(3.14));
        lhs.add(new Integer(1));
        assertEquals(1, lhs.size());
        lhs.retainAll(v);
        assertEquals(0, lhs.size());
        v = new Vector<Integer>();
        v.add(new Integer(1));
        v.add(new Integer(2));
        v.add(new Integer(3));
        v.add(new Integer(4));
        v.add(new Integer(5));
        v.add(new Integer(6));
        lhs.add(new Integer(1));
        lhs.add(new Integer(6));
        lhs.add(new Integer(7));
        lhs.add(new Integer(8));
        lhs.add(new Integer(9));
        lhs.add(new Integer(10));
        lhs.add(new Integer(11));
        lhs.add(new Integer(12));
        lhs.add(new Integer(13));
        assertEquals(9, lhs.size());
        lhs.retainAll(v);
        assertEquals(2, lhs.size());
        try {
            lhs.retainAll(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
        lhs = new Mock_LinkedHashSet();
        try {
            lhs.retainAll(v);
            fail("UnsupportedOperationException expected");
        } catch (UnsupportedOperationException e) {
        }
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toArray",
        args = {}
    )
    public void test_toArray() {
        LinkedHashSet<Integer> lhs = new LinkedHashSet<Integer>();
        lhs.add(new Integer(1));
        lhs.add(new Integer(6));
        lhs.add(new Integer(7));
        lhs.add(new Integer(8));
        lhs.add(new Integer(9));
        lhs.add(new Integer(10));
        lhs.add(new Integer(11));
        lhs.add(new Integer(12));
        lhs.add(new Integer(13));
        Object[] o = lhs.toArray();
        for (int i = 0; i < o.length; i++) {
            assertTrue(lhs.contains(o[i]));
        }
        assertEquals(lhs.size(), o.length);
    }
    @TestTargetNew(
        level = TestLevel.COMPLETE,
        notes = "",
        method = "toArray",
        args = {java.lang.Object[].class}
    )
    public void test_toArray$Ljava_lang_Object() {
        LinkedHashSet<Integer> lhs = new LinkedHashSet<Integer>();
        lhs.add(new Integer(1));
        lhs.add(new Integer(6));
        lhs.add(new Integer(7));
        lhs.add(new Integer(8));
        lhs.add(new Integer(9));
        lhs.add(new Integer(10));
        lhs.add(new Integer(11));
        lhs.add(new Integer(12));
        lhs.add(new Integer(13));
        Object[] o1 = new Object[lhs.size()];
        Object[] o2 = new Double[lhs.size()];
        lhs.toArray(o1);
        for (int i = 0; i < o1.length; i++) {
            assertTrue(lhs.contains(o1[i]));
        }
        try {
            lhs.toArray(null);
            fail("NullPointerException expected");
        } catch (NullPointerException e) {
        }
        try {
            lhs.toArray(o2);
            fail("ArrayStoreException expected");
        } catch (ArrayStoreException e) {
        }
    }
    protected void setUp() {
        objArray = new Object[1000];
        for (int i = 0; i < objArray.length; i++)
            objArray[i] = new Integer(i);
        hs = new LinkedHashSet();
        for (int i = 0; i < objArray.length; i++)
            hs.add(objArray[i]);
        hs.add(null);
    }
    protected void tearDown() {
        objArray = null;
        hs = null;
    }
}
