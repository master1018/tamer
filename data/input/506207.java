public class AtomicStampedReferenceTest extends JSR166TestCase{
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
        return new TestSuite(AtomicStampedReferenceTest.class);
    }
    public void testConstructor(){
        AtomicStampedReference ai = new AtomicStampedReference(one, 0);
        assertEquals(one,ai.getReference());
        assertEquals(0, ai.getStamp());
        AtomicStampedReference a2 = new AtomicStampedReference(null, 1);
        assertNull(a2.getReference());
        assertEquals(1, a2.getStamp());
    }
    public void testGetSet(){
        int[] mark = new int[1];
        AtomicStampedReference ai = new AtomicStampedReference(one, 0);
        assertEquals(one,ai.getReference());
        assertEquals(0, ai.getStamp());
        assertEquals(one, ai.get(mark));
        assertEquals(0, mark[0]);
        ai.set(two, 0);
        assertEquals(two,ai.getReference());
        assertEquals(0, ai.getStamp());
        assertEquals(two, ai.get(mark));
        assertEquals(0, mark[0]);
        ai.set(one, 1);
        assertEquals(one,ai.getReference());
        assertEquals(1, ai.getStamp());
        assertEquals(one, ai.get(mark));
        assertEquals(1,mark[0]);
    }
    public void testAttemptStamp(){
        int[] mark = new int[1];
        AtomicStampedReference ai = new AtomicStampedReference(one, 0);
        assertEquals(0, ai.getStamp());
        assertTrue(ai.attemptStamp(one, 1));
        assertEquals(1, ai.getStamp());
        assertEquals(one, ai.get(mark));
        assertEquals(1, mark[0]);
    }
    public void testCompareAndSet(){
        int[] mark = new int[1];
        AtomicStampedReference ai = new AtomicStampedReference(one, 0);
        assertEquals(one, ai.get(mark));
        assertEquals(0, ai.getStamp());
        assertEquals(0, mark[0]);
        assertTrue(ai.compareAndSet(one, two, 0, 0));
        assertEquals(two, ai.get(mark));
        assertEquals(0, mark[0]);
        assertTrue(ai.compareAndSet(two, m3, 0, 1));
        assertEquals(m3, ai.get(mark));
        assertEquals(1, mark[0]);
        assertFalse(ai.compareAndSet(two, m3, 1, 1));
        assertEquals(m3, ai.get(mark));
        assertEquals(1, mark[0]);
    }
    public void testCompareAndSetInMultipleThreads() {
        final AtomicStampedReference ai = new AtomicStampedReference(one, 0);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    while(!ai.compareAndSet(two, three, 0, 0)) Thread.yield();
                }});
        try {
            t.start();
            assertTrue(ai.compareAndSet(one, two, 0, 0));
            t.join(LONG_DELAY_MS);
            assertFalse(t.isAlive());
            assertEquals(ai.getReference(), three);
            assertEquals(ai.getStamp(), 0);
        }
        catch(Exception e) {
            unexpectedException();
        }
    }
    public void testCompareAndSetInMultipleThreads2() {
        final AtomicStampedReference ai = new AtomicStampedReference(one, 0);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    while(!ai.compareAndSet(one, one, 1, 2)) Thread.yield();
                }});
        try {
            t.start();
            assertTrue(ai.compareAndSet(one, one, 0, 1));
            t.join(LONG_DELAY_MS);
            assertFalse(t.isAlive());
            assertEquals(ai.getReference(), one);
            assertEquals(ai.getStamp(), 2);
        }
        catch(Exception e) {
            unexpectedException();
        }
    }
    public void testWeakCompareAndSet(){
        int[] mark = new int[1];
        AtomicStampedReference ai = new AtomicStampedReference(one, 0);
        assertEquals(one, ai.get(mark));
        assertEquals(0, ai.getStamp ());
        assertEquals(0, mark[0]);
        while(!ai.weakCompareAndSet(one, two, 0, 0));
        assertEquals(two, ai.get(mark));
        assertEquals(0, mark[0]);
        while(!ai.weakCompareAndSet(two, m3, 0, 1));
        assertEquals(m3, ai.get(mark));
        assertEquals(1, mark[0]);
    }
}
