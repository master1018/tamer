public class RemoveContains {
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
    public static void main(String[] args) {
        final Comparator<String> firstChar = new Comparator<String>() {
            public int compare(String x, String y) {
                return x.charAt(0) - y.charAt(0); }};
        test(new PriorityQueue<String>(10, firstChar));
        test(new PriorityBlockingQueue<String>(10, firstChar));
        test(new ArrayBlockingQueue<String>(10));
        test(new LinkedBlockingQueue<String>(10));
        test(new LinkedBlockingDeque<String>(10));
        test(new LinkedTransferQueue<String>());
        test(new ArrayDeque<String>(10));
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new Error("Some tests failed");
    }
    private static void test(Queue<String> q) {
        try {
            List<String> words =
                Arrays.asList("foo", "fee", "fi", "fo", "fum",
                              "Englishman");
            q.addAll(words);
            for (String word : words)
                check(q.contains(word));
            check(! q.contains("flurble"));
            check(q.remove("fi"));
            for (String word : words)
                check(q.contains(word) ^ word.equals("fi"));
            check(! q.remove("fi"));
            check(! q.remove("flurble"));
        } catch (Throwable t) { unexpected(t); }
    }
}
