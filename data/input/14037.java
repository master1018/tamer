public class T4501660 {
    public static void main(String[] args) throws Exception {
        new T4501660().run();
    }
    public void run() throws IOException {
        String testClasses = System.getProperty("test.classes", ".");
        String output = javap("-classpath", testClasses, "-help", "T4501660");
        verify(output,
               "-public", "-protected", "-private", 
               "class T4501660"                     
               );
        if (errors > 0)
            throw new Error(errors + " found.");
    }
    String javap(String... args) {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        int rc = com.sun.tools.javap.Main.run(args, out);
        if (rc != 0)
            throw new Error("javap failed. rc=" + rc);
        out.close();
        System.out.println(sw);
        return sw.toString();
    }
    void verify(String output, String... expects) {
        for (String expect: expects) {
            if (output.indexOf(expect)< 0)
                error(expect + " not found");
        }
    }
    void error(String msg) {
        System.err.println(msg);
        errors++;
    }
    int errors;
}
