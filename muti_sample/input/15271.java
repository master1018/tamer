public class T6715251 {
    public static void main(String... args) throws Exception {
        new T6715251().run();
    }
    void run() throws Exception {
        String testClasses = System.getProperty("test.classes", ".");
        test(2);
        test(0, "-help");
        test(0, "-version");
        test(0, "-fullversion");
        test(0, "-classpath", testClasses, "T6715251");
        if (errors > 0)
            throw new Exception(errors + " errors received");
    }
    void test(int expect, String ... args) {
        int rc = javap(args);
        if (rc != expect)
            error("bad result: expected: " + expect + ", found " + rc + "\n"
                  + log);
    }
    int javap(String... args) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        int rc = com.sun.tools.javap.Main.run(args, pw);
        log = sw.toString();
        return rc;
    }
    void error(String msg) {
        System.err.println(msg);
        errors++;
    }
    String log;
    int errors;
}
