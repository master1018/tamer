public class T4777949 {
    public static void main(String... args) throws Exception {
        new T4777949().run();
    }
    void run() throws Exception {
        File javaFile = writeTestFile();
        File classFile = compileTestFile(javaFile);
        test(".", "p.q.r.Test", false);
        test("p", "q.r.Test", true);
        test("p/q", "r.Test", true);
        test("p/q/r", "Test", true);
        test(".", "p.q.r.Test.Inner", false);
        test(".", "p.q.r.Test$Inner", false);
        test("p", "q.r.Test.Inner", true);
        test("p", "q.r.Test$Inner", true);
        if (errors > 0)
            throw new Exception(errors + " errors found");
    }
    void test(String classPath, String className, boolean expectWarnings) {
        List<Diagnostic<? extends JavaFileObject>> diags =
            javap(Arrays.asList("-classpath", classPath), Arrays.asList(className));
        boolean foundWarnings = false;
        for (Diagnostic<? extends JavaFileObject> d: diags) {
            if (d.getKind() == Diagnostic.Kind.WARNING)
                foundWarnings = true;
        }
    }
    File writeTestFile() throws IOException {
        File f = new File("Test.java");
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
        out.println("package p.q.r;");
        out.println("class Test { class Inner { } }");
        out.close();
        return f;
    }
    File compileTestFile(File f) {
        int rc = com.sun.tools.javac.Main.compile(new String[] { "-d", ".", f.getPath() });
        if (rc != 0)
            throw new Error("compilation failed. rc=" + rc);
        String path = f.getPath();
        return new File(path.substring(0, path.length() - 5) + ".class");
    }
    List<Diagnostic<? extends JavaFileObject>> javap(List<String> args, List<String> classes) {
        DiagnosticCollector<JavaFileObject> dc = new DiagnosticCollector<JavaFileObject>();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        JavaFileManager fm = JavapFileManager.create(dc, pw);
        JavapTask t = new JavapTask(pw, fm, dc, args, classes);
        boolean ok = t.run();
        List<Diagnostic<? extends JavaFileObject>> diags = dc.getDiagnostics();
        if (!ok)
            error("javap failed unexpectedly");
        System.err.println("args=" + args + " classes=" + classes + "\n"
                           + diags + "\n"
                           + sw);
        return diags;
    }
    void error(String msg) {
        System.err.println("error: " + msg);
        errors++;
    }
    int errors;
}
