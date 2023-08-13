public class T_ireturn_8 {
    private synchronized int test() {
        return 0;
    }
    public boolean run() {
        test();
        return true;
    }
}
