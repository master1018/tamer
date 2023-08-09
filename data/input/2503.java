public class Test6992759 {
    static final int N_TESTS = 1000000000;
    public static void main(String[] args) throws Exception {
        Test6992759 test = new Test6992759();
        for (int i = 0; i < N_TESTS; i += 1) {
            test.doTest(10, Integer.MAX_VALUE, i);
        }
        System.out.println("No failure");
    }
    void doTest(int expected, int max, int i) {
        int counted;
        for (counted = 0;
             (counted <= max) && (counted < expected);
             counted += 1) {
        }
        if (counted != expected) {
            throw new RuntimeException("Failed test iteration=" + i +
                                       " max=" + max +
                                       " counted=" + counted +
                                       " expected=" + expected);
        }
    }
}
