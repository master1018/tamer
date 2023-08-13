public class T6411310 extends ToolTester {
    void test(String... args) throws IOException {
        JavaFileObject file = fm.getJavaFileForInput(PLATFORM_CLASS_PATH,
                                                     "java.lang.Object",
                                                     CLASS);
        String fileName = file.getName();
        if (!fileName.matches(".*java/lang/Object.class\\)?")) {
            System.err.println(fileName);
            throw new AssertionError(file);
        }
    }
    public static void main(String... args) throws IOException {
        new T6411310().test(args);
    }
}
