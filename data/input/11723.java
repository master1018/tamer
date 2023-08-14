public class T6176978
{
    public static void main(String[] args) throws Exception {
        File tmpDir = new File("tmp");
        tmpDir.mkdirs();
        File testSrc = new File(System.getProperty("test.src", "."));
        String[] javac_args = {
            "-d",
            "tmp",
            new File(testSrc, "X.java").getPath()
        };
        int rc = com.sun.tools.javac.Main.compile(javac_args);
        if (rc != 0)
            throw new Error("javac exit code: " + rc);
        String[] jdoc_args = {
            "-doclet",
            "X",
            new File(testSrc, "T6176978.java").getPath()
        };
        rc = com.sun.tools.javadoc.Main.execute(jdoc_args);
        if (rc == 0)
            throw new Error("javadoc unexpectedly succeeded");
        Thread currThread = Thread.currentThread();
        ClassLoader saveClassLoader = currThread.getContextClassLoader();
        URLClassLoader urlCL = new URLClassLoader(new URL[] { tmpDir.toURL() });
        currThread.setContextClassLoader(urlCL);
        try {
            rc = com.sun.tools.javadoc.Main.execute(jdoc_args);
            if (rc != 0)
                throw new Error("javadoc exit: " + rc);
        }
        finally {
            currThread.setContextClassLoader(saveClassLoader);
        }
    }
}
