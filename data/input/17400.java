public class Sibling {
    public static void main(String... args) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fm = compiler.getStandardFileManager(null, null, null);
        JavaFileObject sibling =
            fm.getJavaFileObjectsFromFiles(Arrays.asList(new File("Test.java")))
            .iterator().next();
        JavaFileObject classFile =  fm.getJavaFileForOutput(CLASS_OUTPUT,
                                                            "foo.bar.baz.Test",
                                                            CLASS,
                                                            sibling);
        File file = new File("Test.class").getAbsoluteFile();
        if (!classFile.toUri().equals(file.toURI()))
            throw new AssertionError("Expected " + file.toURI() + ", got " +
                                     classFile.toUri());
    }
}
