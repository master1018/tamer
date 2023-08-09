public class T4870651 {
    public static void main(String[] args) throws Exception {
        new T4870651().run();
    }
    public void run() throws IOException {
        verify("Test",
               "class Test<T extends java.lang.Object, " +
                   "E extends java.lang.Exception & java.lang.Comparable<T>, " +
                   "U extends java.lang.Comparable>",
               "v1(java.lang.String...)");
        verify("Test$Enum",
               "flags: ACC_FINAL, ACC_SUPER, ACC_ENUM",
               "flags: ACC_PUBLIC, ACC_STATIC, ACC_FINAL, ACC_ENUM");
        if (errors > 0)
            throw new Error(errors + " found.");
    }
    String javap(String className) {
        String testClasses = System.getProperty("test.classes", ".");
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        String[] args = { "-classpath", testClasses, "-v", className };
        int rc = com.sun.tools.javap.Main.run(args, out);
        if (rc != 0)
            throw new Error("javap failed. rc=" + rc);
        out.close();
        String output = sw.toString();
        System.out.println("class " + className);
        System.out.println(output);
        return output;
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
}
