public class T_move_exception_2 {
    public boolean run() {
        try {
            int a = 15/0;
        } catch (ArithmeticException ae) {
            return true;
        }
        return false;
    }
}
