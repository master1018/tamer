public class T_dreturn_8 {
    private synchronized double test() {
        return 0d;
    }
    public boolean run() {
        test();
        return true;
    }
}
