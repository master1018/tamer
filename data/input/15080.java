public class EmptyMapAndNulls {
    @SuppressWarnings("rawtypes")
    static void realMain(String[] args) throws Throwable {
        Map<String,String> comparable = new TreeMap<>();
        try {
            comparable.put(null, "anything");
            fail("null shouldn't be accepted");
        } catch (NullPointerException failed) {
            pass();
        }
        try {
            comparable.put("test", "anything");
            pass();
        } catch (NullPointerException failed) {
            fail();
        }
        try {
            comparable.put(null, "anything");
            fail("null shouldn't be accepted");
        } catch (NullPointerException failed) {
            pass();
        }
        Map comparator = new TreeMap(String.CASE_INSENSITIVE_ORDER);
        try {
            comparator.put(null, "anything");
            fail("null shouldn't be accepted");
        } catch (NullPointerException failed) {
            pass();
        }
        try {
            comparator.put("test", "anything");
            pass();
        } catch (NullPointerException failed) {
            fail();
        }
        try {
            comparator.put(null, "anything");
            fail("null shouldn't be accepted");
        } catch (NullPointerException failed) {
            pass();
        }
        comparator.clear();
        try {
            comparator.put(new Object(), "anything");
            fail("Object shouldn't be accepted");
        } catch (ClassCastException failed) {
            pass();
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
