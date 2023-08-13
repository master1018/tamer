public class ThreadLocalTest extends JSR166TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());        
    }
    public static Test suite() {
        return new TestSuite(ThreadLocalTest.class);
    }
    static ThreadLocal<Integer> tl = new ThreadLocal<Integer>() {
            public Integer initialValue() {
                return one;
            }
        };
    static InheritableThreadLocal<Integer> itl =
        new InheritableThreadLocal<Integer>() {
            protected Integer initialValue() {
                return zero;
            }
            protected Integer childValue(Integer parentValue) {
                return new Integer(parentValue.intValue() + 1);
            }
        };
    public void testRemove() {
        assertEquals(tl.get(), one);
        tl.set(two);
        assertEquals(tl.get(), two);
        tl.remove();
        assertEquals(tl.get(), one);
    }
    public void testRemoveITL() {
        assertEquals(itl.get(), zero);
        itl.set(two);
        assertEquals(itl.get(), two);
        itl.remove();
        assertEquals(itl.get(), zero);
    }
    private class ITLThread extends Thread {
        final int[] x;
        ITLThread(int[] array) { x = array; }
        public void run() {
            Thread child = null;
            if (itl.get().intValue() < x.length - 1) {
                child = new ITLThread(x);
                child.start();
            }
            Thread.currentThread().yield();
            int threadId = itl.get().intValue();
            for (int j = 0; j < threadId; j++) {
                x[threadId]++;
                Thread.currentThread().yield();
            }
            if (child != null) { 
                try {
                    child.join();
                } catch(InterruptedException e) {
                    threadUnexpectedException();
                }
            }
        }
    }
    public void testGenericITL() {
        final int threadCount = 10;
        final int x[] = new int[threadCount];
        Thread progenitor = new ITLThread(x);
        try {
            progenitor.start();
            progenitor.join();
            for(int i = 0; i < threadCount; i++) {
                assertEquals(i, x[i]);
            }
        } catch(InterruptedException e) {
            unexpectedException();
        }
    }
}
