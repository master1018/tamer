public class SystemTest extends JSR166TestCase {
    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());   
    }
    public static Test suite() {
        return new TestSuite(SystemTest.class);
    }
    static final long MILLIS_ROUND = 17;
    public void testNanoTime1() {
        try {
            long m1 = System.currentTimeMillis();
            Thread.sleep(1);
            long n1 = System.nanoTime();
            Thread.sleep(SHORT_DELAY_MS);
            long n2 = System.nanoTime();
            Thread.sleep(1);
            long m2 = System.currentTimeMillis();
            long millis = m2 - m1;
            long nanos = n2 - n1;
            assertTrue(nanos >= 0);
            long nanosAsMillis = nanos / 1000000;
            assertTrue(nanosAsMillis <= millis + MILLIS_ROUND);
        }
        catch(InterruptedException ie) {
            unexpectedException();
        }
    }
    public void testNanoTime2() {
        try {
            long n1 = System.nanoTime();
            Thread.sleep(1);
            long m1 = System.currentTimeMillis();
            Thread.sleep(SHORT_DELAY_MS);
            long m2 = System.currentTimeMillis();
            Thread.sleep(1);
            long n2 = System.nanoTime();
            long millis = m2 - m1;
            long nanos = n2 - n1;
            assertTrue(nanos >= 0);
            long nanosAsMillis = nanos / 1000000;
            assertTrue(millis <= nanosAsMillis + MILLIS_ROUND);
        }
        catch(InterruptedException ie) {
            unexpectedException();
        }
    }
}
