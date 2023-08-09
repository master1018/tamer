public class T_opc_return_3 {
    private synchronized void test() {
        return;
    }
    public boolean run() {
        test();
        return true;
    }
}
