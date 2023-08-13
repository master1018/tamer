public class T_freturn_8 {
    private synchronized float test() {
        return 0f;
    }
    public boolean run() {
        test();
        return true;
    }
}
