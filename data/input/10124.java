public class Test6795465 {
    static long var_1 = -1;
    void test() {
        long var_2 = var_1 * 1;
        var_2 = var_2 + (new byte[1])[0];
    }
    public static void main(String[] args) {
        Test6795465 t = new Test6795465();
        for (int i = 0; i < 200000; i++) {
            t.test();
        }
    }
}
