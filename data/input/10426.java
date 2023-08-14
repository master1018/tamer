public class DelegateToContextLoader {
    private static final String className = "Dummy";
    private static final String classFileName = className + ".class";
    public static void main(String[] args) throws Exception {
        TestLibrary.suggestSecurityManager("java.rmi.RMISecurityManager");
        URL codebaseURL = TestLibrary.
            installClassInCodebase(className, "codebase");
        ClassLoader codebaseLoader =
            new URLClassLoader(new URL[] { codebaseURL } );
        Thread.currentThread().setContextClassLoader(codebaseLoader);
        File srcDir = new File(TestLibrary.getProperty("test.classes", "."));
        URL dummyURL = new URL("file", "",
            srcDir.getAbsolutePath().replace(File.separatorChar, '/') +
            "/x-files/");
        try {
            Class cl = RMIClassLoader.loadClass(dummyURL, className);
            System.err.println("TEST PASSED: loaded class: " + cl);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                "TEST FAILED: target class in context class loader " +
                "not found using RMIClassLoader");
        }
    }
}
