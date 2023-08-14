public class NCopies {
    static volatile int passed = 0, failed = 0;
    static void fail(String msg) {
        failed++;
        new AssertionError(msg).printStackTrace();
    }
    static void pass() {
        passed++;
    }
    static void unexpected(Throwable t) {
        failed++;
        t.printStackTrace();
    }
    static void check(boolean condition, String msg) {
        if (condition)
            passed++;
        else
            fail(msg);
    }
    static void check(boolean condition) {
        check(condition, "Assertion failure");
    }
    private static void checkEmpty(List<String> x) {
            check(x.isEmpty());
            check(x.size() == 0);
            check(x.indexOf("foo") == -1);
            check(x.lastIndexOf("foo") == -1);
            check(x.toArray().length == 0);
            check(x.toArray().getClass() == Object[].class);
    }
    private static void checkFoos(List<String> x) {
            check(! x.isEmpty());
            check(x.indexOf(new String("foo")) == 0);
            check(x.lastIndexOf(new String("foo")) == x.size()-1);
            check(x.toArray().length == x.size());
            check(x.toArray().getClass() == Object[].class);
            String[] sa = x.toArray(new String[x.size()]);
            check(sa.getClass() == String[].class);
            check(sa[0].equals("foo"));
            check(sa[sa.length-1].equals("foo"));
            check(x.get(x.size()/2).equals("foo"));
            checkEmpty(x.subList(x.size()/2, x.size()/2));
    }
    public static void main(String[] args) {
        try {
            List<String> empty = Collections.nCopies(0, "foo");
            checkEmpty(empty);
            checkEmpty(empty.subList(0,0));
            List<String> foos = Collections.nCopies(42, "foo");
            check(foos.size() == 42);
            checkFoos(foos.subList(foos.size()/2, foos.size()-1));
        } catch (Throwable t) { unexpected(t); }
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new Error("Some tests failed");
    }
}
