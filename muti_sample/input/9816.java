public class T6963934 {
    public static void main(String[] args) throws Exception {
        File testSrc = new File(System.getProperty("test.src"));
        File thisSrc = new File(testSrc, T6963934.class.getSimpleName() + ".java");
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        JavacTask task = (JavacTask) compiler.getTask(null, fileManager, null, null, null,
                fileManager.getJavaFileObjects(thisSrc));
        CompilationUnitTree tree = task.parse().iterator().next();
        int count = 0;
        for (ImportTree importTree : tree.getImports()) {
            System.out.println(importTree);
            count++;
        }
        int expected = 7;
        if (count != expected)
            throw new Exception("unexpected number of imports found: " + count + ", expected: " + expected);
    }
}
