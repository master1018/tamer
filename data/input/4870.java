public class NoSecurityManager {
    private static final String className = "Dummy";
    private static final String classFileName = className + ".class";
    public static void main(String[] args) throws Exception {
        File dstDir = new File(System.getProperty("user.dir"), "codebase");
        if (!dstDir.exists()) {
            if (!dstDir.mkdir()) {
                throw new RuntimeException(
                    "could not create codebase directory");
            }
        }
        File dstFile = new File(dstDir, classFileName);
        File srcDir = new File(System.getProperty("test.classes", "."));
        File srcFile = new File(srcDir, classFileName);
        if (!dstFile.exists()) {
            if (!srcFile.exists()) {
                throw new RuntimeException(
                    "could not find class file to install in codebase " +
                    "(try rebuilding the test)");
            }
            if (!srcFile.renameTo(dstFile)) {
                throw new RuntimeException(
                    "could not install class file in codebase");
            }
        }
        if (srcFile.exists()) {
            if (!srcFile.delete()) {
                throw new RuntimeException(
                    "could not delete duplicate class file in CLASSPATH");
            }
        }
        URL codebaseURL = new URL("file", "",
            dstDir.getAbsolutePath().replace(File.separatorChar, '/') + "/");
        try {
            RMIClassLoader.loadClass(codebaseURL, className);
            throw new RuntimeException(
                "TEST FAILED: class loaded successfully from codebase");
        } catch (ClassNotFoundException e) {
            System.err.println(e.toString());
        }
        RMIClassLoader.loadClass(codebaseURL, "LocalDummy");
        System.err.println("TEST PASSED: local class loaded successfully");
        System.err.println("/nTest getClassLoader with no security manager set");
        ClassLoader loader = RMIClassLoader.getClassLoader("http:
        if (loader == Thread.currentThread().getContextClassLoader()) {
            System.err.println("TEST PASSED: returned context class loader");
        } else {
            throw new RuntimeException(
                "TEST FAILED: returned RMI-created class loader");
        }
    }
}
