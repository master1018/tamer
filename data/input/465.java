public class CheckSealed {
    public static void main(String[] argv) throws Exception {
        boolean fail = true;
        try {
            if ("1".equals(argv[0])) {
                test1();
                test2();
            } else if ("2".equals(argv[0])) {
                test2();
                test1();
            }
        } catch (java.lang.SecurityException e) {
            fail = false;
        }
        if (fail) {
            throw new Exception("Sealing violation undetected.");
        }
    }
    private static void test1() {
        p.A.hello();
    }
    private static void test2() {
        p.B.hello();
    }
}
