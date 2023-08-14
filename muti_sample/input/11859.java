public class T6824493 {
    public static void main(String... args) {
        new T6824493().run();
    }
    void run() {
        test("-XDdetails:source",
            "for (int i = 0; i < 10; i++) {",
            "System.out.println(s + i);");
        test("-XDdetails:tryBlocks",
                "try[0]",
                "end try[0]",
                "catch[0]");
        test("-XDdetails:stackMaps",
                "StackMap locals:  this java/lang/String int",
                "StackMap stack:  java/lang/Throwable");
        test("-XDdetails:localVariables",
                "start local 3 
                "end local 3 
        test("-XDdetails:localVariableTypes",
                "start generic local 3 
                "end generic local 3 
        if (errors > 0)
            throw new Error(errors + " errors found");
    }
    void test(String option, String... expect) {
        String[] args = {
            "-c",
            "-classpath",
            testSrc + File.pathSeparator + testClasses,
            option,
            "Test"
        };
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        int rc = com.sun.tools.javap.Main.run(args, pw);
        if (rc != 0) {
            error("unexpected return code from javap: " + rc);
            return;
        }
        String out = sw.toString();
        System.out.println(out);
        for (String e: expect) {
            if (!out.contains(e))
                error("Not found: " + e);
        }
    }
    void error(String msg) {
        System.err.println("Error: " + msg);
        errors++;
    }
    private int errors;
    private String testSrc = System.getProperty("test.src", ".");
    private String testClasses = System.getProperty("test.classes", ".");
}
class Test {
    void m(String s) {
        for (int i = 0; i < 10; i++) {
            try {
                List<String> list = null;
                System.out.println(s + i);
            } catch (NullPointerException e) {
                System.out.println("catch NPE");
            } finally {
                System.out.println("finally");
            }
        }
    }
}
