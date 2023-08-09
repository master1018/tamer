public class Test6559156 {
    static final int N_TESTS = 1000000;
    public static void main(String[] args) throws Exception {
        Test6559156 test = new Test6559156();
        for (int i = 0; i < N_TESTS; i += 1) {
            test.doTest1(10, Integer.MAX_VALUE, i);
            test.doTest2(10, Integer.MAX_VALUE, i);
        }
        System.out.println("No failure");
    }
    void doTest1(int expected, int max, int i) {
        int counted;
        for (counted = 0;
             (counted <= max) && (counted < expected);
             counted += 1) {
        }
        if (counted != expected) {
            throw new RuntimeException("Failed test1 iteration=" + i +
                                       " max=" + max +
                                       " counted=" + counted +
                                       " expected=" + expected);
        }
    }
    void doTest2(int expected, int max, int i) {
        int counted;
        for (counted = 0;
             (counted < expected) && (counted <= max);
             counted += 1) {
        }
        if (counted != expected) {
            throw new RuntimeException("Failed test1 iteration=" + i +
                                       " max=" + max +
                                       " counted=" + counted +
                                       " expected=" + expected);
        }
    }
}
