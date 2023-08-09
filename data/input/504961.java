public class T_areturn_8 {
    private synchronized String test() {
        return "abc";
    }
    public boolean run() {
        test();
        return true;
    }
}