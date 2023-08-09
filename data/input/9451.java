public class TestJavacTask {
    static JavacTaskImpl getTask(JavaCompiler compiler, File... file) {
        StandardJavaFileManager fm = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> files =
            fm.getJavaFileObjectsFromFiles(Arrays.asList(file));
        return (JavacTaskImpl)compiler.getTask(null, fm, null, null, null, files);
    }
    public static void main(String... args) throws IOException {
        JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
        String srcdir = System.getProperty("test.src");
        File file = new File(srcdir, args[0]);
        JavacTaskImpl task = getTask(tool, file);
        for (TypeElement clazz : task.enter(task.parse()))
            System.out.println(clazz.getSimpleName());
    }
}
