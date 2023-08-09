public class T_return_wide_3 {
    private synchronized long test() {
        return 1;
    }
    public boolean run() {
        test();
        return true;
    }
}
