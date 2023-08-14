public class TestHelpOpts {
    public static void main(String... args) throws Exception {
        new TestHelpOpts().run();
    }
    void run() throws Exception {
        Locale prev = Locale.getDefault();
        try {
            Locale.setDefault(Locale.ENGLISH);
            String[] opts = { "-h", "-help", "-?", "--help" };
            for (String opt: opts)
                test(opt);
        } finally {
            Locale.setDefault(prev);
        }
        if (errors > 0)
            throw new Exception(errors + " errors occurred");
    }
    void test(String opt) {
        System.err.println("test " + opt);
        String[] args = { opt };
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        int rc = com.sun.tools.javah.Main.run(args, pw);
        pw.close();
        String out = sw.toString();
        if (!out.isEmpty())
            System.err.println(out);
        if (rc != 0)
            error("Unexpected exit: rc=" + rc);
        String flat = out.replaceAll("\\s+", " "); 
        if (!flat.contains("Usage: javah [options] <classes> where [options] include:"))
            error("expected text not found");
        if (flat.contains("main.opt"))
            error("key not found in resource bundle: " + flat.replaceAll(".*(main.opt.[^ ]*).*", "$1"));
    }
    void error(String msg) {
        System.err.println(msg);
        errors++;
    }
    int errors;
}
