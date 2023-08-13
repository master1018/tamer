public class T6866657
{
    public static void main(String... args) {
        new T6866657().run();
    }
    void run() {
        verify("java.lang.Object");
        verify("java.lang.String");
        verify("java.util.List");
        verify("java.util.ArrayList");
        if (errors > 0)
            throw new Error(errors + " found.");
    }
    void verify(String className) {
        try {
            PrintWriter log = new PrintWriter(System.out);
            JavaFileManager fileManager = JavapFileManager.create(null, log);
            JavaFileObject fo = fileManager.getJavaFileForInput(StandardLocation.PLATFORM_CLASS_PATH, className, JavaFileObject.Kind.CLASS);
            if (fo == null) {
                error("Can't find " + className);
            } else {
                JavapTask t = new JavapTask(log, fileManager, null);
                t.handleOptions(new String[] { "-sysinfo", className });
                JavapTask.ClassFileInfo cfInfo = t.read(fo);
                expectEqual(cfInfo.cf.byteLength(), cfInfo.size);
            }
        } catch (Exception e) {
            e.printStackTrace();
            error("Exception: " + e);
        }
    }
    void expectEqual(int found, int expected) {
        if (found != expected)
            error("bad value found: " + found + " expected: " + expected);
    }
    void error(String msg) {
        System.err.println(msg);
        errors++;
    }
    int errors;
}
