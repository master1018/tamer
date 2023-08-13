public class T_ret_4_w {
    private void f2() {
    }
    private void f1() {
        f2();
    }
    public boolean run() {
       f1();
       return false;
    }
}
