public class T_return_wide_1 {
    public int run() {
        test();
        return 123456;
    }
    private static long test() {
        int a = 0xaaaa;
        int b = 0xbbbb;
        int c = 0xcccc;
        return 1l;
    }
}
