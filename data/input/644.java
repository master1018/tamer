public class Big {
    private static void realMain(String[] args) throws Throwable {
        final int shift = intArg(args, 0, 10); 
        final int tasks = intArg(args, 1, ~0); 
        final int n = (1<<shift) + 47;
        if ((tasks & 0x1) != 0) {
            System.out.println("byte[]");
            System.gc();
            byte[] a = new byte[n];
            a[0]   = (byte) -44;
            a[1]   = (byte) -43;
            a[n-2] = (byte) +43;
            a[n-1] = (byte) +44;
            for (int i : new int[] { 0, 1, n-2, n-1 })
                try { equal(i, Arrays.binarySearch(a, a[i])); }
                catch (Throwable t) { unexpected(t); }
            for (int i : new int[] { n-2, n-1 })
                try { equal(i, Arrays.binarySearch(a, n-5, n, a[i])); }
                catch (Throwable t) { unexpected(t); }
            a[n-19] = (byte) 45;
            try { Arrays.sort(a, n-29, n); }
            catch (Throwable t) { unexpected(t); }
            equal(a[n-1], (byte) 45);
            equal(a[n-2], (byte) 44);
            equal(a[n-3], (byte) 43);
            equal(a[n-4], (byte)  0);
        }
        if ((tasks & 0x2) != 0) {
            System.out.println("Integer[]");
            System.gc();
            Integer[] a = new Integer[n];
            Integer ZERO = 0;
            Arrays.fill(a, ZERO);
            a[0]   =  -44;
            a[1]   =  -43;
            a[n-2] =  +43;
            a[n-1] =  +44;
            for (int i : new int[] { 0, 1, n-2, n-1 })
                try { equal(i, Arrays.binarySearch(a, a[i])); }
                catch (Throwable t) { unexpected(t); }
            for (int i : new int[] { n-2, n-1 })
                try { equal(i, Arrays.binarySearch(a, n-5, n, a[i])); }
                catch (Throwable t) { unexpected(t); }
            a[n-19] = 45;
            try { Arrays.sort(a, n-29, n); }
            catch (Throwable t) { unexpected(t); }
            equal(a[n-1],  45);
            equal(a[n-2],  44);
            equal(a[n-3],  43);
            equal(a[n-4],   0);
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
    static int intArg(String[] args, int i, int defaultValue) {
        return args.length > i ? Integer.parseInt(args[i]) : defaultValue;}
}
