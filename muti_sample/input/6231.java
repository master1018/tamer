public class NextBytes {
    private static void realMain(String[] args) throws Throwable {
        byte[] expected = new byte[]
            {27, -105, -24, 83, -77, -29, 119, -74, -106, 68, 54};
        Random r = new java.util.Random(2398579034L);
        for (int i = 0; i <= expected.length; i++) {
            r.setSeed(2398579034L);
            byte[] actual = new byte[i];
            r.nextBytes(actual);
            check(Arrays.equals(actual, Arrays.copyOf(expected,i)));
        }
    }
    static volatile int passed = 0, failed = 0;
    static void pass() {passed++;}
    static void fail() {failed++; Thread.dumpStack();}
    static void fail(String msg) {System.out.println(msg); fail();}
    static void unexpected(Throwable t) {failed++; t.printStackTrace();}
    static void check(boolean cond) {if (cond) pass(); else fail();}
    static void equal(Object x, Object y) {
        if (x == null ? y == null : x.equals(y)) pass();
        else fail(x + " not equal to " + y);}
    public static void main(String[] args) throws Throwable {
        try {realMain(args);} catch (Throwable t) {unexpected(t);}
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
}
