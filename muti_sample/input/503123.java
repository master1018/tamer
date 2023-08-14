public class T_return_void_3 {
    private synchronized void test() {
        return;
    }
    public boolean run() {
        test();
        return true;
    }
}
