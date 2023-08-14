public class Tracer {
    static {
        ClassLoader cl = Tracer.class.getClassLoader();
        if (cl != null) {
            throw new RuntimeException("Tracer loaded by: " + cl);
        }
        System.out.println("Tracer loaded by boot class loader.");
    }
    public static void trace(String msg) {
        System.out.println(msg);
    }
}
