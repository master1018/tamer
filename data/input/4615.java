public class T6358786 {
    public static void main(String... args) throws IOException {
        JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fm = tool.getStandardFileManager(null, null, null);
        String srcdir = System.getProperty("test.src");
        File file = new File(srcdir, args[0]);
        JavacTaskImpl task = (JavacTaskImpl)tool.getTask(null, fm, null, null, null, fm.getJavaFileObjectsFromFiles(Arrays.asList(file)));
        Elements elements = task.getElements();
        for (TypeElement clazz : task.enter(task.parse())) {
            String doc = elements.getDocComment(clazz);
            if (doc == null)
                throw new AssertionError(clazz.getSimpleName() + ": no doc comment");
            System.out.format("%s: %s%n", clazz.getSimpleName(), doc);
        }
    }
}
