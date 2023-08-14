public class T6622232 {
    public static void main(String[] args) throws Exception {
        new T6622232().run();
    }
    public void run() throws IOException {
        File javaFile = writeTestFile();
        File classFile = compileTestFile(javaFile);
        String output = javap(classFile);
        verifyNot(output,
                  "\\Q  Constant value: int 3Deprecated: true\\E",
                  "^Deprecated: true",
                  "\\Q   throws java.lang.Exception, java.lang.Error  Deprecated: true\\E"
               );
        if (errors > 0)
            throw new Error(errors + " found.");
    }
    File writeTestFile() throws IOException {
        File f = new File("Test.java");
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
        out.println("class Test { ");
        out.println("    @Deprecated static final int f1 = 3;");
        out.println("    @Deprecated int f2;");
        out.println("    @Deprecated void m() throws Exception, Error { }");
        out.println("}");
        out.close();
        return f;
    }
    File compileTestFile(File f) {
        int rc = com.sun.tools.javac.Main.compile(new String[] { "-g", f.getPath() });
        if (rc != 0)
            throw new Error("compilation failed. rc=" + rc);
        String path = f.getPath();
        return new File(path.substring(0, path.length() - 5) + ".class");
    }
    String javap(File f) {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        int rc = com.sun.tools.javap.Main.run(new String[] { "-v", f.getPath() }, out);
        if (rc != 0)
            throw new Error("javap failed. rc=" + rc);
        out.close();
        System.out.println(sw.toString());
        return sw.toString();
    }
    void verifyNot(String output, String... unexpects) {
        for (String unexpect: unexpects) {
            if (output.matches(unexpect))
                error(unexpect + " unexpectedly found");
        }
    }
    void error(String msg) {
        System.err.println(msg);
        errors++;
    }
    int errors;
}
