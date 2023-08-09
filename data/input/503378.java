public class CopyOnWriteArrayListTest extends JSR166TestCase{
    public static void main(String[] args) {
        junit.textui.TestRunner.run (suite());        
    }
    public static Test suite() {
        return new TestSuite(CopyOnWriteArrayListTest.class);
    }
    static CopyOnWriteArrayList populatedArray(int n){
        CopyOnWriteArrayList a = new CopyOnWriteArrayList();
        assertTrue(a.isEmpty());
        for (int i = 0; i < n; ++i) 
            a.add(new Integer(i));
        assertFalse(a.isEmpty());
        assertEquals(n, a.size());
        return a;
    }
    public void testConstructor() {
        CopyOnWriteArrayList a = new CopyOnWriteArrayList();
        assertTrue(a.isEmpty());
    }
    public void testConstructor2() {
        Integer[] ints = new Integer[SIZE];
        for (int i = 0; i < SIZE-1; ++i)
            ints[i] = new Integer(i);
        CopyOnWriteArrayList a = new CopyOnWriteArrayList(ints);
        for (int i = 0; i < SIZE; ++i) 
            assertEquals(ints[i], a.get(i));
    }
    public void testConstructor3() {
        Integer[] ints = new Integer[SIZE];
        for (int i = 0; i < SIZE-1; ++i)
            ints[i] = new Integer(i);
        CopyOnWriteArrayList a = new CopyOnWriteArrayList(Arrays.asList(ints));
        for (int i = 0; i < SIZE; ++i) 
            assertEquals(ints[i], a.get(i));
    }
    public void testAddAll() {
        CopyOnWriteArrayList full = populatedArray(3);
        Vector v = new Vector();
        v.add(three);
        v.add(four);
        v.add(five);
        full.addAll(v);
        assertEquals(6, full.size());
    }
    public void testAddAllAbsent() {
        CopyOnWriteArrayList full = populatedArray(3);
        Vector v = new Vector();
        v.add(three);
        v.add(four);
        v.add(one); 
        full.addAllAbsent(v);
        assertEquals(5, full.size());
    }
    public void testAddIfAbsent() {
        CopyOnWriteArrayList full = populatedArray(SIZE);
        full.addIfAbsent(one);
        assertEquals(SIZE, full.size());
    }
    public void testAddIfAbsent2() {
        CopyOnWriteArrayList full = populatedArray(SIZE);
        full.addIfAbsent(three);
        assertTrue(full.contains(three));
    }
    public void testClear() {
        CopyOnWriteArrayList full = populatedArray(SIZE);
        full.clear();
        assertEquals(0, full.size());
    }
    public void testClone() {
        CopyOnWriteArrayList l1 = populatedArray(SIZE);
        CopyOnWriteArrayList l2 = (CopyOnWriteArrayList)(l1.clone());
        assertEquals(l1, l2);
        l1.clear();
        assertFalse(l1.equals(l2));
    }
    public void testContains() {
        CopyOnWriteArrayList full = populatedArray(3);
        assertTrue(full.contains(one));
        assertFalse(full.contains(five));
    }
    public void testAddIndex() {
        CopyOnWriteArrayList full = populatedArray(3);
        full.add(0, m1);
        assertEquals(4, full.size());
        assertEquals(m1, full.get(0));
        assertEquals(zero, full.get(1));
        full.add(2, m2);
        assertEquals(5, full.size());
        assertEquals(m2, full.get(2));
        assertEquals(two, full.get(4));
    }
    public void testEquals() {
        CopyOnWriteArrayList a = populatedArray(3);
        CopyOnWriteArrayList b = populatedArray(3);
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertEquals(a.hashCode(), b.hashCode());
        a.add(m1);
        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
        b.add(m1);
        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertEquals(a.hashCode(), b.hashCode());
    }
    public void testContainsAll() {
        CopyOnWriteArrayList full = populatedArray(3);
        Vector v = new Vector();
        v.add(one);
        v.add(two);
        assertTrue(full.containsAll(v));
        v.add(six);
        assertFalse(full.containsAll(v));
    }
    public void testGet() {
        CopyOnWriteArrayList full = populatedArray(3);
        assertEquals(0, ((Integer)full.get(0)).intValue());
    }
    public void testIndexOf() {
        CopyOnWriteArrayList full = populatedArray(3);
        assertEquals(1, full.indexOf(one));
        assertEquals(-1, full.indexOf("puppies"));
    }
    public void testIndexOf2() {
        CopyOnWriteArrayList full = populatedArray(3);
        assertEquals(1, full.indexOf(one, 0));
        assertEquals(-1, full.indexOf(one, 2));
    }
    public void testIsEmpty() {
        CopyOnWriteArrayList empty = new CopyOnWriteArrayList();
        CopyOnWriteArrayList full = populatedArray(SIZE);
        assertTrue(empty.isEmpty());
        assertFalse(full.isEmpty());
    }
    public void testIterator() {
        CopyOnWriteArrayList full = populatedArray(SIZE);
        Iterator i = full.iterator();
        int j;
        for(j = 0; i.hasNext(); j++)
            assertEquals(j, ((Integer)i.next()).intValue());
        assertEquals(SIZE, j);
    }
    public void testIteratorRemove () {
        CopyOnWriteArrayList full = populatedArray(SIZE);
        Iterator it = full.iterator();
        it.next();
        try {
            it.remove();
            shouldThrow();
        }
        catch (UnsupportedOperationException success) {}
    }
    public void testToString() {
        CopyOnWriteArrayList full = populatedArray(3);
        String s = full.toString();
        for (int i = 0; i < 3; ++i) {
            assertTrue(s.indexOf(String.valueOf(i)) >= 0);
        }
    }        
    public void testLastIndexOf1() {
        CopyOnWriteArrayList full = populatedArray(3);
        full.add(one);
        full.add(three);
        assertEquals(3, full.lastIndexOf(one));
        assertEquals(-1, full.lastIndexOf(six));
    }
    public void testlastIndexOf2() {
        CopyOnWriteArrayList full = populatedArray(3);
        full.add(one);
        full.add(three);
        assertEquals(3, full.lastIndexOf(one, 4));
        assertEquals(-1, full.lastIndexOf(three, 3));
    }
    public void testListIterator1() {
        CopyOnWriteArrayList full = populatedArray(SIZE);
        ListIterator i = full.listIterator();
        int j;
        for(j = 0; i.hasNext(); j++)
            assertEquals(j, ((Integer)i.next()).intValue());
        assertEquals(SIZE, j);
    }
    public void testListIterator2() {
        CopyOnWriteArrayList full = populatedArray(3);
        ListIterator i = full.listIterator(1);
        int j;
        for(j = 0; i.hasNext(); j++)
            assertEquals(j+1, ((Integer)i.next()).intValue());
        assertEquals(2, j);
    }
    public void testRemove() {
        CopyOnWriteArrayList full = populatedArray(3);
        assertEquals(two, full.remove(2));
        assertEquals(2, full.size());
    }
    public void testRemoveAll() {
        CopyOnWriteArrayList full = populatedArray(3);
        Vector v = new Vector();
        v.add(one);
        v.add(two);
        full.removeAll(v);
        assertEquals(1, full.size());
    }
    public void testSet() {
        CopyOnWriteArrayList full = populatedArray(3);
        assertEquals(two, full.set(2, four));
        assertEquals(4, ((Integer)full.get(2)).intValue());
    }
    public void testSize() {
        CopyOnWriteArrayList empty = new CopyOnWriteArrayList();
        CopyOnWriteArrayList full = populatedArray(SIZE);
        assertEquals(SIZE, full.size());
        assertEquals(0, empty.size());
    }
    public void testToArray() {
        CopyOnWriteArrayList full = populatedArray(3);
        Object[] o = full.toArray();
        assertEquals(3, o.length);
        assertEquals(0, ((Integer)o[0]).intValue());
        assertEquals(1, ((Integer)o[1]).intValue());
        assertEquals(2, ((Integer)o[2]).intValue());
    }
    public void testToArray2() {
        CopyOnWriteArrayList full = populatedArray(3);
        Integer[] i = new Integer[3];
        i = (Integer[])full.toArray(i);
        assertEquals(3, i.length);
        assertEquals(0, i[0].intValue());
        assertEquals(1, i[1].intValue());
        assertEquals(2, i[2].intValue());
    }
    public void testSubList() {
        CopyOnWriteArrayList a = populatedArray(10);
        assertTrue(a.subList(1,1).isEmpty());
        for(int j = 0; j < 9; ++j) {
            for(int i = j ; i < 10; ++i) {
                List b = a.subList(j,i);
                for(int k = j; k < i; ++k) {
                    assertEquals(new Integer(k), b.get(k-j));
                }
            }
        }
        List s = a.subList(2, 5);
        assertEquals(s.size(), 3);
        s.set(2, m1);
        assertEquals(a.get(4), m1);
        s.clear();
        assertEquals(a.size(), 7);
    }
    public void testToArray_ArrayStoreException() {
        try {
            CopyOnWriteArrayList c = new CopyOnWriteArrayList();
            c.add("zfasdfsdf");
            c.add("asdadasd");
            c.toArray(new Long[5]);
            shouldThrow();
        } catch(ArrayStoreException e){}
    }
    public void testGet1_IndexOutOfBoundsException() {
        try {
            CopyOnWriteArrayList c = new CopyOnWriteArrayList();
            c.get(-1);
            shouldThrow();
        } catch(IndexOutOfBoundsException e){}
    }
    public void testGet2_IndexOutOfBoundsException() {
        try {
            CopyOnWriteArrayList c = new CopyOnWriteArrayList();
            c.add("asdasd");
            c.add("asdad");
            c.get(100);
            shouldThrow();
        } catch(IndexOutOfBoundsException e){}
    }
    public void testSet1_IndexOutOfBoundsException() {
        try {
            CopyOnWriteArrayList c = new CopyOnWriteArrayList();
            c.set(-1,"qwerty");
            shouldThrow();
        } catch(IndexOutOfBoundsException e){}
    }
    public void testSet2() {
        try {
            CopyOnWriteArrayList c = new CopyOnWriteArrayList();
            c.add("asdasd");
            c.add("asdad");
            c.set(100, "qwerty");
            shouldThrow();
        } catch(IndexOutOfBoundsException e){}
    }
    public void testAdd1_IndexOutOfBoundsException() {
        try {
            CopyOnWriteArrayList c = new CopyOnWriteArrayList();
            c.add(-1,"qwerty");
            shouldThrow();
        } catch(IndexOutOfBoundsException e){}
    }
    public void testAdd2_IndexOutOfBoundsException() {
        try {
            CopyOnWriteArrayList c = new CopyOnWriteArrayList();
            c.add("asdasd");
            c.add("asdasdasd");
            c.add(100, "qwerty");
            shouldThrow();
        } catch(IndexOutOfBoundsException e){}
    }
    public void testRemove1_IndexOutOfBounds() {
        try {
            CopyOnWriteArrayList c = new CopyOnWriteArrayList();
            c.remove(-1);
            shouldThrow();
        } catch(IndexOutOfBoundsException e){}
    }
    public void testRemove2_IndexOutOfBounds() {
        try {
            CopyOnWriteArrayList c = new CopyOnWriteArrayList();
            c.add("asdasd");
            c.add("adasdasd");
            c.remove(100);
            shouldThrow();
        } catch(IndexOutOfBoundsException e){}
    }
    public void testAddAll1_IndexOutOfBoundsException() {
        try {
            CopyOnWriteArrayList c = new CopyOnWriteArrayList();
            c.addAll(-1,new LinkedList());
            shouldThrow();
        } catch(IndexOutOfBoundsException e){}
    }
    public void testAddAll2_IndexOutOfBoundsException() {
        try {
            CopyOnWriteArrayList c = new CopyOnWriteArrayList();
            c.add("asdasd");
            c.add("asdasdasd");
            c.addAll(100, new LinkedList());
            shouldThrow();
        } catch(IndexOutOfBoundsException e){}
    }
    public void testListIterator1_IndexOutOfBoundsException() {
        try {
            CopyOnWriteArrayList c = new CopyOnWriteArrayList();
            c.listIterator(-1);
            shouldThrow();
        } catch(IndexOutOfBoundsException e){}
    }
    public void testListIterator2_IndexOutOfBoundsException() {
        try {
            CopyOnWriteArrayList c = new CopyOnWriteArrayList();
            c.add("adasd");
            c.add("asdasdas");
            c.listIterator(100);
            shouldThrow();
        } catch(IndexOutOfBoundsException e){}
    }
    public void testSubList1_IndexOutOfBoundsException() {
        try {
            CopyOnWriteArrayList c = new CopyOnWriteArrayList();
            c.subList(-1,100);
            shouldThrow();
        } catch(IndexOutOfBoundsException e){}
    }
    public void testSubList2_IndexOutOfBoundsException() {
        try {
            CopyOnWriteArrayList c = new CopyOnWriteArrayList();
            c.add("asdasd");
            c.subList(1,100);
            shouldThrow();
        } catch(IndexOutOfBoundsException e){}
    }
    public void testSubList3_IndexOutOfBoundsException() {
        try {
            CopyOnWriteArrayList c = new CopyOnWriteArrayList();
            c.subList(3,1);
            shouldThrow();
        } catch(IndexOutOfBoundsException e){}
    }
    public void testSerialization() {
        CopyOnWriteArrayList q = populatedArray(SIZE);
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(bout));
            out.writeObject(q);
            out.close();
            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(bin));
            CopyOnWriteArrayList r = (CopyOnWriteArrayList)in.readObject();
            assertEquals(q.size(), r.size());
            assertTrue(q.equals(r));
            assertTrue(r.equals(q));
        } catch(Exception e){
            unexpectedException();
        }
    }
}
