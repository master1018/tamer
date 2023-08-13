public class T6414633 {
    public static void main(String... args) {
        String testSrc = System.getProperty("test.src", ".");
        String testClasses = System.getProperty("test.classes", ".");
        JavacTool tool = JavacTool.create();
        MyDiagListener dl = new MyDiagListener();
        StandardJavaFileManager fm = tool.getStandardFileManager(dl, null, null);
        try {
            fm.setLocation(StandardLocation.CLASS_PATH, Arrays.asList(new File(testClasses)));
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        Iterable<? extends JavaFileObject> files =
            fm.getJavaFileObjectsFromFiles(Arrays.asList(new File(testSrc, A.class.getName()+".java")));
        String[] opts = { "-proc:only",
                          "-processor", A.class.getName(),
                          "-classpath", testClasses + System.getProperty("path.separator") + "../../lib" };
        JavacTask task = tool.getTask(null, fm, dl, Arrays.asList(opts), null, files);
        task.call();
        if (dl.diags != 2)
            throw new AssertionError(dl.diags + " diagnostics reported");
    }
    private static class MyDiagListener implements DiagnosticListener<JavaFileObject>
    {
        public void report(Diagnostic d) {
            System.err.println(d);
            diags++;
        }
        int diags;
    }
}
