public class AtomicReferenceArrayTest extends JSR166TestCase 
{
    public static void main (String[] args) {
        junit.textui.TestRunner.run (suite());
    }
    public static Test suite() {
        return new TestSuite(AtomicReferenceArrayTest.class);
    }
    public void testConstructor(){
        AtomicReferenceArray<Integer> ai = new AtomicReferenceArray<Integer>(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertNull(ai.get(i));
        }
    }
    public void testConstructor2NPE() {
        try {
            Integer[] a = null;
            AtomicReferenceArray<Integer> ai = new AtomicReferenceArray<Integer>(a);
        } catch (NullPointerException success) {
        } catch (Exception ex) {
            unexpectedException();
        }
    }
    public void testConstructor2() {
        Integer[] a = { two, one, three, four, seven};
        AtomicReferenceArray<Integer> ai = new AtomicReferenceArray<Integer>(a);
        assertEquals(a.length, ai.length());
        for (int i = 0; i < a.length; ++i) 
            assertEquals(a[i], ai.get(i));
    }
    public void testIndexing(){
        AtomicReferenceArray<Integer> ai = new AtomicReferenceArray<Integer>(SIZE);
        try {
            ai.get(SIZE);
        } catch(IndexOutOfBoundsException success){
        }
        try {
            ai.get(-1);
        } catch(IndexOutOfBoundsException success){
        }
        try {
            ai.set(SIZE, null);
        } catch(IndexOutOfBoundsException success){
        }
        try {
            ai.set(-1, null);
        } catch(IndexOutOfBoundsException success){
        }
    }
    public void testGetSet(){
        AtomicReferenceArray ai = new AtomicReferenceArray(SIZE); 
        for (int i = 0; i < SIZE; ++i) {
            ai.set(i, one);
            assertEquals(one,ai.get(i));
            ai.set(i, two);
            assertEquals(two,ai.get(i));
            ai.set(i, m3);
            assertEquals(m3,ai.get(i));
        }
    }
    public void testCompareAndSet(){
        AtomicReferenceArray ai = new AtomicReferenceArray(SIZE); 
        for (int i = 0; i < SIZE; ++i) {
            ai.set(i, one);
            assertTrue(ai.compareAndSet(i, one,two));
            assertTrue(ai.compareAndSet(i, two,m4));
            assertEquals(m4,ai.get(i));
            assertFalse(ai.compareAndSet(i, m5,seven));
            assertFalse((seven.equals(ai.get(i))));
            assertTrue(ai.compareAndSet(i, m4,seven));
            assertEquals(seven,ai.get(i));
        }
    }
    public void testCompareAndSetInMultipleThreads() {
        final AtomicReferenceArray a = new AtomicReferenceArray(1);
        a.set(0, one);
        Thread t = new Thread(new Runnable() {
                public void run() {
                    while(!a.compareAndSet(0, two, three)) Thread.yield();
                }});
        try {
            t.start();
            assertTrue(a.compareAndSet(0, one, two));
            t.join(LONG_DELAY_MS);
            assertFalse(t.isAlive());
            assertEquals(a.get(0), three);
        }
        catch(Exception e) {
            unexpectedException();
        }
    }
    public void testWeakCompareAndSet(){
        AtomicReferenceArray ai = new AtomicReferenceArray(SIZE); 
        for (int i = 0; i < SIZE; ++i) {
            ai.set(i, one);
            while(!ai.weakCompareAndSet(i, one,two));
            while(!ai.weakCompareAndSet(i, two,m4));
            assertEquals(m4,ai.get(i));
            while(!ai.weakCompareAndSet(i, m4,seven));
            assertEquals(seven,ai.get(i));
        }
    }
    public void testGetAndSet(){
        AtomicReferenceArray ai = new AtomicReferenceArray(SIZE); 
        for (int i = 0; i < SIZE; ++i) {
            ai.set(i, one);
            assertEquals(one,ai.getAndSet(i,zero));
            assertEquals(0,ai.getAndSet(i,m10));
            assertEquals(m10,ai.getAndSet(i,one));
        }
    }
    public void testSerialization() {
        AtomicReferenceArray l = new AtomicReferenceArray(SIZE); 
        for (int i = 0; i < SIZE; ++i) {
            l.set(i, new Integer(-i));
        }
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream(10000);
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(bout));
            out.writeObject(l);
            out.close();
            ByteArrayInputStream bin = new ByteArrayInputStream(bout.toByteArray());
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(bin));
            AtomicReferenceArray r = (AtomicReferenceArray) in.readObject();
            assertEquals(l.length(), r.length());
            for (int i = 0; i < SIZE; ++i) {
                assertEquals(r.get(i), l.get(i));
            }
        } catch(Exception e){
            unexpectedException();
        }
    }
    public void testToString() {
        Integer[] a = { two, one, three, four, seven};
        AtomicReferenceArray<Integer> ai = new AtomicReferenceArray<Integer>(a);
        assertEquals(Arrays.toString(a), ai.toString());
    }
}
