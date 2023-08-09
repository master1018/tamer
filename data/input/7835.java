public class T6437999 extends ToolTester {
    static class MyDiagnosticListener implements DiagnosticListener<JavaFileObject> {
        boolean error = false;
        public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
            error |= diagnostic.getKind() == Diagnostic.Kind.ERROR;
            System.out.println(diagnostic);
        }
    }
    void test(String... args) {
        Iterable<String> sourceLevel = Collections.singleton("6");
        MyDiagnosticListener dl = new MyDiagnosticListener();
        StandardJavaFileManager fm;
        Iterable<? extends JavaFileObject> files;
        dl.error = false;
        fm = getFileManager(tool, dl, Charset.forName("ASCII"));
        fm.handleOption("-source", sourceLevel.iterator());
        files = fm.getJavaFileObjects(new File(test_src, "Utf8.java"));
        tool.getTask(null, fm, null, null, null, files).call();
        if (!dl.error)
            throw new AssertionError("No error in ASCII mode");
        dl.error = false;
        fm = getFileManager(tool, dl, Charset.forName("UTF-8"));
        fm.handleOption("-source", sourceLevel.iterator());
        files = fm.getJavaFileObjects(new File(test_src, "Utf8.java"));
        task = tool.getTask(null, fm, null, null, null, files);
        if (dl.error)
            throw new AssertionError("Error in UTF-8 mode");
    }
    public static void main(String... args) {
        new T6437999().test(args);
    }
}
