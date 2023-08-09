public class Test7041100 {
    static String n = null;
    public static void main(String[] args) throws Exception {
        for (int i = 0; i < 10000; i++) {
            stringEQ(args[0], args[1]);
            stringEQ(args[0], args[0]);
            stringEQ(args[0], n);
            stringEQ(n, args[0]);
        }
    }
    public static boolean stringEQ(String a, String b) {
        if (a == b)
            return true;
        if (a == null || b == null)
            return false;
        else
            return a.equals(b);
    }
}
