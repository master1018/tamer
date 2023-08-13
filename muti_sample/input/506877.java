public class AtomicReferenceTest extends JSR166TestCase {
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
        return new TestSuite(AtomicReferenceTest.class);
    }
    public void testConstructor(){
        AtomicReference ai = new AtomicReference(one);
        assertEquals(one,ai.get());
    }
    public void testConstructor2(){
        AtomicReference ai = new AtomicReference();
        assertNull(ai.get());
    }
    public void testGetSet(){
        AtomicReference ai = new AtomicReference(one);
        assertEquals(one,ai.get());
        ai.set(two);
        assertEquals(two,ai.get());
        ai.set(m3);
        assertEquals(m3,ai.get());
    }
    public void testCompareAndSet(){
        AtomicReference ai = new AtomicReference(one);
        assertTrue(ai.compareAndSet(one,two));
        assertTrue(ai.compareAndSet(two,m4));
        assertEquals(m4,ai.get());
        assertFalse(ai.compareAndSet(m5,seven));
        assertFalse((seven.equals(ai.get())));
        assertTrue(ai.compareAndSet(m4,seven));
        assertEquals(seven,ai.get());
    }
    public void testCompareAndSetInMultipleThreads() {
        final AtomicReference ai = new AtomicReference(one);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    while(!ai.compareAndSet(two, three)) Thread.yield();
                }});
        try {
            t.start();
            assertTrue(ai.compareAndSet(one, two));
            t.join(LONG_DELAY_MS);
            assertFalse(t.isAlive());
            assertEquals(ai.get(), three);
        }
        catch(Exception e) {
            unexpectedException();
        }
    }
    public void testWeakCompareAndSet(){
        AtomicReference ai = new AtomicReference(one);
        while(!ai.weakCompareAndSet(one,two));
        while(!ai.weakCompareAndSet(two,m4));
        assertEquals(m4,ai.get());
        while(!ai.weakCompareAndSet(m4,seven));
        assertEquals(seven,ai.get());
    }
    public void testGetAndSet(){
        AtomicReference ai = new AtomicReference(one);
        assertEquals(one,ai.getAndSet(zero));
        assertEquals(zero,ai.getAndSet(m10));
        assertEquals(m10,ai.getAndSet(one));
    }
    public void testSerialization() {
        AtomicReference l = new AtomicReference();
        try {
            l.set(one);
            ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(bout));
            out.writeObject(l);
            out.close();
            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(bin));
            AtomicReference r = (AtomicReference) in.readObject();
            assertEquals(l.get(), r.get());
        } catch(Exception e){
            unexpectedException();
        }
    }
    public void testToString() {
        AtomicReference<Integer> ai = new AtomicReference<Integer>(one); 
        assertEquals(ai.toString(), one.toString());
        ai.set(two);
        assertEquals(ai.toString(), two.toString());
    }
}
