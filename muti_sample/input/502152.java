public class T_return_object_8 {
    private synchronized String test() {
        return "abc";
    }
    public boolean run() {
        test();
        return true;
    }
}