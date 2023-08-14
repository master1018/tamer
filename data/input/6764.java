public class Mkdir {
    static File a = new File("a");
    static File a_dot = new File(a, ".");
    static File a_dot_b = new File(a_dot, "b");
    public static void main(String[] args) throws Exception {
        if (!a_dot_b.mkdirs())
            throw new Exception("Test failed");
    }
}
