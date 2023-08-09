public class CircularityErrorTest {
    static Instrumentation ins;
    static void resolve() {
        try {
            Class c = A.class;
            throw new RuntimeException("Test failed - class A loaded by: " +
                c.getClassLoader());
        } catch (ClassCircularityError e) {
            System.err.println(e);
        }
    }
    public static void main(String args[]) throws Exception {
        resolve();
        ins.appendToBootstrapClassLoaderSearch(new JarFile("A.jar"));
        resolve();
    }
    public static void premain(String args, Instrumentation i) {
        ins = i;
    }
}
