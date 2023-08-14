public class T6900149 {
    public static void main(String[] args) throws IOException {
        DiagnosticCollector<JavaFileObject> diag =
                new DiagnosticCollector<JavaFileObject>();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fm =
                compiler.getStandardFileManager(null, null, null);
        File emptyFile = File.createTempFile("Empty", ".java");
        File[] files = new File[] { emptyFile, emptyFile };
        CompilationTask task = compiler.getTask(null, fm, diag,
                null, null, fm.getJavaFileObjects(files));
        if (! task.call()) {
            throw new AssertionError("compilation failed");
        }
    }
}
