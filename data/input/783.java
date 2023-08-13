public class Safe {
    public static void main(String[] args) throws Exception {
        try {
            sun.misc.Unsafe.getUnsafe();
        } catch (Exception x) {
            System.err.println("Thrown as expected: " + x);
            return;
        }
        throw new Exception("No exception thrown");
    }
}
