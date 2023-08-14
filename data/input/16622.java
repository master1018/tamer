public class T4459541 {
    public static void main(String[] args) throws Exception {
        new T4459541().run();
    }
    public void run() throws IOException {
        File javaFile = writeTestFile();
        File classFile = compileTestFile(javaFile);
        String output = javap(classFile);
        verify(output);
    }
    File writeTestFile() throws IOException {
        File f = new File("Test.java");
        out = new PrintWriter(new BufferedWriter(new FileWriter(f)));
        println("class Test {");
        println("void begin(int i) {");
        println("i++;");
        println("i++;");
        println("}");
        while (line < 32750)
            println("
        println("void before_32767(int i) {");
        println("i++;");
        println("i++;");
        println("}");
        while (line < 32768-4)
            println("
        println("void straddle_32768(int i) {");
        while (line < 32768+4)
            println("i++;");
        println("}");
        while (line < 65520)
            println("
        println("void between_32768_and_65536(int i) {");
        println("i++;");
        println("i++;");
        println("}");
        while (line < 65536-4)
            println("
        println("void straddle_65536(int i) {");
        while (line < 65536+4)
            println("i++;");
        println("}");
        println("}");
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
        int rc = com.sun.tools.javap.Main.run(new String[] { "-l", f.getPath() }, out);
        if (rc != 0)
            throw new Error("javap failed. rc=" + rc);
        out.close();
        return sw.toString();
    }
    void verify(String output) {
        System.out.println(output);
        if (output.indexOf("-") >= 0)
            throw new Error("- found in output");
    }
    void println(String text) {
        out.println(text);
        line++;
    }
    PrintWriter out;
    int line = 1;
}
