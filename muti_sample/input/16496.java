public class T6306137 {
    boolean error;
    final StandardJavaFileManager fm;
    final JavaCompiler compiler;
    Iterable<? extends JavaFileObject> files;
    DiagnosticListener<JavaFileObject> dl;
    T6306137() {
        dl = new DiagnosticListener<JavaFileObject>() {
                public void report(Diagnostic<? extends JavaFileObject> message) {
                    if (message.getKind() == Diagnostic.Kind.ERROR)
                        error = true;
                    System.out.println(message.getSource()
                                       +":"+message.getStartPosition()+":"
                                       +message.getStartPosition()+":"+message.getPosition());
                    System.out.println(message.toString());
                    System.out.format("Found problem: %s%n", message.getCode());
                    System.out.flush();
                }
        };
        compiler = ToolProvider.getSystemJavaCompiler();
        fm = compiler.getStandardFileManager(dl, null, null);
        String srcdir = System.getProperty("test.src");
        files =
            fm.getJavaFileObjectsFromFiles(Arrays.asList(new File(srcdir, "T6306137.java")));
    }
    void test(String encoding, boolean good) {
        error = false;
        Iterable<String> args = Arrays.asList("-source", "6", "-encoding", encoding, "-d", ".");
        compiler.getTask(null, fm, dl, args, null, files).call();
        if (error == good) {
            if (error) {
                throw new AssertionError("Error reported");
            } else {
                throw new AssertionError("No error reported");
            }
        }
    }
    public static void main(String[] args) {
        T6306137 self = new T6306137();
        self.test("utf-8", true);
        self.test("ascii", false);
        self.test("utf-8", true);
    }
}
