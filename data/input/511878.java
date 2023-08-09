public class AbstractQueueTest extends JSR166TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
        return new TestSuite(AbstractQueueTest.class);
    }
    static class Succeed extends AbstractQueue<Integer> {
        public boolean offer(Integer x) { 
            if (x == null) throw new NullPointerException();
            return true; 
        }
        public Integer peek() { return one; }
        public Integer poll() { return one; }
        public int size() { return 0; }
        public Iterator iterator() { return null; } 
    }
    static class Fail extends AbstractQueue<Integer> {
        public boolean offer(Integer x) { 
            if (x == null) throw new NullPointerException();
            return false; 
        }
        public Integer peek() { return null; }
        public Integer poll() { return null; }
        public int size() { return 0; }
        public Iterator iterator() { return null; } 
    }
    public void testAddS() {
        Succeed q = new Succeed();
        assertTrue(q.add(two));
    }
    public void testAddF() {
        Fail q = new Fail();
        try {
            q.add(one);
            shouldThrow();
        } catch (IllegalStateException success) {
        }
    }
    public void testAddNPE() {
        Succeed q = new Succeed();
        try {
            q.add(null);
            shouldThrow();
        } catch (NullPointerException success) {
        }
    }
    public void testRemoveS() {
        Succeed q = new Succeed();
        q.remove();
    }
    public void testRemoveF() {
        Fail q = new Fail();
        try {
            q.remove();
            shouldThrow();
        } catch (NoSuchElementException success) {
        }
    }
    public void testElementS() {
        Succeed q = new Succeed();
        q.element();
    }
    public void testElementF() {
        Fail q = new Fail();
        try {
            q.element();
            shouldThrow();
        } catch (NoSuchElementException success) {
        }
    }
    public void testAddAll1() {
        try {
            Succeed q = new Succeed();
            q.addAll(null);
            shouldThrow();
        }
        catch (NullPointerException success) {}
    }
    public void testAddAllSelf() {
        try {
            Succeed q = new Succeed();
            q.addAll(q);
            shouldThrow();
        }
        catch (IllegalArgumentException success) {}
    }
    public void testAddAll2() {
        try {
            Succeed q = new Succeed();
            Integer[] ints = new Integer[SIZE];
            q.addAll(Arrays.asList(ints));
            shouldThrow();
        }
        catch (NullPointerException success) {}
    }
    public void testAddAll3() {
        try {
            Succeed q = new Succeed();
            Integer[] ints = new Integer[SIZE];
            for (int i = 0; i < SIZE-1; ++i)
                ints[i] = new Integer(i);
            q.addAll(Arrays.asList(ints));
            shouldThrow();
        }
        catch (NullPointerException success) {}
    }
    public void testAddAll4() {
        try {
            Fail q = new Fail();
            Integer[] ints = new Integer[SIZE];
            for (int i = 0; i < SIZE; ++i)
                ints[i] = new Integer(i);
            q.addAll(Arrays.asList(ints));
            shouldThrow();
        }
        catch (IllegalStateException success) {}
    }
}
