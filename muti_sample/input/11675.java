public class TestExtcheckArgs {
    public static void realMain(String[] args) throws Throwable {
        String testJar = System.getenv("TESTJAVA") + File.separator
            + "lib" + File.separator + "jconsole.jar";
        verify(new String[] {
               }, Main.INSUFFICIENT);
        verify(new String[] {
                   "-verbose"
               }, Main.MISSING);
        verify(new String[] {
                   "-verbose",
                   "foo"
               }, Main.DOES_NOT_EXIST);
        verify(new String[] {
                   testJar,
                   "bar"
               }, Main.EXTRA);
        verify(new String[] {
                   "-verbose",
                   testJar,
                   "bar"
               }, Main.EXTRA);
    }
    static void verify(String[] args, String expected) throws Throwable {
        try {
            Main.realMain(args);
            fail();
        } catch (Exception ex) {
            if (ex.getMessage().startsWith(expected)) {
                pass();
            } else {
                fail("Unexpected message: " + ex.getMessage());
            }
        }
    }
    static volatile int passed = 0, failed = 0;
    static boolean pass() {passed++; return true;}
    static boolean fail() {failed++; Thread.dumpStack(); return false;}
    static boolean fail(String msg) {System.out.println(msg); return fail();}
    static void unexpected(Throwable t) {failed++; t.printStackTrace();}
    static boolean check(boolean cond) {if (cond) pass(); else fail(); return cond;}
    static boolean equal(Object x, Object y) {
        if (x == null ? y == null : x.equals(y)) return pass();
        else return fail(x + " not equal to " + y);}
    public static void main(String[] args) throws Throwable {
        try {realMain(args);} catch (Throwable t) {unexpected(t);}
        System.out.println("\nPassed = " + passed + " failed = " + failed);
        if (failed > 0) throw new AssertionError("Some tests failed");}
}
