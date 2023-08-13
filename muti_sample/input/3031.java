public class T4975569
{
    public static void main(String... args) {
        new T4975569().run();
    }
    void run() {
        verify("T4975569$Anno", "flags: ACC_INTERFACE, ACC_ABSTRACT, ACC_ANNOTATION");
        verify("T4975569$E",    "flags: ACC_FINAL, ACC_SUPER, ACC_ENUM");
        verify("T4975569$S",    "flags: ACC_BRIDGE, ACC_SYNTHETIC",
                                "InnerClasses:\n       static");
        verify("T4975569$V",    "void m(java.lang.String...)",
                                "flags: ACC_VARARGS");
        verify("T4975569$Prot", "InnerClasses:\n       protected");
        if (errors > 0)
            throw new Error(errors + " found.");
    }
    void verify(String className, String... expects) {
        String output = javap(className);
        for (String expect: expects) {
            if (output.indexOf(expect)< 0)
                error(expect + " not found");
        }
    }
    void error(String msg) {
        System.err.println(msg);
        errors++;
    }
    int errors;
    String javap(String className) {
        String newline = System.getProperty("line.separator");
        String testClasses = System.getProperty("test.classes", ".");
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        String[] args = { "-v", "-classpath", testClasses, className };
        int rc = com.sun.tools.javap.Main.run(args, out);
        if (rc != 0)
            throw new Error("javap failed. rc=" + rc);
        out.close();
        String output = sw.toString().replaceAll(newline, "\n");
        System.out.println("class " + className);
        System.out.println(output);
        return output;
    }
    List x() { return null; };
    class V { void m(String... args) { } }
    enum E { e; }
    @interface Anno { }
    static class S extends T4975569 {
        ArrayList x() { return null; }
    }
    protected class Prot { }
}
