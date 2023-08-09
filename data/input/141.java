public class T6980017 {
    public static void main(String... args) throws Exception {
        new T6980017().run();
    }
    void run() throws Exception {
        String[] args = {
            "-v",
            "-XDdetails:source",
            "java.lang.Object"
        };
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        int rc = com.sun.tools.javap.Main.run(args, pw);
        pw.close();
        if (rc != 0)
            error("Unexpected exit code: " + rc);
        boolean foundBlankSourceLine = false;
        boolean foundNoSourceLine = false;
        for (String line: sw.toString().split("[\r\n]+")) {
            System.err.println(line);
            if (line.contains("Source code not available"))
                foundNoSourceLine = true;
            if (line.matches("\\s*\\( *[0-9]+\\)\\s*"))
                foundBlankSourceLine = true;
        }
        if (foundBlankSourceLine)
            error("found blank source lines");
        if (!foundNoSourceLine)
            error("did not find \"Source code not available\" message");
        if (errors > 0)
            throw new Exception(errors + " errors occurred");
    }
    void error(String msg) {
        System.err.println(msg);
        errors++;
    }
    int errors;
}
