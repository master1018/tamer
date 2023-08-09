public class ConcurrentModification {
    static volatile int passed = 0, failed = 0;
    static void fail(String msg) {
        failed++;
        new AssertionError(msg).printStackTrace();
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
        check(condition, "Assertion failed");
    }
    private static void test(ConcurrentMap<Integer, Integer> m)
    {
        try {
            m.clear();
            check(m.isEmpty());
            m.put(1,2);
            Iterator<Map.Entry<Integer,Integer>> it = m.entrySet().iterator();
            if (it.hasNext()) {
                m.remove(1); 
                Map.Entry<Integer, Integer> e = it.next();
                check(m.isEmpty());
                check(e.getKey() == 1);
                check(e.getValue() == 2);
            }
        } catch (Throwable t) {unexpected(t);}
        try {
            m.clear();
            check(m.isEmpty());
            m.put(1,2);
            Iterator<Map.Entry<Integer,Integer>> it = m.entrySet().iterator();
            if (it.hasNext()) {
                m.put(1,3); 
                Map.Entry<Integer, Integer> e = it.next();
                check(e.getKey() == 1);
                check(e.getValue() == 2 || e.getValue() == 3);
                if (m instanceof ConcurrentHashMap) {
                    e.setValue(4);
                    check(m.get(1) == 4);
                }
            }
        } catch (Throwable t) {unexpected(t);}
    }
    public static void main(String[] args) {
        test(new ConcurrentHashMap<Integer,Integer>());
        test(new ConcurrentSkipListMap<Integer,Integer>());
        System.out.printf("%nPassed = %d, failed = %d%n%n", passed, failed);
        if (failed > 0) throw new Error("Some tests failed");
    }
}
