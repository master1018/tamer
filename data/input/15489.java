public class T6622216 {
    public static void main(String[] args) throws Exception {
        new T6622216().run();
    }
    public void run() throws IOException {
        File javaFile = writeTestFile();
        File classFile = compileTestFile(javaFile);
        String output = javap(classFile);
        verify(output);
    }
    File writeTestFile() throws IOException {
        File f = new File("Outer.java");
        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
        out.println("class Outer {");
        out.println("    class Inner { }");
        out.println("}");
        out.close();
        return f;
    }
    File compileTestFile(File f) {
        int rc = com.sun.tools.javac.Main.compile(new String[] { f.getPath() });
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
        return sw.toString();
    }
    void verify(String output) {
        System.out.println(output);
        if (output.indexOf("InnerClasses") == -1)
            throw new Error("InnerClasses not found in output");
    }
}
