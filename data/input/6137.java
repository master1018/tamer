public class NullConstructor {
    public static void main(String args[]) throws Exception {
        try {
            PrintStream ps = new PrintStream((OutputStream) null);
        } catch (Exception e) {
            return;
        }
        throw new Exception("PrintStream does not catch null constructor");
    }
}
